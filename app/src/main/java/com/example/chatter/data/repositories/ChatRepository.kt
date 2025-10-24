package com.example.chatter.data.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.chatter.R
import com.example.chatter.data.model.Message
import com.example.chatter.data.remote.SupaBaseStorageClient
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

interface ChatRepository {
    fun sendMessage(
        messageText: String?,
        channelId: String,
        imageUrl: String?,
        channelName: String
    ): Result<Unit>

    fun getMessages(channelId: String): Flow<List<Message>>
    fun getAccessToken(): String
    suspend fun sendImageMessage(
        imageUri: Uri,
        channelId: String,
        channelName: String
    ): Result<Unit>

    fun postNotificationToUsers(channelName: String, message: Message, imageUrl: String?, channelId: String)

}

class ChatRepositoryImpl @Inject constructor(
    private val realTimeDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging,
    private val supaBaseStorageClient: SupaBaseStorageClient,
    @param:ApplicationContext private val context: Context
) :
    ChatRepository {
    override fun getMessages(channelId: String): Flow<List<Message>> = callbackFlow {
        val databaseRef =
            realTimeDatabase.getReference("messages").child(channelId).orderByChild("createdAt")

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull {
                    it.getValue(Message::class.java)
                }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        databaseRef.addValueEventListener(valueEventListener)

        firebaseMessaging.subscribeToTopic("group_$channelId")

        awaitClose {
            databaseRef.removeEventListener(valueEventListener)
        }
    }


    override fun sendMessage(
        messageText: String?,
        channelId: String,
        imageUrl: String?,
        channelName: String
    ): Result<Unit> {
        val message = Message(
            id = realTimeDatabase.reference.push().key ?: UUID.randomUUID().toString(),
            senderId = firebaseAuth.currentUser?.uid ?: "",
            senderName = firebaseAuth.currentUser?.displayName ?: "",
            message = messageText,
            senderImage = null,
            imageUrl = imageUrl
        )

        try {
            realTimeDatabase.reference.child("messages").child(channelId).push().setValue(message)
                .addOnSuccessListener {
                    postNotificationToUsers(channelName, message, null, channelId)
                }

            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun sendImageMessage(
        imageUri: Uri,
        channelId: String,
        channelName: String
    ): Result<Unit> {
        return try {
            val imageUrl = supaBaseStorageClient.uploadImage(uri = imageUri)
                ?: return Result.failure(Exception("Failed to upload image"))

            sendMessage(null, channelId, imageUrl, channelName = channelName).onFailure {
                return Result.failure(Exception("Failed to send message", it))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun postNotificationToUsers(channelName: String, message: Message, imageUrl: String?, channelId: String) {

        val fcmUrl = "https://fcm.googleapis.com/v1/projects/chatter-9515/messages:send"
        val jsonBody = JSONObject().apply {
            put("message", JSONObject().apply {
                put("topic", "group_$channelId")
                put("notification", JSONObject().apply {
                    put("title", "You have new messages in $channelName")
                    put("body", "${message.senderName}: ${message.message ?: "Image"}")
                })
            })

        }

        val requestBody = jsonBody.toString()

        val request = object : StringRequest(Method.POST, fcmUrl, Response.Listener { response ->
            Log.d("FCM", "Response: $response")

        }, Response.ErrorListener { error ->
            Log.e("FCM", "Error: ${error.message}")

        }) {
            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${getAccessToken()}"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        Volley.newRequestQueue(context).add(request)
    }

    override fun getAccessToken(): String {
        val inputStream = context.resources.openRawResource(R.raw.chatter_key)
        val googleCred = GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        return googleCred.refreshAccessToken().tokenValue

    }
}