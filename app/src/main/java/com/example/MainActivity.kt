package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.screens.MainScreen
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Database, DAOs, and repository securely
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(
            userSettingsDao = database.userSettingsDao(),
            userProgressDao = database.userProgressDao(),
            userMistakeDao = database.userMistakeDao()
        )

        // Instantiate AppViewModel via Factory
        val factory = AppViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[AppViewModel::class.java]

        setContent {
            MainScreen(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
