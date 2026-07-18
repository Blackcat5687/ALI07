package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import android.widget.Toast
import com.example.data.UserSettingsEntity
import com.example.ui.viewmodel.AppViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var micPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        micPermissionGranted = isGranted
        if (isGranted) {
            Toast.makeText(context, "تم تفعيل صلاحية المايكروفون بنجاح!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "تم رفض صلاحية المايكروفون. يرجى تفعيلها من إعدادات الهاتف لتستطيع التحدث مع الذكاء الاصطناعي.", Toast.LENGTH_LONG).show()
        }
    }

    var apiKeyText by remember(settings.geminiApiKey) { mutableStateOf(settings.geminiApiKey) }
    var apiKeyVisible by remember { mutableStateOf(false) }

    val colorsList = listOf(
        "#0D9488" to "تركوازي",
        "#0288D1" to "أزرق سماوي",
        "#7C4DFF" to "بنفسجي ملكي",
        "#E91E63" to "وردي",
        "#2E7D32" to "أخضر الغابة",
        "#FF8F00" to "برتقالي دافئ"
    )

    val fontList = listOf(
        "Default" to "خط النظام الافتراضي",
        "SansSerif" to "خط سان-سيريف الحديث",
        "Serif" to "الخط الكلاسيكي الأنيق",
        "Monospace" to "خط المطورين التقني"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "الإعدادات",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
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
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // 1. Profile Section Card (Matches Screenshot precisely)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEditDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (settings.isDarkMode) Color(0xFF1F2937) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            // Grey Circular Avatar with Person Icon
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(if (settings.isDarkMode) Color(0xFF374151) else Color(0xFFE5E7EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (settings.googlePhotoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = settings.googlePhotoUrl,
                                        contentDescription = "الصورة الشخصية",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = getAvatarIcon(settings.avatar),
                                        contentDescription = "الصورة الشخصية",
                                        modifier = Modifier.size(32.dp),
                                        tint = if (settings.isDarkMode) Color(0xFF9CA3AF) else Color(0xFF4B5563)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Name and Email
                            Column(
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = settings.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (settings.isDarkMode) Color.White else Color(0xFF111827)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = settings.email,
                                    fontSize = 13.sp,
                                    color = if (settings.isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                                )
                            }
                        }

                        // Chevron Right Arrow on far right
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "تعديل",
                            tint = if (settings.isDarkMode) Color(0xFF9CA3AF) else Color(0xFF9CA3AF)
                        )
                    }
                }
            }

            // 2. Main Settings List (Matches Screenshot rows precisely)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (settings.isDarkMode) Color(0xFF1F2937) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        // Row 1: تعديل الحساب (Edit Account)
                        SettingsRow(
                            icon = Icons.Default.Edit,
                            title = "تعديل الحساب",
                            isDarkMode = settings.isDarkMode,
                            onClick = { showEditDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Divider(color = if (settings.isDarkMode) Color(0xFF374151) else Color(0xFFF3F4F6))

                        // Row 2: تغيير اللغة (Language) -> Shows "العربية" and arrow
                        SettingsRow(
                            icon = Icons.Default.Language,
                            title = "تغيير اللغة",
                            isDarkMode = settings.isDarkMode,
                            onClick = {}
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "العربية",
                                    fontSize = 14.sp,
                                    color = if (settings.isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Divider(color = if (settings.isDarkMode) Color(0xFF374151) else Color(0xFFF3F4F6))

                        // Row 3: الوضع (Mode) -> Shows "فاتح" or "داكن" and a Switch
                        SettingsRow(
                            icon = Icons.Default.WbSunny,
                            title = "الوضع",
                            isDarkMode = settings.isDarkMode,
                            onClick = { viewModel.setThemeMode(!settings.isDarkMode) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = if (settings.isDarkMode) "داكن" else "فاتح",
                                    fontSize = 14.sp,
                                    color = if (settings.isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Switch(
                                    checked = settings.isDarkMode,
                                    onCheckedChange = { viewModel.setThemeMode(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF3B82F6)
                                    )
                                )
                            }
                        }

                        Divider(color = if (settings.isDarkMode) Color(0xFF374151) else Color(0xFFF3F4F6))

                        // Row 4: ألوان التطبيق (App Colors) -> Shows palette and arrow
                        SettingsRow(
                            icon = Icons.Default.Palette,
                            title = "ألوان التطبيق",
                            isDarkMode = settings.isDarkMode,
                            onClick = {
                                // Cycle through available primary colors for a cool interactive UX!
                                val colors = listOf("#3B82F6", "#0D9488", "#7C4DFF", "#E91E63", "#FF8F00")
                                val currentIndex = colors.indexOf(settings.themeColorHex)
                                val nextIndex = (currentIndex + 1) % colors.size
                                viewModel.setThemeColor(colors[nextIndex])
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // AI Settings Card
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (settings.isDarkMode) Color(0xFF1F2937) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إعدادات مفتاح الذكاء الاصطناعي (Gemini)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (settings.isDarkMode) Color.White else Color(0xFF111827)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "أدخل مفتاح Gemini API الخاص بك لتتمكن من التحدث مع المعلم الذكي وتلقي تصحيحات دقيقة لنطقك.",
                            fontSize = 13.sp,
                            color = if (settings.isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = apiKeyText,
                            onValueChange = { apiKeyText = it },
                            label = { Text("مفتاح Gemini API") },
                            placeholder = { Text("AIzaSy...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = if (settings.isDarkMode) Color(0xFF9CA3AF) else Color(0xFF4B5563)
                                )
                            },
                            trailingIcon = {
                                val image = if (apiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                val description = if (apiKeyVisible) "إخفاء المفتاح" else "إظهار المفتاح"
                                IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                                    Icon(imageVector = image, contentDescription = description)
                                }
                            },
                            visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (settings.isDarkMode) Color(0xFF374151) else Color(0xFFE5E7EB)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Microphone permission row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (micPermissionGranted) Color(0xFF10B981).copy(alpha = 0.08f) else Color(0xFFEF4444).copy(alpha = 0.08f))
                                .clickable {
                                    if (!micPermissionGranted) {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (micPermissionGranted) Icons.Default.Mic else Icons.Default.MicOff,
                                    contentDescription = null,
                                    tint = if (micPermissionGranted) Color(0xFF10B981) else Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "صلاحية المايكروفون للـ AI:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (settings.isDarkMode) Color.White else Color(0xFF111827)
                                )
                            }
                            Text(
                                text = if (micPermissionGranted) "مفعلة ✅" else "غير مفعلة ❌ (اضغط للطلب)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (micPermissionGranted) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.setGeminiApiKey(apiKeyText.trim())
                                if (!micPermissionGranted) {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    Toast.makeText(context, "تم حفظ مفتاح API بنجاح وصلاحية المايكروفون مفعلة بالفعل!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("حفظ المفتاح وتفعيل الصلاحيات", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. Standalone Centered red "تسجيل الخروج" Button at the bottom (Matches screenshot precisely)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.logout() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (settings.isDarkMode) Color(0xFF1F2937) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "تسجيل الخروج",
                            color = Color(0xFFEF4444),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // --- Profile Editing Dialog ---
    if (showEditDialog) {
        ProfileEditDialog(
            currentSettings = settings,
            onDismiss = { showEditDialog = false },
            onSave = { name, email, avatar ->
                viewModel.updateProfile(name, email, avatar)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun getAvatarIcon(avatar: String): ImageVector {
    return when (avatar) {
        "avatar_1" -> Icons.Default.Face
        "avatar_2" -> Icons.Default.AccountBox
        "avatar_3" -> Icons.Default.AccountCircle
        "avatar_4" -> Icons.Default.Star
        "avatar_5" -> Icons.Default.Person
        else -> Icons.Default.Face
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditDialog(
    currentSettings: UserSettingsEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, email: String, avatar: String) -> Unit
) {
    var name by remember { mutableStateOf(currentSettings.name) }
    var email by remember { mutableStateOf(currentSettings.email) }
    var selectedAvatar by remember { mutableStateOf(currentSettings.avatar) }

    val avatars = listOf("avatar_1", "avatar_2", "avatar_3", "avatar_4", "avatar_5")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "تعديل بيانات الحساب",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Avatar Grid selector
                Text(
                    "اختر صورتك الرمزية",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    avatars.forEach { avatarId ->
                        val isSelected = selectedAvatar == avatarId
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedAvatar = avatarId },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getAvatarIcon(avatarId),
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم الكامل") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء", color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = { onSave(name, email, selectedAvatar) },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("حفظ التغييرات")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    trailingContent: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF4B5563),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkMode) Color(0xFFE5E7EB) else Color(0xFF1F2937)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            content = trailingContent
        )
    }
}
