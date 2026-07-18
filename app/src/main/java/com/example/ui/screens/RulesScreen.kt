package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PronunciationRule(
    val title: String,
    val icon: ImageVector,
    val description: String,
    val examples: List<RuleExample>
)

data class RuleExample(
    val englishWord: String,
    val phonetic: String,
    val translation: String,
    val note: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val rules = remember {
        listOf(
            PronunciationRule(
                title = "الحروف الصامتة (Silent Letters)",
                icon = Icons.Default.VolumeOff,
                description = "قاعدة الحروف التي تُكتب ولا تُنطق في اللغة الإنجليزية، وهي شائعة جداً وتسبب أخطاء بالنطق.",
                examples = listOf(
                    RuleExample("Knife", "/naɪf/", "سكين", "حرف K صامت إذا جاء في بداية الكلمة قبل حرف N."),
                    RuleExample("Walk", "/wɔːk/", "يمشي", "حرف L صامت غالباً في كلمات مثل walk, talk, should, could."),
                    RuleExample("Doubt", "/daʊt/", "شك", "حرف B صامت عندما يأتي بعد حرف M أو قبل حرف T."),
                    RuleExample("Write", "/raɪt/", "يكتب", "حرف W صامت إذا جاء في بداية الكلمة متبوعاً بحرف R.")
                )
            ),
            PronunciationRule(
                title = "أصوات الحرفين C و G (Hard & Soft Sounds)",
                icon = Icons.Default.GTranslate,
                description = "تغيير نطق الحرفين C و G بناءً على الحروف التي تأتي بعدهما.",
                examples = listOf(
                    RuleExample("City", "/ˈsɪti/", "مدينة", "يُنطق حرف C كـ 'S' (ناعم) إذا جاء بعده E أو I أو Y."),
                    RuleExample("Cat", "/kæt/", "قطة", "يُنطق حرف C كـ 'K' (قاسٍ) إذا جاء بعده أي حرف آخر."),
                    RuleExample("Gentle", "/ˈdʒentl/", "لطيف", "يُنطق حرف G كـ 'J' (ناعم) إذا جاء بعده E أو I أو Y."),
                    RuleExample("Goat", "/ɡəʊt/", "ماعز", "يُنطق حرف G كـ 'G' مصرية (قاسٍ) مع بقية الحروف.")
                )
            ),
            PronunciationRule(
                title = "نطق الـ Th (Voiced & Voiceless)",
                icon = Icons.Default.RecordVoiceOver,
                description = "يُنطق المقطع Th بطريقتين مختلفتين تماماً: إما كالثاء العربية (Voiceless) أو كالذال العربية (Voiced).",
                examples = listOf(
                    RuleExample("Three", "/θriː/", "ثلاثة", "نطق voiceless كحرف الثاء باللغة العربية مع إخراج الهواء."),
                    RuleExample("This", "/ðɪs/", "هذا", "نطق voiced كحرف الذال باللغة العربية."),
                    RuleExample("Think", "/θɪŋk/", "يفكر", "نطق كحرف الثاء."),
                    RuleExample("Mother", "/ˈmʌðə/", "أم", "نطق كحرف الذال.")
                )
            ),
            PronunciationRule(
                title = "الأخطاء الشائعة للمتحدثين بالعربية",
                icon = Icons.Default.Warning,
                description = "أشهر الكلمات والتركيبات الصوتية التي يخطئ فيها الناطقون بالعربية وكيفية نطقها بالشكل الصحيح.",
                examples = listOf(
                    RuleExample("People", "/ˈpiːpl/", "ناس", "الخطأ بنطق حرف P كـ B. يجب دفع هواء خفيف من الشفاه عند نطق P."),
                    RuleExample("Ask", "/ɑːsk/", "يسأل", "الخطأ بنطقها aks بسبب التبادل الحرفي. انطق الـ S أولاً ثم K."),
                    RuleExample("Comfortable", "/ˈkʌmftəbl/", "مريح", "الخطأ بنطقها بالتفصيل 'كوم-فور-تي-بل'. النطق الصحيح كـ 'كامف-تـابل'."),
                    RuleExample("Vegetable", "/ˈvegtable/", "خضار", "الخطأ بنطقها 'فيجي-تيبل'. النطق الصحيح هو 'فِج-تابل' بدمج المقطعين.")
                )
            ),
            PronunciationRule(
                title = "نطق الحرف R في اللهجة الأمريكية والبريطانية",
                icon = Icons.Default.Translate,
                description = "الفرق بين اللهجة الأمريكية والبريطانية يكمن أساساً في نطق حرف R في نهاية الكلمات.",
                examples = listOf(
                    RuleExample("Car (US)", "/kɑːr/", "سيارة (أمريكية)", "في اللهجة الأمريكية يُنطق حرف R بوضوح في نهاية الكلمات."),
                    RuleExample("Car (UK)", "/kɑː/", "سيارة (بريطانية)", "في اللهجة البريطانية يكون حرف R في نهاية الكلمات صامتاً تماماً وتمد حركة الفتحة قبله.")
                )
            )
        )
    }

    val filteredRules = rules.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "القواعد",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Search field (Highly polished)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن قاعدة أو كلمة...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = Color(0xFF9CA3AF)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (filteredRules.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لا توجد قواعد تطابق بحثك.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp, top = 4.dp)
                ) {
                    items(filteredRules) { rule ->
                        RuleCard(rule = rule)
                    }
                }
            }
        }
    }
}

fun getRuleColorInfo(title: String): Pair<ImageVector, Color> {
    return when {
        title.contains("صامتة") -> Pair(Icons.Default.VolumeOff, Color(0xFFEC4899))
        title.contains("الحرفين") || title.contains("C و G") -> Pair(Icons.Default.GTranslate, Color(0xFF3B82F6))
        title.contains("Th") -> Pair(Icons.Default.RecordVoiceOver, Color(0xFF8B5CF6))
        title.contains("العربية") -> Pair(Icons.Default.Warning, Color(0xFF10B981))
        title.contains("R") -> Pair(Icons.Default.Translate, Color(0xFFF59E0B))
        else -> Pair(Icons.Default.Star, Color(0xFF3B82F6))
    }
}

@Composable
fun RuleCard(rule: PronunciationRule) {
    var isExpanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrowRotation")
    val (icon, tintColor) = getRuleColorInfo(rule.title)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Content: Colored Circular Badge & Text info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.weight(1f)
                ) {
                    // Circular Colored Icon Badge
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(tintColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = rule.title,
                            tint = tintColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title and short details
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = rule.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Left
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rule.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 1,
                            textAlign = TextAlign.Left
                        )
                    }
                }

                // Far Right: Chevron Expand Arrow
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "تقليص" else "توسيع",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(arrowRotation),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            // Expanded body text
            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = rule.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right,
                    lineHeight = 20.sp
                )
            }

            // Animated Expandable Examples List
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    Text(
                        "أمثلة توضيحية:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = tintColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Right
                    )

                    rule.examples.forEach { example ->
                        ExampleItemRow(example = example, accentColor = tintColor)
                    }
                }
            }
        }
    }
}

@Composable
fun ExampleItemRow(example: RuleExample, accentColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Arabic translation
                Text(
                    text = "=  ${example.translation}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                // English Word & Phonetics
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = example.phonetic,
                        fontSize = 12.sp,
                        color = accentColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = example.englishWord,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Helpful note explanation
            Text(
                text = example.note,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }
    }
}
