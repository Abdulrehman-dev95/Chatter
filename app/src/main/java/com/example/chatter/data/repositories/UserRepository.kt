package com.example.chatter.data.repositories

import com.example.chatter.data.model.Channel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

interface UserRepository {
    fun getChannels(): Flow<List<Channel>>
    suspend fun addChannel(channel: Channel): Result<Unit>
}

class UserRepositoryImpl @Inject constructor(
    private val realTimeDatabase: FirebaseDatabase,
    private val firebaseMessaging: FirebaseMessaging
) :
    UserRepository {
    override fun getChannels(): Flow<List<Channel>> = callbackFlow {
        val channelRef = realTimeDatabase.getReference("channel")

        val channelListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val channel = snapshot.children.map {
                    Channel(
                        id = it.key ?: "channel",
                        name = it.value.toString()
                    )
                }
                trySend(channel)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }

        }
        channelRef.addValueEventListener(channelListener)

        awaitClose {
            channelRef.removeEventListener(channelListener)
        }

    }

    override suspend fun addChannel(channel: Channel): Result<Unit> {
        try {
            val channelRef = realTimeDatabase.getReference("channel")
            val newRef = channelRef.push()
            newRef.setValue(channel.name)
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
