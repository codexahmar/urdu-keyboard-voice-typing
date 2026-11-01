package com.codexahmar.urdu_keyboard.keyboard

import java.util.Locale

/**
 * Fast in-memory emoji search dictionary with comprehensive English and Urdu keyword mappings.
 * Supports prefix matching, fuzzy keyword lookup, and popular emoji fallback.
 */
object EmojiDictionary {

    val popularEmojis = listOf(
        "😂", "❤️", "😊", "👍", "🔥", "😭", "🙏", "😍", "🥰", "✨",
        "🎉", "👏", "🤲", "🇵🇰", "🤣", "💖", "🌹", "☕", "💯", "😎",
        "🥺", "🤔", "💪", "🎂", "🚗", "👌", "😘", "💔", "🌟", "😴"
    )

    private val emojiMap: List<Pair<List<String>, List<String>>> = listOf(
        // ── Smileys & Emotions ───────────────────────────────────────────────
        listOf("smile", "happy", "grin", "joy", "خوش", "ہنسنا", "مسکراہٹ") to
            listOf("😊", "😄", "😃", "😁", "😆", "☺️", "🙂", "😌", "🤗"),
        listOf("laugh", "lol", "haha", "rofl", "hilarious", "قہقہہ", "مذاق", "ہنسی") to
            listOf("😂", "🤣", "😹", "😆", "😅"),
        listOf("love", "heart", "romance", "crush", "پیار", "محبت", "دل", "عشق") to
            listOf("❤️", "💖", "💕", "💗", "💓", "💞", "💘", "💝", "😍", "🥰", "😻", "💌", "❣️", "💟"),
        listOf("kiss", "pappi", "بوسہ", "پیار") to
            listOf("😘", "😚", "😙", "😗", "💋"),
        listOf("cry", "sad", "tear", "weep", "sob", "غم", "رونا", "اداس", "افسوس") to
            listOf("😢", "😭", "😥", "😿", "🥺", "😔", "😞", "😓", "💔"),
        listOf("angry", "mad", "rage", "furious", "غصہ", "ناراض", "خفا") to
            listOf("😡", "😠", "🤬", "👿", "😤", "💢"),
        listOf("cool", "sunglasses", "swag", "attitude", "زبردست", "کمال") to
            listOf("😎", "🕶️", "🤙", "🆒"),
        listOf("thinking", "hmm", "think", "ponder", "سوچ", "خیال") to
            listOf("🤔", "🧐"),
        listOf("wink", "naughty", "شرارت", "مستی") to
            listOf("😉", "😜", "🤪", "😋", "😝", "😛"),
        listOf("sleep", "tired", "yawn", "bed", "نیند", "تھک", "سونا") to
            listOf("😴", "🥱", "💤", "🤤", "🛌"),
        listOf("shock", "surprise", "omg", "wow", "حیران", "تعجب") to
            listOf("😲", "😮", "😱", "🤯", "😳", "😯"),
        listOf("sick", "ill", "fever", "cold", "بیمار", "صحت") to
            listOf("🤒", "😷", "🤕", "🤢", "🤮", "🤧"),
        listOf("angel", "halo", "innocent", "معصوم", "فرشتہ") to
            listOf("😇", "👼"),
        listOf("fire", "lit", "hot", "flame", "آگ", "گرم", "شعلہ") to
            listOf("🔥", "💥", "🌶️", "🚒"),
        listOf("star", "sparkle", "shine", "glow", "ستارہ", "تارہ", "چمک") to
            listOf("⭐", "🌟", "✨", "💫", "🤩"),
        listOf("100", "hundred", "perfect", "score", "نمبر") to
            listOf("💯", "🎯", "✔️", "✅"),

        // ── Hands & Gestures ─────────────────────────────────────────────────
        listOf("thumb", "thumbsup", "ok", "yes", "agree", "ٹھیک", "ہاں", "شاباش") to
            listOf("👍", "👎", "👌", "✌️", "🤝", "✅"),
        listOf("clap", "applause", "bravo", "تالی") to
            listOf("👏", "🙌"),
        listOf("pray", "dua", "namaz", "bless", "ameen", "دعا", "نماز", "شکر", "آمین") to
            listOf("🙏", "🤲", "🕌", "🧎", "🧎‍♂️", "🕋", "📿"),
        listOf("wave", "bye", "hi", "hello", "سلام", "الوداع") to
            listOf("👋", "🙋", "🙋‍♂️", "🖐️"),
        listOf("muscle", "strong", "bicep", "gym", "fit", "طاقت", "مضبوط") to
            listOf("💪", "🏋️", "🥊"),
        listOf("point", "finger", "hand", "اشارہ", "ہاتھ") to
            listOf("👆", "👇", "👈", "👉", "☝️", "✋"),

        // ── Animals & Nature ─────────────────────────────────────────────────
        listOf("cat", "kitten", "kitty", "بلی") to
            listOf("🐱", "🐈", "😻", "😹", "😸", "🐾"),
        listOf("dog", "puppy", "کتا") to
            listOf("🐶", "🐕", "🐩", "🦮", "🐾"),
        listOf("bird", "sparrow", "eagle", "چڑیا", "پرندہ") to
            listOf("🐦", "🦅", "🦆", "🦉", "🕊️", "🦜", "🐥"),
        listOf("flower", "rose", "blossom", "tulip", "پھول", "گلاب") to
            listOf("🌹", "🌸", "🌺", "🌷", "💐", "🌻", "🌼"),
        listOf("sun", "sunny", "day", "سورج", "دھوپ", "دن") to
            listOf("☀️", "🌞", "🌅", "🌤️"),
        listOf("moon", "night", "crescent", "چاند", "رات") to
            listOf("🌙", "🌕", "🌛", "🌜", "🌃"),
        listOf("rain", "cloud", "weather", "بارش", "بادل", "موسم") to
            listOf("🌧️", "🌧", "🌦️", "☁️", "⚡", "⛈️"),
        listOf("tree", "plant", "garden", "درخت", "پودا", "باغ") to
            listOf("🌳", "🌲", "🌴", "🌿", "🍀", "🌱"),

        // ── Food & Drink ─────────────────────────────────────────────────────
        listOf("food", "eat", "hungry", "lunch", "dinner", "کھانا") to
            listOf("🍕", "🍔", "🍟", "🥪", "🌮", "🍲", "🍛", "🍗", "🍖"),
        listOf("tea", "chai", "coffee", "cup", "چائے", "کافی") to
            listOf("☕", "🍵", "🧋", "🫖"),
        listOf("water", "drink", "juice", "پانی", "شربت") to
            listOf("💧", "🥤", "🧃", "🥛"),
        listOf("fruit", "apple", "mango", "banana", "پھل", "سیب", "آم", "کیلا") to
            listOf("🍎", "🍏", "🥭", "🍌", "🍉", "🍇", "🍓", "🍊"),
        listOf("cake", "sweet", "chocolate", "mithai", "کیک", "مٹھائی", "چاکلیٹ") to
            listOf("🎂", "🍰", "🧁", "🍫", "🍬", "🍭", "🍩", "🍨", "🍦"),

        // ── Objects, Places, Flags & Celebrations ────────────────────────────
        listOf("car", "drive", "ride", "auto", "گاڑی") to
            listOf("🚗", "🚘", "🚙", "🏎️", "🏎", "🚓", "🚕", "🚌", "🚛"),
        listOf("bike", "motorcycle", "cycle", "موٹرسائیکل", "سائیکل") to
            listOf("🏍️", "🛵", "🚲"),
        listOf("airplane", "plane", "flight", "travel", "جہاز", "سفر") to
            listOf("✈️", "🛫", "🛬", "🚀", "🧳"),
        listOf("pakistan", "flag", "qaumi", "پاکستان", "جھنڈا") to
            listOf("🇵🇰", "🏳️", "🏴", "🚩"),
        listOf("party", "celebrate", "eid", "mubarak", "جشن", "عید", "مبارک", "خوشی") to
            listOf("🎉", "🎊", "🎈", "🎁", "🥳", "🎇", "🎆"),
        listOf("money", "cash", "dollar", "rupee", "rich", "روپیہ", "پیسے", "امیر") to
            listOf("💰", "💵", "💸", "💳", "🤑", "🪙"),
        listOf("home", "house", "building", "گھر", "مکان") to
            listOf("🏠", "🏡", "🏢", "🏘️"),
        listOf("book", "study", "read", "school", "کتاب", "پڑھائی", "علم") to
            listOf("📚", "📖", "📕", "📝", "✏️", "🎓"),
        listOf("phone", "call", "mobile", "فون", "رابطہ") to
            listOf("📱", "📞", "☎️", "📲")
    )

    fun search(query: String): List<String> {
        val q = query.lowercase(Locale.ROOT).trim()
        if (q.isEmpty()) return popularEmojis

        val results = LinkedHashSet<String>(30)

        // 1. Exact or prefix match on keywords
        for ((keywords, emojis) in emojiMap) {
            for (keyword in keywords) {
                if (keyword.startsWith(q) || q.startsWith(keyword)) {
                    results.addAll(emojis)
                    break
                }
            }
            if (results.size >= 30) break
        }

        // 2. Substring/contains match
        if (results.size < 20) {
            for ((keywords, emojis) in emojiMap) {
                for (keyword in keywords) {
                    if (keyword.contains(q) || q.contains(keyword)) {
                        results.addAll(emojis)
                        break
                    }
                }
                if (results.size >= 30) break
            }
        }

        return if (results.isEmpty()) popularEmojis else results.take(30)
    }
}

