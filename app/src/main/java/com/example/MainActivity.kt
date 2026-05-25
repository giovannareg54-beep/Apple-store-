package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.vm.VirtualOSContainer
import com.example.ui.vm.VirtualOSViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel = remember { VirtualOSViewModel() }
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color(0xFF1E272C))
              .padding(innerPadding)
          ) {
            VirtualOSContainer(viewModel = viewModel)
          }
        }
      }
    }
  }
}

