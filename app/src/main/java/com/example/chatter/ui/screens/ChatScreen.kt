package com.example.chatter.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.chatter.CallActivity
import com.example.chatter.R
import com.example.chatter.data.model.Message
import com.example.chatter.ui.theme.ChatterTheme
import com.example.chatter.ui.theme.ItemBg
import com.example.chatter.ui.theme.OffBlack
import com.example.chatter.ui.theme.Purple
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.io.File

@Composable
fun ChatScreen(modifier: Modifier = Modifier, channelId: String, channelName: String) {
    val viewModel: ChatViewModel = hiltViewModel()



    LaunchedEffect(key1 = true) {
        viewModel.getMessages(channelId)
    }
    val uiState by viewModel.chatUiState.collectAsStateWithLifecycle()


    ChatScreenLayout(
        modifier = modifier,
        channelId = channelId,
        channelName = channelName,
        uiState = uiState,
        onMessageChange = viewModel::onMessageChange,
        onSendMessage = {
            viewModel.sendMessage(channelId, uiState.message)
        },
        onImageClick = {
            viewModel.sendImage(it, channelId)
        }
    )


}

@Composable
fun ChatScreenLayout(
    modifier: Modifier = Modifier,
    channelId: String,
    channelName: String,
    uiState: ChatUIState,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onImageClick: (Uri) -> Unit
) {
    val userName = Firebase.auth.currentUser!!.displayName!!
    val userId = Firebase.auth.currentUser!!.uid


    val appID = 566529291L
    val appSign = "13f48aa1d21d45f2969152627179851a18400ffa78209d666deb12ce247fc7b2"

    val showImagePreviewDialog = remember {
        mutableStateOf<String?>(null)
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val imageOptionsDialog = remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current
    val imageFile = remember {
        File(context.externalCacheDir, "${System.currentTimeMillis()}.jpg")
    }
    val imageUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onImageClick.invoke(imageUri)
        }

    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(imageUri)
        } else (
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                )
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        if (
            it != null
        ) {
            onImageClick.invoke(it)
        }

    }


    fun startCall(isVideo: Boolean) {
        val intent = Intent(context, CallActivity::class.java).apply {
            putExtra("userID", userId)
            putExtra("userName", userName)
            putExtra("callID", channelId)
            putExtra("appID", appID)
            putExtra("appSign", appSign)
            putExtra("isVideo", isVideo)
        }
        context.startActivity(intent)
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OffBlack)
            .padding(horizontal = 4.dp)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(color = ItemBg, shape = RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = channelName,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call Icon",
                tint = Purple,
                modifier = Modifier
                    .size(32.dp)
                    .clickable(
                        onClick = {
                            startCall(isVideo = false)
                        }
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Default.VideoCall,
                contentDescription = "Video Call Icon",
                tint = Purple,
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        startCall(isVideo = true)
                    }
            )

        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(uiState.messages) { chatMessage ->
                val isCurrentUser = chatMessage.senderId == Firebase.auth.currentUser?.uid
                ChatBubbleItem(
                    message = chatMessage,
                    isCurrentUser = isCurrentUser,
                    onImageClick = {
                        showImagePreviewDialog.value = it
                    }
                )
            }

        }

        OutlinedTextField(
            value = uiState.message,
            onValueChange = onMessageChange,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
            }),
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.send),
                    contentDescription = "Send Message",
                    tint = Purple,
                    modifier = Modifier.clickable(
                        onClick = onSendMessage,
                        enabled = uiState.message.isNotBlank(),
                    )
                )
            },
            leadingIcon = {
                IconButton(
                    onClick = {
                        imageOptionsDialog.value = true
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.attach),
                        contentDescription = "Attach Files",
                        tint = Purple
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ItemBg,
                unfocusedContainerColor = ItemBg,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedTrailingIconColor = Color.White,
                unfocusedTrailingIconColor = Color.White,

                ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)

        )

        if (imageOptionsDialog.value) {
            ImageOptionsDialog(
                onDismissRequest = { imageOptionsDialog.value = false },
                onCameraClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        cameraLauncher.launch(imageUri)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                onGalleryClick = {
                    galleryLauncher.launch("image/*")
                }
            )
        }

        showImagePreviewDialog.value?.let {
            ImagePreviewDialog(it) {
                showImagePreviewDialog.value = null
            }
        }
    }
}


@Composable
fun ChatBubbleItem(
    modifier: Modifier = Modifier,
    message: Message,
    isCurrentUser: Boolean = false,
    onImageClick: (String) -> Unit = {}
) {
    val color = if (isCurrentUser) {
        Purple
    } else {
        ItemBg
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 8.dp
            ),
        horizontalArrangement = if (isCurrentUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        },
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (!isCurrentUser) {
            Image(
                painter = painterResource(R.drawable.friend),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = color
            ), shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            message.imageUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(200.dp)
                        .clickable(
                            onClick = { onImageClick.invoke(it) }
                        )
                )
            }

            message.message?.let {
                Text(
                    text = it.trim(),
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ImageOptionsDialog(
    onDismissRequest: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        title = {
            Text(text = "Image Options")
        },
        text = {
            Text(text = "Choose an option to proceed.")
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCameraClick()
                    onDismissRequest()
                }
            ) {
                Text("Camera")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onGalleryClick()
                    onDismissRequest()
                }
            ) {
                Text("Gallery")
            }
        }
    )
}

@Composable
fun ImagePreviewDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Image Preview",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ChatScreenUiPreview() {
    ChatterTheme {
        ChatBubbleItem(
            message = Message(
                id = "1",
                senderId = "1",
                senderName = "1",
                message = "Hi! This is Chatter",
                senderImage = null,
                imageUrl = null
            )
        )
    }

}
