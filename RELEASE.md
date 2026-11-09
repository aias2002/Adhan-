# خطوات نشر إصدار جديد لتطبيق Adhan+ (Release Guide)

كلما قمت بتحديث التطبيق، اتبع هذه الخطوات الأربع البسيطة لضمان عمل منظومة التحديثات البرمجية دون أي مشاكل:

---

### الخطوة 1: رفع رقم الإصدار في `app/build.gradle.kts`
افتح ملف `app/build.gradle.kts` وتوجه إلى كتلة `defaultConfig`:
- ارفع `versionCode` برقم واحد دائماً (مثلاً: إذا كان `6` اجعله `7`).
- حدّث `versionName` باسم الإصدار الجديد (مثلاً: `"1.6"`).

```kotlin
defaultConfig {
    applicationId = "com.aistudio.salatglobal.vprtwo"
    minSdk = 24
    targetSdk = 35

    // =========================================================================
    // VERSION CONFIGURATION:
    // With every new update release, ALWAYS increment versionCode (+1)
    // and update versionName to match the version.json manifest.
    // =========================================================================
    versionCode = 7       // ⬅ ارفع هذا الرقم دائماً (+1)
    versionName = "1.6"   // ⬅ اسم الإصدار الجديد
    ...
}
```

---

### الخطوة 2: بناء ملف الـ APK
- قم ببناء حزمة الـ APK بصيغة Release أو Debug من خلال لوحة التصدير أو الأمر:
  ```bash
  gradle assembleRelease
  ```
- أو عبر خيار Export APK في بيئة العمل.

---

### الخطوة 3: رفع ملف APK إلى MediaFire
- ارفع ملف الـ APK الجديد إلى حسابك في MediaFire بالاسم نفسه (`Adhan+.apk`) حتى يظل الرابط ثابتاً:
  `https://www.mediafire.com/file/v3yjntcyqfxlbnr/Adhan%2B.apk/file`

---

### الخطوة 4: تحديث ملف `version.json` في مستودعك (أو الرابط المستضاف)
حدّث ملف `version.json` بنفس القيم المحدثة (التي وضعتها في الخطوة 1):

```json
{
  "versionCode": 7,
  "versionName": "1.6",
  "apkUrl": "https://www.mediafire.com/file/v3yjntcyqfxlbnr/Adhan%2B.apk/file",
  "mirrorUrl": "",
  "changelog": {
    "ar": "تحسينات في الأداء ودقة التنبيهات وإصلاحات عامة.",
    "en": "Performance enhancements, notification improvements and bug fixes."
  },
  "minVersionCode": 1
}
```

> **ملاحظة هامة:**
> التطبيق يقارن `versionCode` كأرقام صحيحة فقط (`remote > installed`).
> بمجرد قيام المستخدم بتثبيت الإصدار الجديد، يختفي إشعار التحديث تلقائياً ولا يعود للظهور نهائياً لنفس الإصدار.
