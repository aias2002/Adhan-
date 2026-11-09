package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- Aladhan API Models ---

@JsonClass(generateAdapter = true)
data class AladhanResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: AladhanData?
)

@JsonClass(generateAdapter = true)
data class AladhanData(
    @Json(name = "timings") val timings: PrayerTimings,
    @Json(name = "date") val dateInfo: DateInfo?
)

@JsonClass(generateAdapter = true)
data class PrayerTimings(
    @Json(name = "Fajr") val fajr: String,
    @Json(name = "Sunrise") val sunrise: String,
    @Json(name = "Dhuhr") val dhuhr: String,
    @Json(name = "Asr") val asr: String,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha") val isha: String
)

@JsonClass(generateAdapter = true)
data class DateInfo(
    @Json(name = "readable") val readable: String?,
    @Json(name = "hijri") val hijri: HijriDate?
)

@JsonClass(generateAdapter = true)
data class HijriDate(
    @Json(name = "date") val date: String,
    @Json(name = "day") val day: String,
    @Json(name = "weekday") val weekday: HijriWeekday?,
    @Json(name = "month") val month: HijriMonth?,
    @Json(name = "year") val year: String
)

@JsonClass(generateAdapter = true)
data class HijriWeekday(
    @Json(name = "ar") val ar: String?,
    @Json(name = "en") val en: String?
)

@JsonClass(generateAdapter = true)
data class HijriMonth(
    @Json(name = "number") val number: Int,
    @Json(name = "ar") val ar: String?,
    @Json(name = "en") val en: String?
)

// --- Alquran.cloud API Models ---

@JsonClass(generateAdapter = true)
data class QuranSurahListResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: List<SurahItem>?
)

@JsonClass(generateAdapter = true)
data class QuranSurahDetailResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: SurahDetail?
)

@JsonClass(generateAdapter = true)
data class SurahItem(
    @Json(name = "number") val number: Int,
    @Json(name = "name") val name: String,
    @Json(name = "englishName") val englishName: String,
    @Json(name = "englishNameTranslation") val englishNameTranslation: String,
    @Json(name = "numberOfAyahs") val numberOfAyahs: Int,
    @Json(name = "revelationType") val revelationType: String
)

@JsonClass(generateAdapter = true)
data class SurahDetail(
    @Json(name = "number") val number: Int,
    @Json(name = "name") val name: String,
    @Json(name = "englishName") val englishName: String,
    @Json(name = "englishNameTranslation") val englishNameTranslation: String,
    @Json(name = "numberOfAyahs") val numberOfAyahs: Int,
    @Json(name = "revelationType") val revelationType: String,
    @Json(name = "ayahs") val ayahs: List<AyahItem>
)

@JsonClass(generateAdapter = true)
data class AyahItem(
    @Json(name = "number") val number: Int,
    @Json(name = "text") val text: String,
    @Json(name = "numberInSurah") val numberInSurah: Int,
    @Json(name = "juz") val juz: Int?,
    @Json(name = "page") val page: Int?
)

// --- Algerian Preset Cities (All 58 Wilayas) ---

data class CityPreset(
    val code: Int,
    val nameAr: String,
    val nameEn: String,
    val lat: Double,
    val lng: Double,
    val countryAr: String = "الجزائر"
)

data class PrayerOffsets(
    val fajr: Int = 0,
    val sunrise: Int = 0,
    val dhuhr: Int = 0,
    val asr: Int = 0,
    val maghrib: Int = 2, // Official safety offset (+2 mins) for Maghrib
    val isha: Int = 0
)

data class PrayerAdhanSettings(
    val fajr: Boolean = true,
    val dhuhr: Boolean = true,
    val asr: Boolean = true,
    val maghrib: Boolean = true,
    val isha: Boolean = true,
    val is24HourFormat: Boolean = true
) {
    fun isEnabledFor(prayerKey: String): Boolean {
        return when (prayerKey.lowercase()) {
            "fajr" -> fajr
            "dhuhr" -> dhuhr
            "asr" -> asr
            "maghrib" -> maghrib
            "isha" -> isha
            else -> true
        }
    }
}

val GLOBAL_CITIES = listOf(
    CityPreset(101, "مكة المكرمة", "Makkah", 21.3891, 39.8579, "المملكة العربية السعودية"),
    CityPreset(102, "المدينة المنورة", "Madinah", 24.5247, 39.5692, "المملكة العربية السعودية"),
    CityPreset(103, "القدس الشريف", "Jerusalem", 31.7683, 35.2137, "فلسطين"),
    CityPreset(104, "الرياض", "Riyadh", 24.7136, 46.6753, "المملكة العربية السعودية"),
    CityPreset(105, "جدة", "Jeddah", 21.5433, 39.1728, "المملكة العربية السعودية"),
    CityPreset(106, "القاهرة", "Cairo", 30.0444, 31.2357, "مصر"),
    CityPreset(107, "الإسكندرية", "Alexandria", 31.2001, 29.9187, "مصر"),
    CityPreset(108, "إسطنبول", "Istanbul", 41.0082, 28.9784, "تركيا"),
    CityPreset(109, "أنقرة", "Ankara", 39.9334, 32.8597, "تركيا"),
    CityPreset(110, "دبي", "Dubai", 25.2048, 55.2708, "الإمارات العربية المتحدة"),
    CityPreset(111, "أبو ظبي", "Abu Dhabi", 24.4539, 54.3773, "الإمارات العربية المتحدة"),
    CityPreset(112, "الدوحة", "Doha", 25.2854, 51.5310, "قطر"),
    CityPreset(113, "الكويت", "Kuwait City", 29.3759, 47.9774, "الكويت"),
    CityPreset(114, "المنامة", "Manama", 26.2285, 50.5860, "البحرين"),
    CityPreset(115, "مسقط", "Muscat", 23.5880, 58.3829, "سلطنة عُمان"),
    CityPreset(116, "عَمّان", "Amman", 31.9454, 35.9284, "الأردن"),
    CityPreset(117, "دمشق", "Damascus", 33.5138, 36.2765, "سوريا"),
    CityPreset(118, "بيروت", "Beirut", 33.8938, 35.5018, "لبنان"),
    CityPreset(119, "بغداد", "Baghdad", 33.3152, 44.3661, "العراق"),
    CityPreset(120, "صنعاء", "Sanaa", 15.3694, 44.1910, "اليمن"),
    CityPreset(121, "تونس العاصمة", "Tunis", 36.8065, 10.1815, "تونس"),
    CityPreset(122, "صفاقس", "Sfax", 34.7406, 10.7603, "تونس"),
    CityPreset(123, "سوسة", "Sousse", 35.8256, 10.6369, "تونس"),
    CityPreset(124, "طرابلس", "Tripoli", 32.8872, 13.1913, "ليبيا"),
    CityPreset(125, "بنغازي", "Benghazi", 32.1167, 20.0667, "ليبيا"),
    CityPreset(126, "الرباط", "Rabat", 34.0209, -6.8416, "المغرب"),
    CityPreset(127, "الدار البيضاء", "Casablanca", 33.5731, -7.5898, "المغرب"),
    CityPreset(128, "فاس", "Fez", 34.0331, -5.0003, "المغرب"),
    CityPreset(129, "مراكش", "Marrakech", 31.6295, -7.9811, "المغرب"),
    CityPreset(130, "طنجة", "Tangier", 35.7595, -5.8340, "المغرب"),
    CityPreset(131, "وجدة", "Oujda", 34.6814, -1.9086, "المغرب"),
    CityPreset(132, "نواكشوط", "Nouakchott", 18.0735, -15.9582, "موريتانيا"),
    CityPreset(133, "الخرطوم", "Khartoum", 15.5007, 32.5599, "السودان"),
    CityPreset(134, "مقديشو", "Mogadishu", 2.0469, 45.3182, "الصومال"),
    CityPreset(135, "جاكرتا", "Jakarta", -6.2088, 106.8456, "إندونيسيا"),
    CityPreset(136, "كوالالمبور", "Kuala Lumpur", 3.1390, 101.6869, "ماليزيا"),
    CityPreset(137, "إسلام آباد", "Islamabad", 33.6844, 73.0479, "باكستان"),
    CityPreset(138, "كراتشي", "Karachi", 24.8607, 67.0011, "باكستان"),
    CityPreset(139, "دلهي", "New Delhi", 28.6139, 77.2090, "الهند"),
    CityPreset(140, "طشقند", "Tashkent", 41.2995, 69.2401, "أوزبكستان"),
    CityPreset(141, "سراييفو", "Sarajevo", 43.8563, 18.4131, "البوسنة والهرسك"),
    CityPreset(142, "باريس", "Paris", 48.8566, 2.3522, "فرنسا"),
    CityPreset(143, "مارسيليا", "Marseille", 43.2965, 5.3698, "فرنسا"),
    CityPreset(144, "ليون", "Lyon", 45.7640, 4.8357, "فرنسا"),
    CityPreset(145, "لندن", "London", 51.5074, -0.1278, "المملكة المتحدة"),
    CityPreset(146, "مانشستر", "Manchester", 53.4808, -2.2426, "المملكة المتحدة"),
    CityPreset(147, "برلين", "Berlin", 52.5200, 13.4050, "ألمانيا"),
    CityPreset(148, "فرانكفورت", "Frankfurt", 50.1109, 8.6821, "ألمانيا"),
    CityPreset(149, "بروكسل", "Brussels", 50.8503, 4.3517, "بلجيكا"),
    CityPreset(150, "أمستردام", "Amsterdam", 52.3676, 4.9041, "هولندا"),
    CityPreset(151, "مدريد", "Madrid", 40.4168, -3.7038, "إسبانيا"),
    CityPreset(152, "برشلونة", "Barcelona", 41.3879, 2.1699, "إسبانيا"),
    CityPreset(153, "روما", "Rome", 41.9028, 12.4964, "إيطاليا"),
    CityPreset(154, "ميلانو", "Milan", 45.4642, 9.1900, "إيطاليا"),
    CityPreset(155, "جنيف", "Geneva", 46.2044, 6.1432, "سويسرا"),
    CityPreset(156, "فيينا", "Vienna", 48.2082, 16.3738, "النمسا"),
    CityPreset(157, "موسكو", "Moscow", 55.7558, 37.6173, "روسيا"),
    CityPreset(158, "مونتريال", "Montreal", 45.5017, -73.5673, "كندا"),
    CityPreset(159, "تورونتو", "Toronto", 43.6532, -79.3832, "كندا"),
    CityPreset(160, "نيويورك", "New York", 40.7128, -74.0060, "الولايات المتحدة"),
    CityPreset(161, "شيكاغو", "Chicago", 41.8781, -87.6298, "الولايات المتحدة"),
    CityPreset(162, "لوس أنجلوس", "Los Angeles", 34.0522, -118.2437, "الولايات المتحدة"),
    CityPreset(163, "سيدني", "Sydney", -33.8688, 151.2093, "أستراليا"),
    CityPreset(164, "طوكيو", "Tokyo", 35.6762, 139.6503, "اليابان")
)

val ALGERIA_CITIES = listOf(
    CityPreset(1, "أدرار", "Adrar", 27.8742, -0.2939),
    CityPreset(2, "الشلف", "Chlef", 36.1653, 1.3345),
    CityPreset(3, "الأغواط", "Laghouat", 33.8000, 2.8651),
    CityPreset(4, "أم البواقي", "Oum El Bouaghi", 35.8755, 7.1135),
    CityPreset(5, "باتنة", "Batna", 35.5559, 6.1741),
    CityPreset(6, "بجاية", "Béjaïa", 36.7558, 5.0843),
    CityPreset(7, "بسكرة", "Biskra", 34.8500, 5.7333),
    CityPreset(8, "بشار", "Béchar", 31.6167, -2.2167),
    CityPreset(9, "البليدة", "Blida", 36.4700, 2.8300),
    CityPreset(10, "البويرة", "Bouira", 36.3749, 3.9020),
    CityPreset(11, "تمنراست", "Tamanrasset", 22.7850, 5.5228),
    CityPreset(12, "تبسة", "Tébessa", 35.4042, 8.1242),
    CityPreset(13, "تلمسان", "Tlemcen", 34.8783, -1.3150),
    CityPreset(14, "تيارت", "Tiaret", 35.3710, 1.3170),
    CityPreset(15, "تيزي وزو", "Tizi Ouzou", 36.7118, 4.0459),
    CityPreset(16, "الجزائر العاصمة", "Algiers", 36.7538, 3.0588),
    CityPreset(17, "الجلفة", "Djelfa", 34.6728, 3.2630),
    CityPreset(18, "جيجل", "Jijel", 36.8206, 5.7667),
    CityPreset(19, "سطيف", "Sétif", 36.1900, 5.4100),
    CityPreset(20, "سعيدة", "Saïda", 34.8303, 0.1517),
    CityPreset(21, "سكيكدة", "Skikda", 36.8792, 6.9042),
    CityPreset(22, "سيدي بلعباس", "Sidi Bel Abbès", 35.1899, -0.6308),
    CityPreset(23, "عنابة", "Annaba", 36.9000, 7.7667),
    CityPreset(24, "قالمة", "Guelma", 36.4622, 7.4261),
    CityPreset(25, "قسنطينة", "Constantine", 36.3650, 6.6147),
    CityPreset(26, "المدية", "Médéa", 36.2642, 2.7539),
    CityPreset(27, "مستغانم", "Mostaganem", 35.9312, 0.0892),
    CityPreset(28, "المسيلة", "M'Sila", 35.7058, 4.5419),
    CityPreset(29, "معسكر", "Mascara", 35.3966, 0.1403),
    CityPreset(30, "ورقلة", "Ouargla", 31.9593, 5.3074),
    CityPreset(31, "وهران", "Oran", 35.6987, -0.6349),
    CityPreset(32, "البيض", "El Bayadh", 33.6831, 1.0193),
    CityPreset(33, "إليزي", "Illizi", 26.4833, 8.4667),
    CityPreset(34, "برج بوعريريج", "Bordj Bou Arréridj", 36.0732, 4.7611),
    CityPreset(35, "بومرداس", "Boumerdès", 36.7664, 3.4772),
    CityPreset(36, "الطارف", "El Tarf", 36.7672, 8.3138),
    CityPreset(37, "تندوف", "Tindouf", 27.6711, -8.1478),
    CityPreset(38, "تسمسيلت", "Tissemsilt", 35.6072, 1.8108),
    CityPreset(39, "الوادي", "El Oued", 33.3683, 6.8674),
    CityPreset(40, "خنشلة", "Khenchela", 35.4358, 7.1433),
    CityPreset(41, "سوق أهراس", "Souk Ahras", 36.2864, 7.9511),
    CityPreset(42, "تيبازة", "Tipaza", 36.5897, 2.4475),
    CityPreset(43, "ميلة", "Mila", 36.4503, 6.2644),
    CityPreset(44, "عين الدفلى", "Aïn Defla", 36.2641, 1.9679),
    CityPreset(45, "النعامة", "Naâma", 33.2667, -0.3167),
    CityPreset(46, "عين تموشنت", "Aïn Témouchent", 35.2975, -1.1403),
    CityPreset(47, "غرداية", "Ghardaïa", 32.4900, 3.6700),
    CityPreset(48, "غليزان", "Relizane", 35.7372, 0.5558),
    CityPreset(49, "المغير", "El M'Ghair", 33.9500, 5.9200),
    CityPreset(50, "المنيعة", "El Meniaa", 30.5833, 2.8833),
    CityPreset(51, "أولاد جلال", "Ouled Djellal", 34.4333, 5.0667),
    CityPreset(52, "برج باجي مختار", "Bordj Badji Mokhtar", 21.3283, 0.9547),
    CityPreset(53, "بني عباس", "Béni Abbès", 30.1333, -2.1667),
    CityPreset(54, "تيميمون", "Timimoun", 29.2639, 0.2310),
    CityPreset(55, "تقرت", "Touggourt", 33.1053, 6.0628),
    CityPreset(56, "جانت", "Djanet", 24.5553, 9.4858),
    CityPreset(57, "إن صالح", "In Salah", 27.1935, 2.4607),
    CityPreset(58, "إن قزام", "In Guezzam", 19.5686, 5.7725),
    // التقسيم الإداري الجديد للجزائر (الولايات الـ 11 الجديدة: 59 إلى 69)
    CityPreset(59, "العلمة", "El Eulma", 36.1528, 5.6908),
    CityPreset(60, "بريكة", "Barika", 35.3892, 5.3658),
    CityPreset(61, "بوسعادة", "Bou Saâda", 35.2833, 4.1833),
    CityPreset(62, "عين وسارة", "Aïn Oussara", 35.4500, 2.9000),
    CityPreset(63, "مسعد", "Messaad", 34.1500, 3.5000),
    CityPreset(64, "قصر الشلالة", "Ksar Chellala", 35.1667, 2.3167),
    CityPreset(65, "آفلو", "Aflou", 34.1167, 2.1000),
    CityPreset(66, "فرندة", "Frenda", 35.0667, 1.0500),
    CityPreset(67, "الأبيض سيدي الشيخ", "El Abiodh Sidi Cheikh", 32.8944, 0.5469),
    CityPreset(68, "القل", "Collo", 37.0069, 6.5603),
    CityPreset(69, "مغنية", "Maghnia", 34.8464, -1.7289)
)

val ALL_PRESET_CITIES = ALGERIA_CITIES + GLOBAL_CITIES

data class IslamicHoliday(
    val id: String,
    val nameAr: String,
    val hijriDateStr: String,
    val hijriMonth: Int, // 1 to 12
    val hijriDay: Int,
    val description: String,
    val significance: String
)

val ISLAMIC_HOLIDAYS_LIST = listOf(
    IslamicHoliday(
        id = "hijri_new_year",
        nameAr = "رأس السنة الهجرية",
        hijriDateStr = "1 محرم",
        hijriMonth = 1,
        hijriDay = 1,
        description = "بداية العام الهجري الجديد وذكرى هجرة المصطفى صلى الله عليه وسلم",
        significance = "عطلة دينية ومناسبة مباركة"
    ),
    IslamicHoliday(
        id = "ashura",
        nameAr = "يوم عاشوراء",
        hijriDateStr = "10 محرم",
        hijriMonth = 1,
        hijriDay = 10,
        description = "اليوم الذي نجى الله فيه موسى عليه السلام وقومه من فرعون، ويُستحب صيامه",
        significance = "صيام مستحب وتكفير لذنوب سنة ماضية"
    ),
    IslamicHoliday(
        id = "mawlid",
        nameAr = "المولد النبوي الشريف",
        hijriDateStr = "12 ربيع الأول",
        hijriMonth = 3,
        hijriDay = 12,
        description = "ذكرى مولد خير البرية نبينا محمد صلى الله عليه وسلم",
        significance = "احتفال ومولد الهادي البشير"
    ),
    IslamicHoliday(
        id = "isra_miraj",
        nameAr = "ذكرى الإسراء والمعراج",
        hijriDateStr = "27 رجب",
        hijriMonth = 7,
        hijriDay = 27,
        description = "معجزة الإسراء من المسجد الحرام إلى المسجد الأقصى والعروج إلى السماوات العلى",
        significance = "فرض الصلوات الخمس ومناسبة إيمانية عظيمة"
    ),
    IslamicHoliday(
        id = "nisf_shaban",
        nameAr = "ليلة النصف من شعبان",
        hijriDateStr = "15 شعبان",
        hijriMonth = 8,
        hijriDay = 15,
        description = "ليلة مباركة تحول فيها القبلة وتُرفع فيها الأعمال إلى الله تعالى",
        significance = "استعداد روحي لشهر رمضان المبارك"
    ),
    IslamicHoliday(
        id = "ramadan_start",
        nameAr = "أول أيام شهر رمضان المبارك",
        hijriDateStr = "1 رمضان",
        hijriMonth = 9,
        hijriDay = 1,
        description = "شهر الصيام والقرآن والرحمة والمغفرة والعتق من النيران",
        significance = "ركن الصيام الفريضة"
    ),
    IslamicHoliday(
        id = "laylat_qadr",
        nameAr = "ليلة القدر المباركة (الوترية)",
        hijriDateStr = "27 رمضان",
        hijriMonth = 9,
        hijriDay = 27,
        description = "خير من ألف شهر، تنزل فيها الملائكة والروح بإذن ربهم من كل أمر",
        significance = "أعظم ليالي العام عبادةً ودعاءً"
    ),
    IslamicHoliday(
        id = "eid_fitr",
        nameAr = "عيد الفطر المبارك",
        hijriDateStr = "1 شوال",
        hijriMonth = 10,
        hijriDay = 1,
        description = "فرحة الصائمين بتمام شهر الصيام وصلة الأرحام وإخراج الزكاة",
        significance = "عيد إسلامي بهيج (3 أيام)"
    ),
    IslamicHoliday(
        id = "arafah",
        nameAr = "يوم عرفة المبارك",
        hijriDateStr = "9 ذو الحجة",
        hijriMonth = 12,
        hijriDay = 9,
        description = "أعظم أيام الحج وصيامه يكفر سنتين (ماضية ومقبلة) لغير الحاج",
        significance = "ركن الحج الأعظم ويوم المغفرة والعتق"
    ),
    IslamicHoliday(
        id = "eid_adha",
        nameAr = "عيد الأضحى المبارك",
        hijriDateStr = "10 ذو الحجة",
        hijriMonth = 12,
        hijriDay = 10,
        description = "ذكرى فداء نبي الله إسماعيل عليه السلام وشعيرة الأضحية المباركة",
        significance = "عيد الأضحى وأيام التشريق المباركة"
    )
)

data class NextPrayerInfo(
    val nameAr: String,
    val nameEn: String,
    val formattedTime: String,
    val remainingMillis: Long
)

data class CalculationMethod(
    val id: Int,
    val nameAr: String,
    val regionAr: String,
    val detailsAr: String
)

val COMMON_CALCULATION_METHODS = listOf(
    CalculationMethod(
        id = 16,
        nameAr = "وزارة الشؤون الدينية والأوقاف (الجزائر)",
        regionAr = "الجزائر (الافتراضي الرسمي)",
        detailsAr = "الفجر: 18° ، العشاء: 17° (الحساب الرسمي الجزائري)"
    ),
    CalculationMethod(
        id = 4,
        nameAr = "جامعة أم القرى - مكة المكرمة",
        regionAr = "المملكة العربية السعودية ودول الخليج",
        detailsAr = "الفجر: 18.5° ، العشاء: بعد المغرب بـ 90 دقيقة (120 في رمضان)"
    ),
    CalculationMethod(
        id = 5,
        nameAr = "الهيئة المصرية العامة للمساحة",
        regionAr = "مصر، سوريا، لبنان وأجزاء من إفريقيا",
        detailsAr = "الفجر: 19.5° ، العشاء: 17.5°"
    ),
    CalculationMethod(
        id = 3,
        nameAr = "رابطة العالم الإسلامي (MWL)",
        regionAr = "أوروبا، الشرق الأقصى ومعظم الدول",
        detailsAr = "الفجر: 18° ، العشاء: 17°"
    ),
    CalculationMethod(
        id = 2,
        nameAr = "الجمعية الإسلامية لأمريكا الشمالية (ISNA)",
        regionAr = "الولايات المتحدة الأمريكية وكندا",
        detailsAr = "الفجر: 15° ، العشاء: 15°"
    ),
    CalculationMethod(
        id = 12,
        nameAr = "اتحاد المنظمات الإسلامية في فرنسا (UOIF)",
        regionAr = "فرنسا ومناطق أوروبا الغربية",
        detailsAr = "الفجر: 12° ، العشاء: 12°"
    ),
    CalculationMethod(
        id = 13,
        nameAr = "رئاسة الشؤون الدينية التركية (Diyanet)",
        regionAr = "تركيا ودول البلقان",
        detailsAr = "الفجر: 18° ، العشاء: 17°"
    ),
    CalculationMethod(
        id = 1,
        nameAr = "جامعة العلوم الإسلامية بكراتشي",
        regionAr = "باكستان، الهند، بنغلاديش وأفغانستان",
        detailsAr = "الفجر: 18° ، العشاء: 18°"
    )
)

fun CalculationMethod.getName(isAr: Boolean): String = if (isAr) nameAr else when (id) {
    16 -> "Ministry of Religious Affairs and Awqaf (Algeria)"
    4 -> "Umm al-Qura University, Makkah"
    5 -> "Egyptian General Authority of Survey"
    3 -> "Muslim World League (MWL)"
    2 -> "Islamic Society of North America (ISNA)"
    12 -> "Union of Islamic Organizations of France (UOIF)"
    13 -> "Presidency of Religious Affairs, Turkey (Diyanet)"
    1 -> "University of Islamic Sciences, Karachi"
    else -> nameAr
}

fun CalculationMethod.getRegion(isAr: Boolean): String = if (isAr) regionAr else when (id) {
    16 -> "Algeria (official default)"
    4 -> "Saudi Arabia and the Gulf states"
    5 -> "Egypt, Syria, Lebanon and parts of Africa"
    3 -> "Europe, the Far East and most countries"
    2 -> "United States and Canada"
    12 -> "France and parts of Western Europe"
    13 -> "Turkey and the Balkans"
    1 -> "Pakistan, India, Bangladesh and Afghanistan"
    else -> regionAr
}

fun CalculationMethod.getDetails(isAr: Boolean): String = if (isAr) detailsAr else when (id) {
    16 -> "Fajr: 18°, Isha: 17° (Official Algerian standard)"
    4 -> "Fajr: 18.5°, Isha: 90 min after Maghrib (120 in Ramadan)"
    5 -> "Fajr: 19.5°, Isha: 17.5°"
    3 -> "Fajr: 18°, Isha: 17°"
    2 -> "Fajr: 15°, Isha: 15°"
    12 -> "Fajr: 12°, Isha: 12°"
    13 -> "Fajr: 18°, Isha: 17°"
    1 -> "Fajr: 18°, Isha: 18°"
    else -> detailsAr
}

data class AppBackgroundTheme(
    val id: String,
    val nameAr: String,
    val descAr: String,
    val lightBgColor: Long,
    val darkBgColor: Long,
    val lightCardColor: Long,
    val darkCardColor: Long
)

val PRESET_BACKGROUND_THEMES = listOf(
    AppBackgroundTheme(
        id = "parchment",
        nameAr = "ورق كلاسيكي دافئ",
        descAr = "المظهر التراثي الأصيل لتطبيق Adhan+",
        lightBgColor = 0xFFF7F4EEL,
        darkBgColor = 0xFF14181CL,
        lightCardColor = 0xFFF2ECE1L,
        darkCardColor = 0xFF1C2228L
    ),
    AppBackgroundTheme(
        id = "marble",
        nameAr = "رخام أبيض نقي",
        descAr = "مظهر ناصع ومشرق وأنيق",
        lightBgColor = 0xFFFAF8F5L,
        darkBgColor = 0xFF12161AL,
        lightCardColor = 0xFFF0EEEAL,
        darkCardColor = 0xFF1A1F24L
    ),
    AppBackgroundTheme(
        id = "emerald",
        nameAr = "أخضر أندلسي هادئ",
        descAr = "مستوحى من عمارة وزخارف المساجد",
        lightBgColor = 0xFFF0F5F2L,
        darkBgColor = 0xFF0F1A15L,
        lightCardColor = 0xFFE2EDE6L,
        darkCardColor = 0xFF16251EL
    ),
    AppBackgroundTheme(
        id = "dark_night",
        nameAr = "كحلي ليلي داكن",
        descAr = "مريح للعينين وخافت في الإضاءة الضعيفة",
        lightBgColor = 0xFF0E1318L,
        darkBgColor = 0xFF0A0E12L,
        lightCardColor = 0xFF172028L,
        darkCardColor = 0xFF121922L
    ),
    AppBackgroundTheme(
        id = "bright_gold",
        nameAr = "ذهبي براق",
        descAr = "مظهر ملكي ذهبي فاخر وبراق",
        lightBgColor = 0xFFFFF9E6L,
        darkBgColor = 0xFF2A2107L,
        lightCardColor = 0xFFFFF1C5L,
        darkCardColor = 0xFF3D300BL
    ),
    AppBackgroundTheme(
        id = "pale_grey",
        nameAr = "رمادي باهت",
        descAr = "مظهر كلاسيكي هادئ وبسيط بالرمادي المريح",
        lightBgColor = 0xFFF3F4F6L,
        darkBgColor = 0xFF1B1E22L,
        lightCardColor = 0xFFE5E7EBL,
        darkCardColor = 0xFF252A30L
    )
)
