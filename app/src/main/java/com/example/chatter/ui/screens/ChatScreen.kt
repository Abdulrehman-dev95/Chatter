package com.example.chatter.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.chatter.R
import com.example.chatter.data.model.Message
import com.example.chatter.ui.theme.ChatterTheme
import com.example.chatter.ui.theme.DarkGray
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
    channelName: String,
    uiState: ChatUIState,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onImageClick: (Uri) -> Unit
) {
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


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 4.dp)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = DarkGray, shape = RoundedCornerShape(16.dp))
        ) {
            Text(
                text = channelName,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(uiState.messages) {
                val isCurrentUser = it.senderId == Firebase.auth.currentUser?.uid
                ChatBubbleItem(
                    message = it,
                    isCurrentUser = isCurrentUser
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
                    tint = Color.Unspecified,
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
                        tint = Color.Unspecified
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkGray,
                unfocusedContainerColor = DarkGray,
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


    }


}

@Composable
fun ChatBubbleItem(
    modifier: Modifier = Modifier,
    message: Message,
    isCurrentUser: Boolean = false
) {
    val color = if (isCurrentUser) {
        Purple
    } else {
        DarkGray
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
                Image(
                    painter = rememberAsyncImagePainter(model = it),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(200.dp)
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
                }
            ) {
                Text("Camera")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onGalleryClick()
                }
            ) {
                Text("Gallery")
            }
        }
    )
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


