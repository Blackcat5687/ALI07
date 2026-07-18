package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Dialogue
import com.example.data.SampleDialogues
import com.example.data.UserProgressEntity
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialoguesScreen(
    viewModel: AppViewModel,
    onDialogueSelected: (Dialogue) -> Unit,
    modifier: Modifier = Modifier
) {
    val progressList by viewModel.allProgress.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    var selectedCategory by remember { mutableStateOf("الكل") }

    val categories = listOf("الكل", "الحياة اليومية", "السفر والسياحة", "الأعمال والمهن")

    val filteredDialogues = if (selectedCategory == "الكل") {
        SampleDialogues.dialogues
    } else {
        SampleDialogues.dialogues.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "الحوارات",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "تصفية الحوارات",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // 1. Interactive Category Filters (Horizontal scroll, standard RTL matching Arabic layout)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF3B82F6),
                            selectedLabelColor = Color.White,
                            containerColor = if (settings.isDarkMode) Color(0xFF374151) else Color(0xFFE5E7EB),
                            labelColor = if (settings.isDarkMode) Color.LightGray else Color(0xFF4B5563)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = null
                    )
                }
            }

            // 2. Dialogues Vertical List
            if (filteredDialogues.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "لا توجد حوارات في هذا القسم حالياً.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(filteredDialogues) { dialogue ->
                        val dialogueProgress = progressList.find { it.dialogueId == dialogue.id }

                        DialogueCard(
                            dialogue = dialogue,
                            userProgress = dialogueProgress,
                            onClick = { onDialogueSelected(dialogue) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun getDialogueIconInfo(title: String): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
    return when {
        title.contains("المطار") -> Pair(Icons.Default.Flight, Color(0xFF3B82F6))
        title.contains("الفندق") -> Pair(Icons.Default.Hotel, Color(0xFFF59E0B))
        title.contains("المطعم") -> Pair(Icons.Default.Restaurant, Color(0xFFEF4444))
        title.contains("التسوق") -> Pair(Icons.Default.ShoppingCart, Color(0xFF10B981))
        title.contains("عمل") || title.contains("مقابلة") -> Pair(Icons.Default.Work, Color(0xFF8B5CF6))
        else -> Pair(Icons.Default.Book, Color(0xFF3B82F6))
    }
}

@Composable
fun DialogueCard(
    dialogue: Dialogue,
    userProgress: UserProgressEntity?,
    onClick: () -> Unit
) {
    val isCompleted = userProgress?.isCompleted == true
    val progressPct = if (userProgress != null) {
        if (userProgress.isCompleted) 100
        else if (userProgress.totalTurns > 0) {
            (userProgress.turnsCompleted * 100 / userProgress.totalTurns)
        } else 0
    } else 0
    val progressRatio = if (userProgress != null) {
        if (userProgress.isCompleted) 1f
        else if (userProgress.totalTurns > 0) {
            userProgress.turnsCompleted.toFloat() / userProgress.totalTurns
        } else 0f
    } else 0f

    val (icon, tintColor) = getDialogueIconInfo(dialogue.title)

    val levelColor = when (dialogue.level) {
        "سهل" -> Color(0xFF10B981) // Green
        "متوسط" -> Color(0xFFF59E0B) // Amber
        "متقدم" -> Color(0xFFEF4444) // Red
        else -> Color(0xFF3B82F6)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Content: Icon and details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.weight(1f)
            ) {
                // Circle Badge with Icon
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(tintColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = dialogue.title,
                        tint = tintColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title, level, progress
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dialogue.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Level badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(levelColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = dialogue.level,
                            color = levelColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Progress Bar + %
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "$progressPct%",
                            fontSize = 11.sp,
                            color = if (isCompleted) Color(0xFF10B981) else Color(0xFF6B7280),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (isCompleted) Color(0xFF10B981) else tintColor,
                            trackColor = if (isCompleted) Color(0xFF10B981).copy(alpha = 0.15f) else tintColor.copy(alpha = 0.15f)
                        )
                    }
                }
            }

            // Right Content: stats + Start (ابدأ) Button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                // Info: turns and time
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${dialogue.turnsCount} جملة",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = dialogue.durationText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Start button (ابدأ)
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tintColor
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = "ابدأ",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
