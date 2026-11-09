package com.example.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.ui.AppTab
import java.util.Locale

object AppLocalization {
    
    fun getPrayerName(key: String, lang: String): String {
        val isAr = lang != "en"
        return when (key.lowercase()) {
            "fajr" -> if (isAr) "الفجر" else "Fajr"
            "sunrise" -> if (isAr) "الشروق" else "Sunrise"
            "dhuhr" -> if (isAr) "الظهر" else "Dhuhr"
            "asr" -> if (isAr) "العصر" else "Asr"
            "maghrib" -> if (isAr) "المغرب" else "Maghrib"
            "isha" -> if (isAr) "العشاء" else "Isha"
            else -> key
        }
    }

    fun getTabName(tab: AppTab, lang: String): String {
        val isAr = lang != "en"
        return when (tab) {
            AppTab.PRAYER_CLOCK -> if (isAr) "المواقيت" else "Times"
            AppTab.PRAYER_LIST -> if (isAr) "القائمة" else "List"
            AppTab.QURAN -> if (isAr) "المصحف" else "Quran"
            AppTab.QIBLA -> if (isAr) "القبلة" else "Qibla"
            AppTab.HOLIDAYS -> if (isAr) "الرزنامة" else "Calendar"
        }
    }

    fun getString(key: String, lang: String): String {
        val isAr = lang != "en"
        return when (key) {
            "islamic_holidays" -> if (isAr) "الأعياد والمناسبات" else "Islamic Holidays"
            "hijri_year_occasions" -> if (isAr) "مناسبات العام الهجري" else "Hijri year occasions"
            "hijri_new_year" -> if (isAr) "رأس السنة الهجرية" else "Hijri New Year"
            "hijri_new_year_desc" -> if (isAr) "بداية العام الهجري الجديد وذكرى هجرة المصطفى صلى الله عليه وسلم" else "The beginning of the new Hijri year and the Prophet's migration"
            "hijri_new_year_sig" -> if (isAr) "عطلة دينية ومناسبة مباركة" else "Religious holiday and blessed occasion"
            "ashura" -> if (isAr) "يوم عاشوراء" else "Day of Ashura"
            "ashura_desc" -> if (isAr) "اليوم الذي نجى الله فيه موسى عليه السلام وقومه من فرعون، ويُستحب صيامه" else "The day God saved Moses (peace be upon him) and his people from Pharaoh, and fasting is recommended"
            "ashura_sig" -> if (isAr) "صيام مستحب وتكفير لذنوب سنة ماضية" else "Recommended fasting and expiation for past sins"
            "mawlid" -> if (isAr) "المولد النبوي الشريف" else "Prophet's Birthday"
            "mawlid_desc" -> if (isAr) "ذكرى مولد خير البرية نبينا محمد صلى الله عليه وسلم" else "Commemoration of the birth of the best of mankind, our Prophet Muhammad"
            "mawlid_sig" -> if (isAr) "احتفال ومولد الهادي البشير" else "Celebration and birth of the Guide"
            "isra_miraj" -> if (isAr) "ذكرى الإسراء والمعراج" else "Isra and Mi'raj"
            "isra_miraj_desc" -> if (isAr) "معجزة الإسراء من المسجد الحرام إلى المسجد الأقصى والعروج إلى السماوات العلى" else "The miracle of the Night Journey from the Sacred Mosque in Makkah to Al-Aqsa Mosque, and the Ascension to the highest heavens"
            "isra_miraj_sig" -> if (isAr) "فرض الصلوات الخمس ومناسبة إيمانية عظيمة" else "Prescription of the five daily prayers and a great occasion of faith"
            "nisf_shaban" -> if (isAr) "ليلة النصف من شعبان" else "Mid-Sha'ban Night"
            "nisf_shaban_desc" -> if (isAr) "ليلة مباركة تحول فيها القبلة وتُرفع فيها الأعمال إلى الله تعالى" else "A blessed night on which the Qiblah was changed and deeds are raised to Allah the Almighty"
            "nisf_shaban_sig" -> if (isAr) "استعداد روحي لشهر رمضان المبارك" else "Spiritual preparation for the blessed month of Ramadan"
            "ramadan_start" -> if (isAr) "أول أيام شهر رمضان المبارك" else "Start of Ramadan"
            "ramadan_start_desc" -> if (isAr) "شهر الصيام والقرآن والرحمة والمغفرة والعتق من النيران" else "The month of fasting, the Qur'an, mercy, forgiveness, and deliverance from the Fire"
            "ramadan_start_sig" -> if (isAr) "ركن الصيام الفريضة" else "The obligatory pillar of fasting"
            "laylat_qadr" -> if (isAr) "ليلة القدر المباركة (الوترية)" else "Night of Power (Laylat al-Qadr)"
            "laylat_qadr_desc" -> if (isAr) "خير من ألف شهر، تنزل فيها الملائكة والروح بإذن ربهم من كل أمر" else "Better than a thousand months, on which angels and the Spirit descend by permission of their Lord for every matter"
            "laylat_qadr_sig" -> if (isAr) "أعظم ليالي العام عبادةً ودعاءً" else "The greatest night of the year for worship and supplication"
            "eid_fitr" -> if (isAr) "عيد الفطر المبارك" else "Eid al-Fitr"
            "eid_fitr_desc" -> if (isAr) "فرحة الصائمين بتمام شهر الصيام وصلة الأرحام وإخراج الزكاة" else "The joy of those who fast upon completing the month of fasting, maintaining kinship ties, and giving Zakat"
            "eid_fitr_sig" -> if (isAr) "عيد إسلامي بهيج (3 أيام)" else "Joyful Islamic holiday (3 days)"
            "arafah" -> if (isAr) "يوم عرفة المبارك" else "Day of Arafah"
            "arafah_desc" -> if (isAr) "أعظم أيام الحج وصيامه يكفر سنتين (ماضية ومقبلة) لغير الحاج" else "The greatest day of Hajj, and fasting it expiates two years (past and coming) for non-pilgrims"
            "arafah_sig" -> if (isAr) "ركن الحج الأعظم ويوم المغفرة والعتق" else "The greatest pillar of Hajj and a day of forgiveness"
            "eid_adha" -> if (isAr) "عيد الأضحى المبارك" else "Eid al-Adha"
            "eid_adha_desc" -> if (isAr) "ذكرى فداء نبي الله إسماعيل عليه السلام وشعيرة الأضحية المباركة" else "Commemoration of the redemption of Prophet Ibrahim (peace be upon him) and the blessed sacrifice ritual"
            "eid_adha_sig" -> if (isAr) "عيد الأضحى وأيام التشريق المباركة" else "Eid al-Adha and the blessed days of Tashreeq"
            "past" -> if (isAr) "انقضى" else "Past"
            "next" -> if (isAr) "القادمة 🌟" else "Next 🌟"
            "today" -> if (isAr) "اليوم" else "Today"
            "tomorrow" -> if (isAr) "غداً" else "Tomorrow"
            "in_2_days" -> if (isAr) "بعد يومين" else "In 2 days"
            "days_format" -> if (isAr) "بعد %d يوماً" else "In %d days"
            "past_badge" -> if (isAr) "انقضى ✓" else "Past ✓"
            "time_remaining" -> if (isAr) "المتبقي على صلاة" else "Time remaining for"
            "adhan_in" -> if (isAr) "الأذان بعد" else "Adhan in"
            "clock_now" -> if (isAr) "الساعة الآن" else "Current time"
            "prayer_times" -> if (isAr) "مواقيت الصلاة" else "Prayer Times"
            "qibla_compass" -> if (isAr) "بوصلة القبلة" else "Qibla Compass"
            "islamic_calendar" -> if (isAr) "الأعياد والمناسبات" else "Islamic Calendar"
            "fine_tune" -> if (isAr) "تعديل دقائق المواقيت:" else "Fine Tune (minutes):"
            "next_occasion" -> if (isAr) "القادمة ⏳" else "Next ⏳"
            "passed" -> if (isAr) "مضت ✓" else "Passed ✓"
            "adjust_hijri" -> if (isAr) "تصحيح الهجري" else "Adjust Hijri"
            "fixed_location" -> if (isAr) "موقع مثبت" else "Pinned Location"
            "hafs" -> if (isAr) "حفص عن عاصم" else "Hafs 'an 'Asim"
            "warsh" -> if (isAr) "ورش عن نافع" else "Warsh 'an Nafi'"
            "continue_reading_bookmark" -> if (isAr) "متابعة القراءة المرجعية 🔖" else "Continue Reading 🔖"
            "continue_btn" -> if (isAr) "متابعة ←" else "Continue →"
            "ayah_word" -> if (isAr) "آية" else "Ayah"
            else -> key
        }
    }

    fun getLocalizedCityName(cityName: String, lang: String): String {
        val isAr = lang != "en"
        if (isAr) return cityName
        if (cityName.contains("موقعي الحالي") || cityName.contains("Current Location")) {
            return "My current location"
        }
        val preset = com.example.data.model.ALL_PRESET_CITIES.find {
            it.nameAr == cityName || it.nameEn.equals(cityName, ignoreCase = true)
        }
        return preset?.nameEn ?: cityName
    }

    fun getLocalizedCountryName(countryName: String, lang: String): String {
        val isAr = lang != "en"
        if (isAr) return countryName
        return if (countryName == "الجزائر") "Algeria" else countryName
    }

    val LocalNumeralSystem = staticCompositionLocalOf { "western" }

    /**
     * Converts all digits in a String to Eastern (Arabic-Indic: ٠، ١، ٢...) or Western (Arabic-Ghubari: 0, 1, 2...)
     * deterministically, ensuring 100% uniformity with zero mixing.
     */
    fun formatDigits(text: String, numeralSystem: String): String {
        if (text.isEmpty()) return text
        val isEastern = numeralSystem == "eastern"
        val sb = StringBuilder(text.length)
        for (i in 0 until text.length) {
            val c = text[i]
            if (isEastern) {
                val mapped = when (c) {
                    '0' -> '٠'
                    '1' -> '١'
                    '2' -> '٢'
                    '3' -> '٣'
                    '4' -> '٤'
                    '5' -> '٥'
                    '6' -> '٦'
                    '7' -> '٧'
                    '8' -> '٨'
                    '9' -> '٩'
                    else -> c
                }
                sb.append(mapped)
            } else {
                val mapped = when (c) {
                    '٠' -> '0'
                    '١' -> '1'
                    '٢' -> '2'
                    '٣' -> '3'
                    '٤' -> '4'
                    '٥' -> '5'
                    '٦' -> '6'
                    '٧' -> '7'
                    '٨' -> '8'
                    '٩' -> '9'
                    else -> c
                }
                sb.append(mapped)
            }
        }
        return sb.toString()
    }

    fun formatNumber(number: Number, numeralSystem: String): String {
        return formatDigits(number.toString(), numeralSystem)
    }

    fun getHijriMonthNameAr(month: Int): String {
        return when (month) {
            1 -> "محرم"
            2 -> "صفر"
            3 -> "ربيع الأول"
            4 -> "ربيع الثاني"
            5 -> "جمادى الأولى"
            6 -> "جمادى الآخرة"
            7 -> "رجب"
            8 -> "شعبان"
            9 -> "رمضان"
            10 -> "شوال"
            11 -> "ذو القعدة"
            12 -> "ذو الحجة"
            else -> "محرم"
        }
    }

    fun getHijriMonthNameEn(month: Int): String {
        return when (month) {
            1 -> "Muharram"
            2 -> "Safar"
            3 -> "Rabi' al-Awwal"
            4 -> "Rabi' al-Thani"
            5 -> "Jumada al-Awwal"
            6 -> "Jumada al-Thani"
            7 -> "Rajab"
            8 -> "Sha'ban"
            9 -> "Ramadan"
            10 -> "Shawwal"
            11 -> "Dhu al-Qi'dah"
            12 -> "Dhu al-Hijjah"
            else -> "Muharram"
        }
    }

    fun getHijriMonthNameShortEn(month: Int): String {
        return when (month) {
            1 -> "Muharram"
            2 -> "Safar"
            3 -> "Rabi' I"
            4 -> "Rabi' II"
            5 -> "Jumada I"
            6 -> "Jumada II"
            7 -> "Rajab"
            8 -> "Sha'ban"
            9 -> "Ramadan"
            10 -> "Shawwal"
            11 -> "Dhu al-Qi'dah"
            12 -> "Dhu al-Hijjah"
            else -> "Muharram"
        }
    }

    fun parseHijriMonthNumber(text: String?): Int {
        if (text == null) return 1
        return when {
            text.contains("محرم") || text.contains("Muharram", ignoreCase = true) -> 1
            text.contains("صفر") || text.contains("Safar", ignoreCase = true) -> 2
            text.contains("ربيع الأول") || text.contains("Rabi' al-Awwal", ignoreCase = true) || text.contains("Rabi I", ignoreCase = true) -> 3
            text.contains("ربيع الثاني") || text.contains("ربيع الآخر") || text.contains("Rabi II", ignoreCase = true) || text.contains("Rabi' al-Thani", ignoreCase = true) -> 4
            text.contains("جمادى الأولى") || text.contains("Jumada al-Awwal", ignoreCase = true) || text.contains("Jumada I", ignoreCase = true) -> 5
            text.contains("جمادى الآخرة") || text.contains("جمادى الثانية") || text.contains("Jumada II", ignoreCase = true) || text.contains("Jumada al-Thani", ignoreCase = true) -> 6
            text.contains("رجب") || text.contains("Rajab", ignoreCase = true) -> 7
            text.contains("شعبان") || text.contains("Sha'ban", ignoreCase = true) -> 8
            text.contains("رمضان") || text.contains("Ramadan", ignoreCase = true) -> 9
            text.contains("شوال") || text.contains("Shawwal", ignoreCase = true) -> 10
            text.contains("ذو القعدة") || text.contains("Dhu al-Qi'dah", ignoreCase = true) -> 11
            text.contains("ذو الحجة") || text.contains("Dhu al-Hijjah", ignoreCase = true) -> 12
            else -> 1
        }
    }

    fun parseHijriString(dateStr: String?): Triple<Int, Int, Int>? {
        if (dateStr.isNullOrBlank()) return null
        val cleaned = dateStr.map { ch ->
            when (ch) {
                '٠' -> '0'; '١' -> '1'; '٢' -> '2'; '٣' -> '3'; '٤' -> '4'
                '٥' -> '5'; '٦' -> '6'; '٧' -> '7'; '٨' -> '8'; '٩' -> '9'
                else -> ch
            }
        }.joinToString("")
        val numbers = Regex("\\d+").findAll(cleaned).map { it.value.toInt() }.toList()
        val day = numbers.getOrNull(0) ?: 1
        val year = numbers.getOrNull(1) ?: 1448
        val month = parseHijriMonthNumber(dateStr)
        return Triple(day, month, year)
    }

    fun formatHijriDate(day: Int, month: Int, year: Int, lang: String, short: Boolean = false): String {
        val isAr = lang != "en"
        return if (isAr) {
            "$day ${getHijriMonthNameAr(month)} $year هـ"
        } else {
            val mName = if (short) getHijriMonthNameShortEn(month) else getHijriMonthNameEn(month)
            "$day $mName $year AH"
        }
    }

    fun formatHijriEvent(day: Int, month: Int, lang: String): String {
        val isAr = lang != "en"
        return if (isAr) {
            "$day ${getHijriMonthNameAr(month)}"
        } else {
            "$day ${getHijriMonthNameShortEn(month)}"
        }
    }

    fun getLocalizedHijriDate(dateStr: String?, lang: String, short: Boolean = false): String {
        if (dateStr.isNullOrBlank()) return ""
        val isAr = lang != "en"
        if (isAr) return dateStr
        val parsed = parseHijriString(dateStr)
        if (parsed != null) {
            return formatHijriDate(parsed.first, parsed.second, parsed.third, lang, short)
        }
        return dateStr
    }

    private val surahEnglishNames = arrayOf(
        "",
        "Al-Fatihah", "Al-Baqarah", "Ali 'Imran", "An-Nisa'", "Al-Ma'idah", "Al-An'am", "Al-A'raf", "Al-Anfal", "At-Tawbah", "Yunus",
        "Hud", "Yusuf", "Ar-Ra'd", "Ibrahim", "Al-Hijr", "An-Nahl", "Al-Isra'", "Al-Kahf", "Maryam", "Ta-Ha",
        "Al-Anbiya'", "Al-Hajj", "Al-Mu'minun", "An-Nur", "Al-Furqan", "Ash-Shu'ara'", "An-Naml", "Al-Qasas", "Al-'Ankabut", "Ar-Rum",
        "Luqman", "As-Sajdah", "Al-Ahzab", "Saba'", "Fatir", "Ya-Sin", "As-Saffat", "Sad", "Az-Zumar", "Ghafir",
        "Fussilat", "Ash-Shura", "Az-Zukhruf", "Ad-Dukhan", "Al-Jathiyah", "Al-Ahqaf", "Muhammad", "Al-Fath", "Al-Hujurat", "Qaf",
        "Adh-Dhariyat", "At-Tur", "An-Najm", "Al-Qamar", "Ar-Rahman", "Al-Waqi'ah", "Al-Hadid", "Al-Mujadilah", "Al-Hashr", "Al-Mumtahanah",
        "As-Saff", "Al-Jumu'ah", "Al-Munafiqun", "At-Taghabun", "At-Talaq", "At-Tahrim", "Al-Mulk", "Al-Qalam", "Al-Haqqah", "Al-Ma'arij",
        "Nuh", "Al-Jinn", "Al-Muzzammil", "Al-Muddaththir", "Al-Qiyamah", "Al-Insan", "Al-Mursalat", "An-Naba'", "An-Nazi'at", "'Abasa",
        "At-Takwir", "Al-Infitar", "Al-Mutaffifin", "Al-Inshiqaq", "Al-Buruj", "At-Tariq", "Al-A'la", "Al-Ghashiyah", "Al-Fajr", "Al-Balad",
        "Ash-Shams", "Al-Layl", "Ad-Duha", "Ash-Sharh", "At-Tin", "Al-'Alaq", "Al-Qadr", "Al-Bayyinah", "Az-Zalzalah", "Al-'Adiyat",
        "Al-Qari'ah", "At-Takathur", "Al-'Asr", "Al-Humazah", "Al-Fil", "Quraysh", "Al-Ma'un", "Al-Kawthar", "Al-Kafirun", "An-Nasr",
        "Al-Masad", "Al-Ikhlas", "Al-Falaq", "An-Nas"
    )

    private val surahEnglishMeanings = arrayOf(
        "",
        "The Opening", "The Cow", "The Family of Imran", "The Women", "The Table Spread", "The Cattle", "The Heights", "The Spoils of War", "The Repentance", "Jonah",
        "Hud", "Joseph", "The Thunder", "Abraham", "The Rocky Tract", "The Bee", "The Night Journey", "The Cave", "Mary", "Ta-Ha",
        "The Prophets", "The Pilgrimage", "The Believers", "The Light", "The Criterion", "The Poets", "The Ant", "The Stories", "The Spider", "The Romans",
        "Luqman", "The Prostration", "The Combined Forces", "Sheba", "Originator", "Ya-Sin", "Those Who Set the Ranks", "The Letter Sad", "The Troops", "The Forgiver",
        "Explained in Detail", "The Consultation", "The Ornaments of Gold", "The Smoke", "The Crouching", "The Wind-Curved Sandhills", "Muhammad", "The Victory", "The Rooms", "The Letter Qaf",
        "The Winnowing Winds", "The Mount", "The Star", "The Moon", "The Most Merciful", "The Inevitable", "The Iron", "The Pleading Woman", "The Exile", "She That Is to Be Examined",
        "The Ranks", "Friday", "The Hypocrites", "The Mutual Disillusion", "The Divorce", "The Prohibition", "The Sovereignty", "The Pen", "The Reality", "The Ascending Stairways",
        "Noah", "The Jinn", "The Enshrouded One", "The Cloaked One", "The Resurrection", "The Human", "The Emissaries", "The Tidings", "Those Who Drag Forth", "He Frowned",
        "The Overthrowing", "The Cleaving", "The Defrauding", "The Sundering", "The Mansions of the Stars", "The Nightcomer", "The Most High", "The Overwhelming", "The Dawn", "The City",
        "The Sun", "The Night", "The Morning Hours", "The Relief", "The Fig", "The Clot", "The Power", "The Clear Proof", "The Earthquake", "The Courser",
        "The Calamity", "The Rivalry in World Increase", "The Declining Day", "The Traducer", "The Elephant", "Quraysh", "The Small Kindnesses", "The Abundance", "The Disbelievers", "The Divine Support",
        "The Palm Fiber", "The Sincerity", "The Daybreak", "Mankind"
    )

    fun getSurahEnglishName(number: Int): String {
        return if (number in 1..114) surahEnglishNames[number] else "Surah $number"
    }

    fun getSurahEnglishMeaning(number: Int): String {
        return if (number in 1..114) surahEnglishMeanings[number] else ""
    }

    fun formatTime(timeStr: String, is24Hour: Boolean, lang: String, numeralSystem: String = "western"): String {
        if (timeStr.isBlank()) return timeStr
        val trimmed = timeStr.take(5)
        val formatted = if (is24Hour) {
            trimmed
        } else {
            try {
                val parts = trimmed.split(":")
                if (parts.size < 2) trimmed
                else {
                    val hour = parts[0].toIntOrNull() ?: return trimmed
                    val minute = parts[1]
                    val isPm = hour >= 12
                    val hour12 = when {
                        hour == 0 -> 12
                        hour > 12 -> hour - 12
                        else -> hour
                    }
                    val suffix = if (lang == "en") {
                        if (isPm) "PM" else "AM"
                    } else {
                        if (isPm) "م" else "ص"
                    }
                    String.format(Locale.US, "%02d:%s %s", hour12, minute, suffix)
                }
            } catch (_: Exception) {
                trimmed
            }
        }
        return formatDigits(formatted, numeralSystem)
    }
}

@Composable
fun String.toLocalizedDigits(): String {
    val system = AppLocalization.LocalNumeralSystem.current
    return AppLocalization.formatDigits(this, system)
}

@Composable
fun Number.toLocalizedDigits(): String {
    val system = AppLocalization.LocalNumeralSystem.current
    return AppLocalization.formatNumber(this, system)
}
