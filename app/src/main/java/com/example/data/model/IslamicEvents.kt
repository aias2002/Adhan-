package com.example.data.model

data class IslamicEvent(
    val titleAr: String,
    val hijriDay: Int,
    val hijriMonth: Int,
    val hijriMonthNameAr: String,
    val gregorianDateAr: String,
    val descriptionAr: String,
    val iconEmoji: String
)

val ISLAMIC_HOLIDAYS = listOf(
    IslamicEvent("رأس السنة الهجرية 1448", 1, 1, "محرم", "16 يونيو 2026", "بداية العام الهجري الجديد وهجرة النبي ﷺ", "🌙"),
    IslamicEvent("يوم عاشوراء المبارك", 10, 1, "محرم", "25 يونيو 2026", "يوم نجّى الله فيه موسى عليه السلام ويسن صيامه", "🤲"),
    IslamicEvent("المولد النبوي الشريف", 12, 3, "ربيع الأول", "24 أغسطس 2026", "ذكرى مولد خير الأنام محمد ﷺ", "✨"),
    IslamicEvent("ذكرى الإسراء والمعراج", 27, 7, "رجب", "5 يناير 2027", "رحلة الإسراء والمعراج وفرض الصلوات الخمس", "🕌"),
    IslamicEvent("ليلة النصف من شعبان", 15, 8, "شعبان", "23 يناير 2027", "ليلة مباركة تحول فيها القبلة إلى المسجد الحرام", "📿"),
    IslamicEvent("أول أيام شهر رمضان", 1, 9, "رمضان", "8 فبراير 2027", "شهر الصيام والقرآن والرحمة والغفران", "📖"),
    IslamicEvent("ليلة القدر المباركة", 27, 9, "رمضان", "6 مارس 2027", "خير من ألف شهر، تنزل فيها الملائكة والروح", "🌟"),
    IslamicEvent("عيد الفطر السعيد", 1, 10, "شوال", "10 مارس 2027", "فرحة الصائمين بتمام صيام شهر رمضان", "🎉"),
    IslamicEvent("يوم عرفة المبارك", 9, 12, "ذو الحجة", "16 مايو 2027", "أفضل أيام السنة، يسن صيامه لغير الحاج", "🕋"),
    IslamicEvent("عيد الأضحى المبارك", 10, 12, "ذو الحجة", "17 مايو 2027", "عيد التضحية وسنة الخليل إبراهيم عليه السلام", "🐑")
)

fun calculateImsakTime(fajrTimeStr: String): String {
    val cleanFajr = fajrTimeStr.take(5)
    val parts = cleanFajr.split(":")
    if (parts.size == 2) {
        val h = parts[0].toIntOrNull() ?: return cleanFajr
        val m = parts[1].toIntOrNull() ?: return cleanFajr
        val totalMins = h * 60 + m - 10
        val newH = (totalMins / 60 + 24) % 24
        val newM = (totalMins % 60 + 60) % 60
        return String.format(java.util.Locale.US, "%02d:%02d", newH, newM)
    }
    return cleanFajr
}
