package com.example.chatter.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chatter.data.model.Channel
import com.example.chatter.ui.theme.ChatterTheme
import com.example.chatter.ui.theme.DarkGray
import com.example.chatter.utils.OneTimeScreenUiEvent

@Composable
fun HomeScreen(modifier: Modifier = Modifier, onNavigateToChatScreen: (String, String) -> Unit) {
    val homeViewModel = hiltViewModel<HomeViewModel>()
    val uiState: HomeUiState by homeViewModel.homeUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        homeViewModel.eventFlow.collect {
            when (it) {
                is OneTimeScreenUiEvent.ShowToast -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }

    ScreenLayout(
        modifier = modifier,
        uiState = uiState,
        onAddChannelClick = { homeViewModel.addNewChannel() },
        onNewChannelNameChange = homeViewModel::onNewChannelNameChange,
        onChannelClick = onNavigateToChatScreen
    )


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenLayout(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onAddChannelClick: () -> Unit,
    onNewChannelNameChange: (String) -> Unit,
    onChannelClick: (String, String) -> Unit
) {
    val addChannelDialog = remember { mutableStateOf(false) }

    if (!uiState.isLoading) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(color = Color.Black)
        ) {

            Text(
                text = "Messages",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )


            TextField(
                value = "",
                onValueChange = {},
                placeholder = {
                    Text(
                        text = "Search...."
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(40.dp),
                colors = TextFieldDefaults.colors().copy(
                    unfocusedContainerColor = DarkGray,
                    focusedContainerColor = DarkGray,
                    focusedTextColor = Color.Gray,
                    unfocusedTextColor = Color.Gray,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search Icon"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)

            )

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    items(uiState.channels) {
                        ChannelListItem(channel = it, onChannelClick = onChannelClick)
                    }
                }


                if (
                    addChannelDialog.value
                ) {
                    ModalBottomSheet(
                        onDismissRequest = { addChannelDialog.value = false },
                    ) {

                        AddChannelDialog(
                            onAddChannelClick = {
                                onAddChannelClick()
                                addChannelDialog.value = false
                            },
                            onNewChannelNameChange = onNewChannelNameChange,
                            uiState = uiState
                        )

                    }

                }

                FloatingActionButton(
                    onClick = {
                        addChannelDialog.value = true
                    }, modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Text(text = "Add Channel", modifier = Modifier.padding(16.dp))
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ChannelListItem(
    modifier: Modifier = Modifier,
    channel: Channel,
    onChannelClick: (String, String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(color = DarkGray, shape = RoundedCornerShape(16.dp))
            .clickable(
                onClick = { onChannelClick(channel.id, channel.name) }
            ), verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(72.dp)
                .background(
                    color = Color.Yellow.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = channel.name.first().uppercase(),
                fontSize = 36.sp,
                color = Color.White
            )
        }

        Text(
            text = channel.name,
            color = Color.White,
            modifier = Modifier.padding(8.dp)
        )
    }

}

@Composable
fun AddChannelDialog(
    modifier: Modifier = Modifier,
    onAddChannelClick: () -> Unit,
    onNewChannelNameChange: (String) -> Unit,
    uiState: HomeUiState
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Add Channel",
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = uiState.newChannelName, onValueChange = onNewChannelNameChange,
            label = {
                Text("Channel Name")
            },
            supportingText = {
                Text("Enter the name of the channel")
            },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddChannelClick,
            enabled = uiState.newChannelName.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            if (!uiState.isNewChannelAdding) {
                Text("Add Channel")
            } else {
                CircularProgressIndicator()
            }
        }

    }


}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ChatterTheme {
        ScreenLayout(
            uiState = HomeUiState(),
            onAddChannelClick = {},
            onNewChannelNameChange = {},
            onChannelClick = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenDialogPreview() {
    ChatterTheme {
        AddChannelDialog(
            onAddChannelClick = {},
            onNewChannelNameChange = {},
            uiState = HomeUiState()
        )

    }

}

@Preview(showBackground = true)
@Composable
fun ScreenChannelItemPreview() {
    ChatterTheme {
        ChannelListItem(channel = Channel(name = "Test Channel"), onChannelClick = { _, _ -> })

    }

}