package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Dialogue
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel

@Composable
fun MainScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    
    // Manage whether user is in DialoguePracticeScreen
    var activePracticeDialogue by remember { mutableStateOf<Dialogue?>(null) }
    
    // Bottom Navigation tab index (from left to right: 0=Settings, 1=Dialogues, 2=Rules, 3=Progress)
    var selectedTab by remember { mutableStateOf(1) } // Default to Dialogues (حوارات) for great starting UX!

    // Wrap the entire app in our dynamic custom theme
    MyApplicationTheme(
        darkTheme = settings.isDarkMode,
        themeColorHex = settings.themeColorHex
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (!settings.isLoggedInWithGoogle) {
                LoginScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (activePracticeDialogue != null) {
                // Display active interactive practice view
                DialoguePracticeScreen(
                    viewModel = viewModel,
                    onBackClicked = {
                        activePracticeDialogue = null
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Show Main tab bar view with bottom navigation bar
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            // Tab 0: Settings (الإعدادات)
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 0) Icons.Default.Settings else Icons.Outlined.Settings,
                                        contentDescription = "الإعدادات"
                                    )
                                },
                                label = { Text("الإعدادات", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )

                            // Tab 1: Dialogues (الحوارات)
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 1) Icons.Default.QuestionAnswer else Icons.Outlined.QuestionAnswer,
                                        contentDescription = "الحوارات"
                                    )
                                },
                                label = { Text("الحوارات", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )

                            // Tab 2: Rules (القواعد)
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 2) Icons.Default.MenuBook else Icons.Outlined.MenuBook,
                                        contentDescription = "القواعد"
                                    )
                                },
                                label = { Text("القواعد", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )

                            // Tab 3: Progress Rate (نسبة التقدم)
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 3) Icons.Default.BarChart else Icons.Outlined.BarChart,
                                        contentDescription = "التقدم"
                                    )
                                },
                                label = { Text("التقدم", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    },
                    modifier = modifier
                ) { innerPadding ->
                    // Animated transition between main tabs
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "تنقل التبويبات",
                        modifier = Modifier.padding(innerPadding)
                    ) { tab ->
                        when (tab) {
                            0 -> SettingsScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                            1 -> DialoguesScreen(
                                viewModel = viewModel,
                                onDialogueSelected = { dialogue ->
                                    viewModel.startDialoguePractice(dialogue)
                                    activePracticeDialogue = dialogue
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                            2 -> RulesScreen(modifier = Modifier.fillMaxSize())
                            3 -> ProgressScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}
