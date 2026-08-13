package com.qibla.prayertimes.model

import android.content.Context
import com.qibla.prayertimes.util.LocalePrefs

/** One catalog entry with names in all three supported languages, plus coordinates. */
private data class CityEntry(
    val fa: String,
    val en: String,
    val ar: String,
    val lat: Double,
    val lon: Double
)

private val CITY_CATALOG = listOf(
    // --- حرمین شریفین / The Two Holy Mosques ---
    CityEntry("مکه مکرمه", "Mecca", "مكة المكرمة", 21.4225, 39.8262),
    CityEntry("مدینه منوره", "Medina", "المدينة المنورة", 24.5247, 39.5692),

    // --- شهرهای زیارتی عراق / Iraqi pilgrimage cities ---
    CityEntry("کربلا", "Karbala", "كربلاء", 32.6149, 44.0246),
    CityEntry("نجف", "Najaf", "النجف", 31.9986, 44.3325),
    CityEntry("کاظمین (بغداد)", "Kadhimiya (Baghdad)", "الكاظمية (بغداد)", 33.3785, 44.3405),
    CityEntry("سامرا", "Samarra", "سامراء", 34.1959, 43.8742),
    CityEntry("بغداد", "Baghdad", "بغداد", 33.3152, 44.3661),
    CityEntry("بصره", "Basra", "البصرة", 30.5085, 47.7835),

    // --- شام و منطقه / Levant ---
    CityEntry("دمشق", "Damascus", "دمشق", 33.5138, 36.2765),
    CityEntry("بیروت", "Beirut", "بيروت", 33.8938, 35.5018),
    CityEntry("قدس (بیت‌المقدس)", "Jerusalem", "القدس", 31.7683, 35.2137),

    // --- استان‌های مرکز ایران / Central Iran ---
    CityEntry("تهران", "Tehran", "طهران", 35.6892, 51.3890),
    CityEntry("قم", "Qom", "قم", 34.6401, 50.8764),
    CityEntry("کرج", "Karaj", "كرج", 35.8400, 50.9391),
    CityEntry("اراک", "Arak", "أراك", 34.0954, 49.6900),
    CityEntry("قزوین", "Qazvin", "قزوين", 36.2688, 50.0041),
    CityEntry("سمنان", "Semnan", "سمنان", 35.5769, 53.3971),

    // --- خراسان / Khorasan ---
    CityEntry("مشهد", "Mashhad", "مشهد", 36.2605, 59.6168),
    CityEntry("نیشابور", "Nishapur", "نيسابور", 36.2133, 58.7958),
    CityEntry("بیرجند", "Birjand", "بيرجند", 32.8663, 59.2211),
    CityEntry("بجنورد", "Bojnord", "بجنورد", 37.4747, 57.3291),
    CityEntry("سبزوار", "Sabzevar", "سبزوار", 36.2126, 57.6788),

    // --- فارس و جنوب / Fars & the south ---
    CityEntry("شیراز", "Shiraz", "شيراز", 29.5918, 52.5837),
    CityEntry("بندرعباس", "Bandar Abbas", "بندر عباس", 27.1865, 56.2808),
    CityEntry("بوشهر", "Bushehr", "بوشهر", 28.9684, 50.8385),
    CityEntry("یاسوج", "Yasuj", "ياسوج", 30.6682, 51.5880),
    CityEntry("بندر لنگه", "Bandar Lengeh", "بندر لنگه", 26.5578, 54.8807),
    CityEntry("کیش", "Kish Island", "جزيرة كيش", 26.5578, 53.9773),
    CityEntry("قشم", "Qeshm", "قشم", 26.9581, 56.2719),

    // --- اصفهان و مرکز / Isfahan & central plateau ---
    CityEntry("اصفهان", "Isfahan", "أصفهان", 32.6546, 51.6680),
    CityEntry("کاشان", "Kashan", "كاشان", 33.9850, 51.4100),
    CityEntry("یزد", "Yazd", "يزد", 31.8974, 54.3569),
    CityEntry("کرمان", "Kerman", "كرمان", 30.2839, 57.0834),
    CityEntry("زاهدان", "Zahedan", "زاهدان", 29.4963, 60.8629),
    CityEntry("رفسنجان", "Rafsanjan", "رفسنجان", 30.4067, 55.9938),

    // --- غرب و شمال‌غرب / West & northwest ---
    CityEntry("تبریز", "Tabriz", "تبريز", 38.0800, 46.2919),
    CityEntry("ارومیه", "Urmia", "أرومية", 37.5527, 45.0761),
    CityEntry("اردبیل", "Ardabil", "أردبيل", 38.2498, 48.2933),
    CityEntry("زنجان", "Zanjan", "زنجان", 36.6736, 48.4787),
    CityEntry("همدان", "Hamadan", "همدان", 34.7992, 48.5146),
    CityEntry("کرمانشاه", "Kermanshah", "كرمانشاه", 34.3142, 47.0650),
    CityEntry("سنندج", "Sanandaj", "سنندج", 35.3145, 46.9923),
    CityEntry("ایلام", "Ilam", "إيلام", 33.6374, 46.4227),
    CityEntry("خرم‌آباد", "Khorramabad", "خرم آباد", 33.4870, 48.3557),

    // --- شمال و دریای خزر / North & the Caspian coast ---
    CityEntry("رشت", "Rasht", "رشت", 37.2809, 49.5832),
    CityEntry("ساری", "Sari", "ساري", 36.5633, 53.0601),
    CityEntry("گرگان", "Gorgan", "جرجان", 36.8386, 54.4341),
    CityEntry("بابل", "Babol", "بابل الإيرانية", 36.5513, 52.6789),
    CityEntry("آمل", "Amol", "آمل", 36.4696, 52.3512),
    CityEntry("چالوس", "Chalus", "جالوس", 36.6550, 51.4200),
    CityEntry("بندر انزلی", "Bandar-e Anzali", "بندر أنزلي", 37.4646, 49.4599),

    // --- خوزستان و جنوب‌غرب / Khuzestan & southwest ---
    CityEntry("اهواز", "Ahvaz", "الأهواز", 31.3183, 48.6706),
    CityEntry("آبادان", "Abadan", "عبادان", 30.3392, 48.3043),
    CityEntry("دزفول", "Dezful", "دزفول", 32.3814, 48.4058),
    CityEntry("شوشتر", "Shushtar", "شوشتر", 32.0447, 48.8558),

    // --- شهرهای مهم جهان / Major world cities ---
    CityEntry("استانبول", "Istanbul", "إسطنبول", 41.0082, 28.9784),
    CityEntry("آنکارا", "Ankara", "أنقرة", 39.9334, 32.8597),
    CityEntry("قاهره", "Cairo", "القاهرة", 30.0444, 31.2357),
    CityEntry("کراچی", "Karachi", "كراتشي", 24.8607, 67.0011),
    CityEntry("لاهور", "Lahore", "لاهور", 31.5497, 74.3436),
    CityEntry("اسلام‌آباد", "Islamabad", "إسلام آباد", 33.6844, 73.0479),
    CityEntry("کابل", "Kabul", "كابل", 34.5553, 69.2075),
    CityEntry("دبی", "Dubai", "دبي", 25.2048, 55.2708),
    CityEntry("ابوظبی", "Abu Dhabi", "أبوظبي", 24.4539, 54.3773),
    CityEntry("دوحه", "Doha", "الدوحة", 25.2854, 51.5310),
    CityEntry("منامه", "Manama", "المنامة", 26.2285, 50.5860),
    CityEntry("کویت", "Kuwait City", "مدينة الكويت", 29.3759, 47.9774),
    CityEntry("مسقط", "Muscat", "مسقط", 23.5880, 58.3829),
    CityEntry("ریاض", "Riyadh", "الرياض", 24.7136, 46.6753),
    CityEntry("جده", "Jeddah", "جدة", 21.4858, 39.1925),
    CityEntry("عمان (اردن)", "Amman", "عمّان", 31.9454, 35.9284),
    CityEntry("صنعا", "Sanaa", "صنعاء", 15.3694, 44.1910),
    CityEntry("خارطوم", "Khartoum", "الخرطوم", 15.5007, 32.5599),
    CityEntry("جاکارتا", "Jakarta", "جاكرتا", -6.2088, 106.8456),
    CityEntry("کوالالامپور", "Kuala Lumpur", "كوالالمبور", 3.1390, 101.6869),
    CityEntry("داکا", "Dhaka", "دكا", 23.8103, 90.4125),
    CityEntry("دهلی نو", "New Delhi", "نيودلهي", 28.6139, 77.2090),
    CityEntry("لندن", "London", "لندن", 51.5074, -0.1278),
    CityEntry("پاریس", "Paris", "باريس", 48.8566, 2.3522),
    CityEntry("برلین", "Berlin", "برلين", 52.5200, 13.4050),
    CityEntry("مسکو", "Moscow", "موسكو", 55.7558, 37.6173),
    CityEntry("نیویورک", "New York", "نيويورك", 40.7128, -74.0060),
    CityEntry("تورنتو", "Toronto", "تورونتو", 43.6532, -79.3832),
    CityEntry("سیدنی", "Sydney", "سيدني", -33.8688, 151.2093)
)

/** Resolves which of the catalog's three languages should be used to display city names. */
private fun currentCatalogLanguage(context: Context): String = LocalePrefs.currentLanguage(context)

private fun CityEntry.nameFor(language: String): String = when (language) {
    "fa" -> fa
    "ar" -> ar
    else -> en
}

/**
 * Returns the built-in city list with names in whichever of the app's three supported
 * languages (Persian, English, Arabic) is currently active — the user's in-app language
 * choice if they've set one, otherwise the system language. Falls back to English for
 * any other language.
 */
fun defaultCities(context: Context): List<City> {
    val language = currentCatalogLanguage(context)
    return CITY_CATALOG.map { entry -> City(entry.nameFor(language), entry.lat, entry.lon) }
}

/**
 * If [lat]/[lon] match one of the built-in catalog entries, returns that entry's name in
 * whichever language is currently active (in-app override, else system) — used to re-localize
 * a *previously saved* selected city after the in-app language changes, since the name
 * persisted to storage is a plain string frozen in whatever language was active when it was
 * saved.
 *
 * Returns null for coordinates that aren't in the catalog (custom cities the user added, or
 * ones picked via the map/geocoding search) — those names are user-owned and must never be
 * overwritten. A small tolerance absorbs floating-point round-tripping through JSON storage.
 */
fun localizedCatalogName(context: Context, lat: Double, lon: Double): String? {
    val entry = CITY_CATALOG.firstOrNull {
        kotlin.math.abs(it.lat - lat) < 0.0001 && kotlin.math.abs(it.lon - lon) < 0.0001
    } ?: return null
    return entry.nameFor(currentCatalogLanguage(context))
}
