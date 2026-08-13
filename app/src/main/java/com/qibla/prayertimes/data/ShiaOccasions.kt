package com.qibla.prayertimes.data

import android.content.Context
import com.qibla.prayertimes.util.LocalePrefs

enum class OccasionType { CELEBRATION, MOURNING }

data class ShiaOccasion(
    val hijriMonth: Int,
    val hijriDay: Int,
    val type: OccasionType,
    val nameFa: String,
    val nameAr: String,
    val nameEn: String
) {
    fun name(context: Context): String = when (LocalePrefs.currentLanguage(context)) {
        "en" -> nameEn
        "ar" -> nameAr
        else -> nameFa
    }
}

/**
 * A curated (not exhaustive) list of the most widely observed occasions in the Twelver Shia
 * calendar — chosen to be the ones essentially every mainstream Shia source agrees on, to avoid
 * showing a disputed date as if it were settled. Dates without broad agreement across sources
 * (a few Imams' martyrdom/birth dates are cited differently in different narrations) are left
 * out rather than guessed at.
 */
object ShiaOccasions {
    private val ALL = listOf(
        // --- Muharram ---
        ShiaOccasion(1, 1, OccasionType.CELEBRATION, "آغاز سال نو قمری", "رأس السنة الهجرية", "Islamic New Year"),
        ShiaOccasion(1, 2, OccasionType.MOURNING, "ورود امام حسین (ع) به کربلا", "دخول الإمام الحسين (ع) كربلاء", "Imam Husayn's arrival in Karbala"),
        ShiaOccasion(1, 7, OccasionType.MOURNING, "قطع آب بر یاران امام حسین (ع)", "قطع الماء عن أصحاب الإمام الحسين (ع)", "Water cut off from Imam Husayn's camp"),
        ShiaOccasion(1, 9, OccasionType.MOURNING, "تاسوعای حسینی", "تاسوعاء الحسيني", "Tasu'a"),
        ShiaOccasion(1, 10, OccasionType.MOURNING, "عاشورا؛ شهادت امام حسین (ع)", "عاشوراء؛ استشهاد الإمام الحسين (ع)", "Day of Ashura — martyrdom of Imam Husayn"),
        ShiaOccasion(1, 25, OccasionType.MOURNING, "شهادت امام سجاد (ع)", "استشهاد الإمام السجاد (ع)", "Martyrdom of Imam al-Sajjad"),

        // --- Safar ---
        ShiaOccasion(2, 20, OccasionType.MOURNING, "اربعین حسینی", "الأربعين الحسيني", "Arba'een"),
        ShiaOccasion(2, 28, OccasionType.MOURNING, "رحلت پیامبر اکرم (ص) و شهادت امام حسن مجتبی (ع)", "رحيل النبي (ص) واستشهاد الإمام الحسن المجتبى (ع)", "Passing of the Prophet & martyrdom of Imam Hasan"),
        ShiaOccasion(2, 29, OccasionType.MOURNING, "شهادت امام رضا (ع)", "استشهاد الإمام الرضا (ع)", "Martyrdom of Imam Reza"),

        // --- Rabi al-Awwal ---
        ShiaOccasion(3, 17, OccasionType.CELEBRATION, "ولادت پیامبر اکرم (ص) و امام جعفر صادق (ع)", "ولادة النبي (ص) والإمام جعفر الصادق (ع)", "Birth of the Prophet & Imam Ja'far al-Sadiq"),

        // --- Jumada al-Thani ---
        ShiaOccasion(6, 3, OccasionType.MOURNING, "شهادت حضرت فاطمه زهرا (س)", "استشهاد السيدة فاطمة الزهراء (س)", "Martyrdom of Lady Fatima al-Zahra"),
        ShiaOccasion(6, 20, OccasionType.CELEBRATION, "ولادت حضرت فاطمه زهرا (س)", "ولادة السيدة فاطمة الزهراء (س)", "Birth of Lady Fatima al-Zahra"),

        // --- Rajab ---
        ShiaOccasion(7, 1, OccasionType.CELEBRATION, "ولادت امام محمدباقر (ع)", "ولادة الإمام محمد الباقر (ع)", "Birth of Imam Muhammad al-Baqir"),
        ShiaOccasion(7, 13, OccasionType.CELEBRATION, "ولادت امام علی (ع)", "ولادة الإمام علي (ع)", "Birth of Imam Ali"),
        ShiaOccasion(7, 25, OccasionType.MOURNING, "شهادت امام موسی کاظم (ع)", "استشهاد الإمام موسى الكاظم (ع)", "Martyrdom of Imam Musa al-Kadhim"),
        ShiaOccasion(7, 27, OccasionType.CELEBRATION, "مبعث پیامبر اکرم (ص)", "المبعث النبوي الشريف", "Prophet's first revelation (Mab'ath)"),

        // --- Sha'ban ---
        ShiaOccasion(8, 3, OccasionType.CELEBRATION, "ولادت امام حسین (ع)", "ولادة الإمام الحسين (ع)", "Birth of Imam Husayn"),
        ShiaOccasion(8, 4, OccasionType.CELEBRATION, "ولادت حضرت ابوالفضل العباس (ع)", "ولادة أبي الفضل العباس (ع)", "Birth of Abu al-Fadl al-Abbas"),
        ShiaOccasion(8, 5, OccasionType.CELEBRATION, "ولادت امام زین‌العابدین (ع)", "ولادة الإمام زين العابدين (ع)", "Birth of Imam Zayn al-Abidin"),
        ShiaOccasion(8, 15, OccasionType.CELEBRATION, "نیمه شعبان؛ ولادت امام زمان (عج)", "نصف شعبان؛ ولادة الإمام المهدي (عج)", "Mid-Sha'ban — Birth of Imam al-Mahdi"),

        // --- Ramadan ---
        ShiaOccasion(9, 1, OccasionType.CELEBRATION, "آغاز ماه مبارک رمضان", "بداية شهر رمضان المبارك", "Start of Ramadan"),
        ShiaOccasion(9, 15, OccasionType.CELEBRATION, "ولادت امام حسن مجتبی (ع)", "ولادة الإمام الحسن المجتبى (ع)", "Birth of Imam Hasan al-Mujtaba"),
        ShiaOccasion(9, 19, OccasionType.MOURNING, "ضربت خوردن امام علی (ع) — شب قدر", "ضرب الإمام علي (ع) — ليلة القدر", "Imam Ali struck in Kufa Mosque — a Laylat al-Qadr"),
        ShiaOccasion(9, 21, OccasionType.MOURNING, "شهادت امام علی (ع)", "استشهاد الإمام علي (ع)", "Martyrdom of Imam Ali"),
        ShiaOccasion(9, 23, OccasionType.CELEBRATION, "شب قدر", "ليلة القدر", "Laylat al-Qadr"),

        // --- Shawwal ---
        ShiaOccasion(10, 1, OccasionType.CELEBRATION, "عید سعید فطر", "عيد الفطر السعيد", "Eid al-Fitr"),
        ShiaOccasion(10, 25, OccasionType.MOURNING, "شهادت امام جعفر صادق (ع)", "استشهاد الإمام جعفر الصادق (ع)", "Martyrdom of Imam Ja'far al-Sadiq"),

        // --- Dhu al-Qi'dah ---
        ShiaOccasion(11, 11, OccasionType.CELEBRATION, "ولادت امام رضا (ع)", "ولادة الإمام الرضا (ع)", "Birth of Imam Reza"),

        // --- Dhu al-Hijjah ---
        ShiaOccasion(12, 7, OccasionType.MOURNING, "شهادت امام محمدباقر (ع)", "استشهاد الإمام محمد الباقر (ع)", "Martyrdom of Imam Muhammad al-Baqir"),
        ShiaOccasion(12, 9, OccasionType.CELEBRATION, "روز عرفه", "يوم عرفة", "Day of Arafah"),
        ShiaOccasion(12, 10, OccasionType.CELEBRATION, "عید سعید قربان", "عيد الأضحى السعيد", "Eid al-Adha"),
        ShiaOccasion(12, 15, OccasionType.CELEBRATION, "ولادت امام هادی (ع)", "ولادة الإمام الهادي (ع)", "Birth of Imam al-Hadi"),
        ShiaOccasion(12, 18, OccasionType.CELEBRATION, "عید سعید غدیر خم", "عيد الغدير السعيد", "Eid al-Ghadeer"),
        ShiaOccasion(12, 24, OccasionType.CELEBRATION, "عید مباهله", "عيد المباهلة", "Eid al-Mubahila")
    )

    /** Occasions falling on this Hijri month/day, if any (usually zero or one). */
    fun forDate(hijriMonth: Int, hijriDay: Int): List<ShiaOccasion> =
        ALL.filter { it.hijriMonth == hijriMonth && it.hijriDay == hijriDay }
}
