package com.example.chatter.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.chatter.ui.navigation.NavigationHost

@Composable
fun MainApp() {
    Scaffold(modifier = Modifier.fillMaxSize(), containerColor = Color.White) {
        NavigationHost(
            modifier = Modifier
                .padding(it)
        )
    }
}