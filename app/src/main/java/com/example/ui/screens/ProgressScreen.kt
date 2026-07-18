package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserMistakeEntity
import com.example.data.UserProgressEntity
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val progressList by viewModel.allProgress.collectAsState()
    val mistakesList by viewModel.allMistakes.collectAsState()
    val settings by viewModel.userSettings.collectAsState()

    // Aggregate statistics
    val totalDialogues = 5
    val completedCount = progressList.count { it.isCompleted }
    val startedCount = progressList.size - completedCount

    val totalTimeSpentSeconds = progressList.sumOf { it.timeSpentSeconds }
    val timeSpentMinutes = (totalTimeSpentSeconds / 60) + if (totalTimeSpentSeconds % 60 > 0) 1 else 0

    val averageScore = if (progressList.isNotEmpty()) {
        progressList.map { it.averageScore }.average().toInt()
    } else {
        0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "التقدم",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(if (settings.isDarkMode) Color(0xFF111827) else Color(0xFFF3F4F6))
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {
            // 1. Row of 3 Modern Quick Stats Cards (Matches Screenshot precisely)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniStatCard(
                        value = "$completedCount من $totalDialogues",
                        label = "المكتملة",
                        tintColor = Color(0xFF3B82F6),
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        value = "$averageScore%",
                        label = "دقة النطق",
                        tintColor = Color(0xFF10B981),
                        icon = Icons.Default.GraphicEq,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        value = "$timeSpentMinutes دقيقة",
                        label = "وقت التدريب",
                        tintColor = Color(0xFFF59E0B),
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Beautiful Trend Chart
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "مخطط تطور النطق",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "معدل دقة اللفظ وتطورك اليومي في آخر الجلسات",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (progressList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "ابدأ حوارك الأول لترى مخطط دقة اللفظ وتطورك هنا!",
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                        } else {
                            val scores = progressList.map { it.averageScore }
                            ProgressLineChart(
                                scores = scores,
                                accentColor = Color(0xFF3B82F6),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            )
                        }
                    }
                }
            }

            // 3. New Card: مخطط وقت التدريب اليومي (Daily Training Time Chart)
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "مخطط وقت التدريب اليومي",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "إجمالي وقت التدريب الفعلي بالدقائق في آخر 5 أيام نشطة",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        val sdf = remember { java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault()) }
                        val dailyData = remember(progressList) {
                            progressList
                                .groupBy { sdf.format(java.util.Date(it.lastPracticed)) }
                                .mapValues { entry ->
                                    val totalSeconds = entry.value.sumOf { it.timeSpentSeconds }
                                    totalSeconds / 60f
                                }
                                .toList()
                                .sortedBy { it.first }
                                .takeLast(5)
                        }

                        if (dailyData.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "ابدأ حوارك الأول لترى مخطط وقت تدريبك اليومي هنا!",
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                        } else {
                            TrainingBarChart(
                                dailyData = dailyData,
                                accentColor = Color(0xFF10B981), // Beautiful Green for training time progress
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            // 4. Pronunciation Mistakes Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "الأخطاء الصوتية المتكررة",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                )
            }

            if (mistakesList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SentimentSatisfiedAlt,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "ممتاز! لا توجد أخطاء لفظية مسجلة حالياً.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                "عندما تخطئ بنطق كلمة أثناء المحادثات، سيقوم المعلم الذكي بحفظها هنا مع نصائح لتعديلها.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(mistakesList) { mistake ->
                    MistakeItemRow(
                        mistake = mistake,
                        onDeleteClicked = { viewModel.removeMistake(mistake.word) }
                    )
                }
            }
        }
    }
}

@Composable
fun MiniStatCard(
    value: String,
    label: String,
    tintColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circle Badge with icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tintColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tintColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProgressLineChart(
    scores: List<Float>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val maxScore = 100f

        val pointsCount = scores.size
        val stepX = if (pointsCount > 1) width / (pointsCount - 1) else width
        
        // Draw grid lines
        val gridLines = 4
        val stepY = height / gridLines
        for (i in 0..gridLines) {
            val y = i * stepY
            drawLine(
                color = Color.LightGray.copy(alpha = 0.2f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (scores.isNotEmpty()) {
            val path = Path()
            
            scores.forEachIndexed { index, score ->
                val x = index * stepX
                val y = height - (score / maxScore) * height
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                drawCircle(
                    color = accentColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            drawPath(
                path = path,
                color = accentColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun TrainingBarChart(
    dailyData: List<Pair<String, Float>>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val maxMinutes = (dailyData.maxOfOrNull { it.second } ?: 10f).coerceAtLeast(1f)
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        dailyData.forEach { (dateStr, minutes) ->
            // Proportional height
            val progressFactor = (minutes / maxMinutes).coerceIn(0f, 1f)
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                // Time label
                Text(
                    text = String.format(java.util.Locale.US, "%.1f د", minutes),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                // The Bar
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .weight(1f, fill = false)
                        .fillMaxHeight(progressFactor)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(accentColor)
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Date label
                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun MistakeItemRow(
    mistake: UserMistakeEntity,
    onDeleteClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete mistake button (Mastered)
                IconButton(onClick = onDeleteClicked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "تم التجاوز بنجاح",
                        tint = Color(0xFF10B981)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Mistake details
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Red.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "كررتها ${mistake.count} مرات",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = mistake.word,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "السياق: \"${mistake.lastSeenSentence}\"",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AI correction tip box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "نصيحة المعلم",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mistake.correctionTip,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Right,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
