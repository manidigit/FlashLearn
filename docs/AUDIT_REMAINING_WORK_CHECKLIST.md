## Issue 10 — Theme Selection

- Status: **implemented; CI pending**
- Root cause: Settings still rendered all available themes as fixed-width cards inside a horizontally scrollable row, which is inefficient for a growing/custom theme catalog and is not a compact select control.
- Fix: replaced the card list with a single dropdown selector while preserving theme IDs, AppViewModel persistence, built-in/custom distinction, and import/export.
- Accessibility/localization: selector and fallback labels are resource-backed in Persian and English; the active theme is explicitly marked.
- Regression: `SettingsThemeDropdownTest`.
- No Room schema/migration or Theme JSON format change.

# FlashLearn — Audit Remaining Work Checklist

این فایل فهرست مواردی است که در ممیزی v6.61 مطرح شدند اما در v6.61.1 به‌طور کامل انجام نشده‌اند. هدف این فایل این است که هر مورد را جداگانه بررسی و پس از تکمیل، با نتیجه و CI سبز علامت‌گذاری کنیم.

وضعیت‌ها: ⬜ انجام نشده | 🔎 نیازمند بررسی دقیق | ✅ تکمیل و تأیید شده

## 1. استخراج کامل رشته‌های Hardcoded UI
- وضعیت: ⬜
- تمام متن‌های hardcoded فارسی/قابل ترجمه در UI بررسی و به strings.xml منتقل شوند.
- در v6.61.1 فقط رشته‌های هدر Review استخراج شدند.

## 2. مهاجرت کامل به Navigation Compose
- وضعیت: ⬜
- جایگزینی الگوی دستی selectedRoute و when در MainActivity با Navigation Compose و back stack واقعی.
- مسیرها، Back، recreation و deep-link/lifecycle behavior بررسی شوند.

## 3. بررسی Scope و Lifecycle تمام ViewModelها
- وضعیت: 🔎
- Activity-scoped بودن ViewModelها بررسی و فقط موارد لازم destination-scoped شوند.
- صرف Activity-scoped بودن به‌تنهایی باگ یا memory leak اثبات نمی‌کند.

## 4. بازبینی و ساده‌سازی سیستم Theme Metrics
- وضعیت: 🔎
- FlashLearnThemeTokens، FlashLearnThemeSpec و metric/layout strategy بررسی شوند.
- اعتبارسنجی metric(name) انجام شده، اما ساده‌سازی معماری هنوز انجام نشده است.

## 5. Density Scaling
- وضعیت: ✅
- override سراسری LocalDensity حذف شد.
- فقط در صورت مشاهده مشکل باقی‌مانده نیاز به بررسی مجدد دارد.

## 6. جایگزینی Hardcoded Padding/Spacing باقی‌مانده
- وضعیت: ⬜
- کل UI برای spacing/padding مستقیم بررسی و موارد مناسب با tokenهای theme جایگزین شوند.
- هدر Review اصلاح شده؛ کل پروژه هنوز audit نشده است.

## 7. RTL/LTR برای متن‌های Latin/Spanish
- وضعیت: 🔎
- مواردی که متن لاتین/اسپانیایی در محیط RTL به LTR نیاز دارد بررسی شوند.
- فقط موارد واقعاً لازم تغییر کنند.

## 8. بررسی Material3 Dynamic Color و Surface Variants
- وضعیت: 🔎
- palette، surface/container roles و dynamic color در Light/Dark بررسی شوند.
- این مورد در ممیزی اولیه موضوع طراحی بود، نه باگ قطعی.

## 9. اعتبارسنجی کامل Custom Theme JSON
- وضعیت: 🔎
- parser/schema/validation برای مقادیر ناقص، نوع نادرست، رنگ‌ها، metrics و fallback بررسی شود.
- metric validation انجام شده، اما audit کامل JSON انجام نشده است.

## 10. SavedStateHandle / Navigation State — تکمیل معماری
- وضعیت: 🔎
- SavedStateHandle برای route/concept اضافه شده، اما جایگزین کامل Navigation Compose نیست.
- بعد از تصمیم درباره Navigation Compose، state restoration نهایی بازبینی شود.

## 11. ProGuard / R8 Rules
- وضعیت: 🔎
- R8/ProGuard برای serialization/reflection/DI/Compose و کتابخانه‌های پروژه audit شود.
- Release R8 build در CI سبز بوده و فعلاً خطای اثبات‌شده‌ای وجود ندارد؛ این مورد audit تکمیلی است.

## 12. Error Handling و UI State Robustness
- وضعیت: ⬜
- مسیرهای خطا، loading، empty state و failure propagation در ViewModel/UseCase/UI بررسی شوند.

## 13. تست‌های UI برای Theme / Direction / Dark-Light
- وضعیت: ⬜
- UI tests برای Light/Dark، RTL/LTR و theme/accent variations تکمیل شوند.
- تست‌ها باید پایدار و قابل تکرار در CI باشند.

## 14. خوانایی و ساختار فایل‌های فشرده
- وضعیت: ⬜
- AppViewModel، FlashLearnThemeTokens و FlashLearnThemeSpec بازبینی شوند.
- فقط refactorهای بدون تغییر رفتار انجام شوند.

## Completed in v6.61.1
- ✅ Color compositing / Color.compositeOver
- ✅ Accent color application
- ✅ Global density override removal
- ✅ metric(name) validation
- ✅ Review concurrency Mutex
- ✅ Initial SavedStateHandle support
- ✅ Review header localization
- ✅ Review header theme-token spacing
- ✅ CI regression fix
- ✅ CI verification on green run

## Item 1 completion note
- Extracted user-visible Compose strings from About, Add Word, Bulk Import, Backup/Restore, Library, Category Selection, Review, Needs Review, Progress, Settings, navigation shell/components, and MainActivity.
- Added Persian and English Android string resources, including formatted/dynamic strings and accessibility labels.
- Kept Persian category-name matching in `CategorySelectionScreen` because those literals are classification data used by icon-selection logic, not rendered UI copy.
- Final CI verification: GREEN on workflow run `36481473544` (Build + Unit Test and Instrumentation + Upgrade Gate).

## UI language application fix
- The Settings screen previously displayed “Persian” but did not actually apply an app locale; Android therefore used the device locale and could render the English `values-en` resources.
- Added persisted app-language selection, locale-wrapped Activity resources, and a functional Settings language toggle.
- Default remains Persian.
- CI run `36484231210` is GREEN.

## v6.68 — A1 Duplicate merge state preservation
- وضعیت: 🔎 نیازمند CI
- Root cause: duplicate LearningState/DifficultyState was not transferred when the survivor had no corresponding state; the survivor state snapshot could also become stale across duplicate iterations.
- Fix: merge helpers remap missing state to the survivor concept and refresh the survivor state after each merge; existing accumulated correct/wrong totals remain additive when both states exist.
- Regression test: duplicateLearningAndDifficulty_areTransferredWhenSurvivorHasNoState.
- Direct prerequisite fixed in the same release branch: remaining v6.67 Pronunciation/Example references were removed from data mapping and duplicate translation construction so the v8 Content model compiles.
- CI: pending.

## v6.69 — Refresh cleans Spanish word edges
- وضعیت: 🔎 نیازمند CI
- Root cause: Spanish entries stored with stray symbols (e.g. `*`) at the start/end; Refresh never cleaned Spanish text.
- Fix: content data version 4 in RefreshDataUseCase strips edge noise from `es` rows and rebuilds canonicalKey; Persian text and Spanish punctuation untouched.
- Regression tests: spanishEdgeNoiseIsRemovedAndCanonicalKeyRebuilt, spanishRowMadeOnlyOfNoiseIsLeftUntouched.
- CI: pending.

## Workflow
هر بار فقط یک مورد انتخاب شود: کد فعلی بررسی شود، فقط همان مورد اصلاح شود، تست‌ها اجرا شوند، CI تا GREEN دنبال شود، و همین فایل با نتیجه و CI به‌روزرسانی شود.