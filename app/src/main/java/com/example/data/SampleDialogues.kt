package com.example.data

data class DialogueTurn(
    val role: String, // "AI" or "USER"
    val text: String, // English phrase to be read or listened to
    val translation: String, // Arabic translation
    val pronunciationTip: String = "" // Phonetic training tips
)

data class Dialogue(
    val id: String,
    val title: String,
    val level: String, // "سهل" | "متوسط" | "متقدم"
    val category: String,
    val turnsCount: Int,
    val durationText: String,
    val turns: List<DialogueTurn>
)

object SampleDialogues {
    val dialogues = listOf(
        Dialogue(
            id = "ordering_coffee",
            title = "طلب قهوة في المقهى",
            level = "سهل",
            category = "الحياة اليومية",
            turnsCount = 8,
            durationText = "3 دقائق",
            turns = listOf(
                DialogueTurn(
                    role = "AI",
                    text = "Welcome to Coffee Spot! What can I get started for you today?",
                    translation = "أهلاً بك في كوفي سبوت! بماذا يمكنني البدء لك اليوم؟",
                    pronunciationTip = "انتبه لنطق حرف t في كلمة started وحرف o في كلمة Spot."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "Hello! I would like to order a medium hot latte, please.",
                    translation = "مرحباً! أود طلب لاتيه ساخن متوسط الحجم، من فضلك.",
                    pronunciationTip = "انطق كلمة latte كـ 'لا-تيه' برقة، واحرص على نطق please بـ s خفيفة مسموعة كـ z."
                ),
                DialogueTurn(
                    role = "AI",
                    text = "Sure thing! Would you like that with whole milk, oat milk, or almond milk?",
                    translation = "بالتأكيد! هل تود ذلك بحليب كامل الدسم، حليب الشوفان، أم حليب اللوز؟",
                    pronunciationTip = "حرف l صامت في كلمة almond وتنطق 'آ-موند'."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "I prefer oat milk, please, and not too sweet.",
                    translation = "أفضل حليب الشوفان، من فضلك، وليس حلوًا جدًا.",
                    pronunciationTip = "انطق كلمة oat بمد حرف o، واجعل صوت t واضحاً في نهاية sweet."
                ),
                DialogueTurn(
                    role = "AI",
                    text = "You got it. That will be a medium hot oat milk latte. Anything else to eat?",
                    translation = "فهمتك. سيكون ذلك لاتيه ساخن متوسط بحليب الشوفان. أي شيء آخر للأكل؟",
                    pronunciationTip = "كلمة Anything تبدأ بـ th تنطق كالثاء العربية."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "No, thank you. That is all for today.",
                    translation = "لا، شكراً لك. هذا كل شيء لليوم.",
                    pronunciationTip = "أخرج لسانك قليلاً عند نطق th في thank وفي That."
                ),
                DialogueTurn(
                    role = "AI",
                    text = "Great! That comes to four fifty. Please tap your card here.",
                    translation = "رائع! الحساب هو أربعة وخمسون سنتاً. يرجى تمرير بطاقتك هنا.",
                    pronunciationTip = "كلمة tap تنطق بفتح التاء 'تاب' وليس 'تيب'."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "Okay, here is my card. Thank you very much!",
                    translation = "حسناً، ها هي بطاقتي. شكراً جزيلاً لك!",
                    pronunciationTip = "انطق كلمة very بضم خفيف للشفاه بدون تلامس الأسنان مع الشفة السفلى بقوة."
                )
            )
        ),
        Dialogue(
            id = "hotel_checkin",
            title = "تسجيل الوصول في الفندق",
            level = "متوسط",
            category = "السفر والسياحة",
            turnsCount = 8,
            durationText = "4 دقائق",
            turns = listOf(
                DialogueTurn(
                    role = "AI",
                    text = "Good afternoon! Welcome to the Grand Plaza Hotel. How may I assist you?",
                    translation = "مساء الخير! مرحباً بك في فندق جراند بلازا. كيف يمكنني مساعدتك؟",
                    pronunciationTip = "كلمة assist تنطق بـ s خفيفة 'أ-سِست'."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "Hi! I have a reservation for a double room under the name of Adam.",
                    translation = "مرحباً! لدي حجز لغرفة مزدوجة باسم آدم.",
                    pronunciationTip = "انطق reservation كـ 'ري-زر-في-شن' بتركيز على الـ z والمقطع الأخير شن."
                ),
                DialogueTurn(
                    role = "AI",
                    text = "Ah, yes, Mr. Adam. I found your booking for three nights. May I see your ID, please?",
                    translation = "آه، نعم، سيد آدم. لقد وجدت حجزك لثلاث ليالٍ. هل يمكنني رؤية هويتك، من فضلك؟",
                    pronunciationTip = "حرف g-h صامتان في كلمة nights وتنطق 'نايتس'."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "Sure, here is my passport and my credit card.",
                    translation = "بالتأكيد، ها هو جواز سفري وبطاقتي الائتمانية.",
                    pronunciationTip = "انطق كلمة credit كـ 'كرِ-دِت' بكسر خفيف."
                ),
                DialogueTurn(
                    role = "AI",
                    text = "Excellent. You are all set! Your room number is 405 on the fourth floor. Here is your keycard.",
                    translation = "ممتاز. كل شيء جاهز! رقم غرفتك هو 405 في الطابق الرابع. ها هي بطاقة مفتاحك.",
                    pronunciationTip = "كلمة excellent تنطق 'إك-سيل-أنت'."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "Thank you! What time is breakfast served in the morning?",
                    translation = "شكراً لك! في أي وقت يتم تقديم وجبة الإفطار في الصباح؟",
                    pronunciationTip = "انطق breakfast كـ 'برِك-فاست' وليس 'بريك-فاست'."
                ),
                DialogueTurn(
                    role = "AI",
                    text = "Breakfast is served from seven to ten AM in our main dining hall on the first floor.",
                    translation = "يتم تقديم وجبة الإفطار من السابعة إلى العاشرة صباحاً في صالة الطعام الرئيسية في الطابق الأول.",
                    pronunciationTip = "كلمة dining تنطق 'داي-ننج'."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "Perfect. And is there a password for the hotel Wi-Fi?",
                    translation = "ممتاز. وهل هناك كلمة مرور لشبكة الواي فاي بالفندق؟",
                    pronunciationTip = "احرص على إخراج هواء خفيف عند نطق p في password."
                )
            )
        ),
        Dialogue(
            id = "job_interview",
            title = "مقابلة عمل احترافية",
            level = "متقدم",
            category = "الأعمال والمهن",
            turnsCount = 8,
            durationText = "5 دقائق",
            turns = listOf(
                DialogueTurn(
                    role = "AI",
                    text = "Good morning! Thank you for coming in today. To start, could you please tell me a bit about yourself?",
                    translation = "صباح الخير! شكراً لقدومك اليوم. للبدء، هل يمكنك إخباري بقليل عن نفسك؟",
                    pronunciationTip = "الـ l صامت في كلمة could وتنطق 'كُد'."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "Good morning. I am a software engineer with three years of experience building mobile applications.",
                    translation = "صباح الخير. أنا مهندس برمجيات لدي ثلاث سنوات من الخبرة في بناء تطبيقات الهاتف المحمول.",
                    pronunciationTip = "انطق software كـ 'سوف-ت-وير' واحرص على نطق th في three كالثاء."
                ),
                DialogueTurn(
                    role = "AI",
                    text = "That sounds impressive. What would you say is your greatest professional strength?",
                    translation = "هذا يبدو رائعاً. ما الذي تقول أنه أكبر نقطة قوة مهنية لديك؟",
                    pronunciationTip = "انطق strength بـ th واضحة في نهايتها 'سترينغث'."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "My greatest strength is problem solving and my ability to learn new technologies quickly.",
                    translation = "أكبر نقطة قوة لدي هي حل المشكلات وقدرتي على تعلم التقنيات الجديدة بسرعة.",
                    pronunciationTip = "كلمة technologies تنطق 'تِك-نو-لو-جيز' بلفظ ch كالكاف."
                ),
                DialogueTurn(
                    role = "AI",
                    text = "Excellent. And why do you want to work for our company?",
                    translation = "ممتاز. ولماذا تريد العمل في شركتنا؟",
                    pronunciationTip = "انطق كلمة company كـ 'كام-بني' وليس 'كوم-باني'."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "I admire your company's innovative products and your strong commitment to quality engineering.",
                    translation = "أنا معجب بمنتجات شركتكم المبتكرة والتزامكم القوي بالهندسة عالية الجودة.",
                    pronunciationTip = "انطق innovative كـ 'إِن-أو-في-تِف' بكسر النون والـ v."
                ),
                DialogueTurn(
                    role = "AI",
                    text = "Thank you. Finally, where do you see yourself in five years?",
                    translation = "شكراً لك. أخيراً، أين ترى نفسك بعد خمس سنوات؟",
                    pronunciationTip = "انطق Finally بـ 'فاي-نلي'."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "In five years, I hope to lead a team of talented developers and drive impactful software projects.",
                    translation = "بعد خمس سنوات، آمل أن أقود فريقاً من المطورين الموهوبين وأوجه مشاريع برمجية مؤثرة.",
                    pronunciationTip = "انطق developers كـ 'دي-فِل-أ-برز' والـ projects بـ p مسموعة بوضوح."
                )
            )
        ),
        Dialogue(
            id = "airport_help",
            title = "طلب مساعدة في المطار",
            level = "سهل",
            category = "السفر والسياحة",
            turnsCount = 8,
            durationText = "3 دقائق",
            turns = listOf(
                DialogueTurn(
                    role = "AI",
                    text = "Excuse me, sir! You look lost. Can I help you find your terminal?",
                    translation = "معذرة يا سيدي! تبدو تائهاً. هل يمكنني مساعدتك في العثور على صالة السفر الخاصة بك؟",
                    pronunciationTip = "كلمة terminal تنطق 'تِر-مي-نال'."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "Yes, please! I am looking for terminal three. My flight to London departs soon.",
                    translation = "نعم، من فضلك! أنا أبحث عن الصالة رقم ثلاثة. رحلتي إلى لندن تغادر قريباً.",
                    pronunciationTip = "أخرج الهواء عند نطق p في departs."
                ),
                DialogueTurn(
                    role = "AI",
                    text = "Ah, you are in terminal one. Terminal three is in the adjacent building. You can take the shuttle train.",
                    translation = "آه، أنت في الصالة رقم واحد. الصالة رقم ثلاثة في المبنى المجاور. يمكنك ركوب قطار النقل السريع.",
                    pronunciationTip = "حرف d في adjacent صامت خفيف وتنطق 'أ-جي-سنت'."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "Where can I find the shuttle train station?",
                    translation = "أين يمكنني العثور على محطة قطار النقل السريع؟",
                    pronunciationTip = "انطق shuttle بـ 'شا-تل' مع تضخيم خفيف لحرف l."
                ),
                DialogueTurn(
                    role = "AI",
                    text = "Just go up the escalator behind you and follow the signs for 'Airport Train'. It is very easy to find.",
                    translation = "فقط اصعد السلم الكهربائي خلفك واتبع لافتات 'قطار المطار'. من السهل جداً العثور عليه.",
                    pronunciationTip = "انطق escalator كـ 'إس-كالي-تر'."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "Is the train free of charge, or do I need to buy a ticket?",
                    translation = "هل القطار مجاني، أم أحتاج لشراء تذكرة؟",
                    pronunciationTip = "كلمة charge تنطق بـ 'تشارج' بنطق الـ ch بوضوح."
                ),
                DialogueTurn(
                    role = "AI",
                    text = "It is completely free! It runs every five minutes and takes only three minutes to get there.",
                    translation = "إنه مجاني تماماً! يعمل كل خمس دقائق ويستغرق ثلاث دقائق فقط للوصول إلى هناك.",
                    pronunciationTip = "كلمة runs تنهي بصوت z خفيف."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "Thank you so much! You saved my day.",
                    translation = "شكراً جزيلاً لك! لقد أنقذت يومي.",
                    pronunciationTip = "انطق saved كـ 'سِيفد' برقة."
                )
            )
        ),
        Dialogue(
            id = "pharmacy_visit",
            title = "في الصيدلية لشراء دواء",
            level = "متوسط",
            category = "الحياة اليومية",
            turnsCount = 8,
            durationText = "4 دقائق",
            turns = listOf(
                DialogueTurn(
                    role = "AI",
                    text = "Hello! How can I help you today?",
                    translation = "مرحباً! كيف يمكنني مساعدتك اليوم؟",
                    pronunciationTip = "نطق الحرف h كصوت هاء صافٍ ومسموع."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "Hello. I have a sore throat and a bad headache. What do you recommend?",
                    translation = "مرحباً. لدي التهاب في الحلق وصداع شديد. بمَ توصيني؟",
                    pronunciationTip = "في كلمة headache الـ ch تنطق كالكاف 'هِد-إِيك'."
                ),
                DialogueTurn(
                    role = "AI",
                    text = "For a sore throat, these lozenges are very effective. And for the headache, you can take paracetamol.",
                    translation = "لالتهاب الحلق، هذه الأقراص المحلاة فعالة جداً. وللصداع، يمكنك تناول الباراسيتامول.",
                    pronunciationTip = "انطق lozenges كـ 'لو-زِن-جيز' و paracetamol كـ 'بارا-سيتامول'."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "Should I take this medicine before or after meals?",
                    translation = "هل يجب أن أتناول هذا الدواء قبل أم بعد الوجبات؟",
                    pronunciationTip = "انتبه لصوت sh في should وتنطق 'شُد' والـ l صامت."
                ),
                DialogueTurn(
                    role = "AI",
                    text = "Take the paracetamol after a light meal with plenty of water. The lozenges can be taken anytime.",
                    translation = "تناول الباراسيتامول بعد وجبة خفيفة مع الكثير من الماء. الأقراص المحلاة يمكن تناولها في أي وقت.",
                    pronunciationTip = "احرص على إخراج هواء مع حرف p في plenty."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "Are there any side effects that I should know about?",
                    translation = "هل هناك أي آثار جانبية يجب أن أعرفها؟",
                    pronunciationTip = "انطق effects بـ 'إ-فِكتس' بوضوح."
                ),
                DialogueTurn(
                    role = "AI",
                    text = "The paracetamol might make you feel a little drowsy, so avoid driving right after taking it.",
                    translation = "الباراسيتامول قد يجعلك تشعر بالنعاس قليلاً، لذا تجنب القيادة مباشرة بعد تناوله.",
                    pronunciationTip = "كلمة drowsy تنطق 'دراو-زي' بصوت z."
                ),
                DialogueTurn(
                    role = "USER",
                    text = "Understood. I will take them and rest. Thank you so much!",
                    translation = "مفهوم. سآخذهم وأرتاح. شكراً جزيلاً لك!",
                    pronunciationTip = "احرص على نطق th في them بصوت الذال."
                )
            )
        )
    )
}
