package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.AberApp
import com.example.ui.theme.AberTheme
import com.example.data.AberRepository
import com.example.data.supabase.SupabaseSessionStore

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AberRepository.instance.configureSessionStore(SupabaseSessionStore(applicationContext))
    enableEdgeToEdge()
    setContent {
      AberTheme {
        AberApp()
      }
    }
  }
}

