# Review Screen Design Specification — v6.14

## Source of truth

The supplied Review reference image is the visual source of truth for the Review setup state.

## Visual contract

- RTL Persian presentation.
- Warm cream page background.
- Dark brown primary text.
- Brown/gold accent for icons, borders, selected states, and the primary action.
- Large right-aligned title: «مرور کلمات».
- Circular back control on the opposite side of the title.
- Small decorative gold ornament under the title.
- Numbered section headings:
  1. حالت پاسخ اجرا
  2. دسته بندی لغات
  3. مرور ویژه
  4. زمانبندی مرور
  5. سطح دشواری کلمات
  6. سطح دشواری آزمون تستی (quiz mode)
  7. تعداد کلمات
- Choice cards are compact, horizontally arranged, rounded, lightly elevated, and use the shared Review token palette.
- Selected choices use the gold accent, warm selected fill, stronger border, and a small check badge.
- Category selection is a single full-width row with category icon and leading navigation affordance.
- Word-count choices are compact rounded pills; the selected count uses a filled gold treatment.
- The filtered-word summary is a bordered surface with a leading filter/tuning icon and centered count/details.
- «شروع مرور» is the full-width primary gold action with a white circular play affordance.
- The shared bottom navigation remains in the shell; when the Review route is active it adopts the Review palette.

## Engineering contract

- ReviewScreen does not own a second independent color/dimension system.
- Review visual values are centralized in FlashLearnThemeTokens.
- ReviewViewModel behavior, selection semantics, scheduling, filtering, quiz/flashcard behavior, persistence, and database contracts are unchanged by this UI checkpoint.
- The screen follows the global app layout direction; no screen-local RTL provider is introduced.
- The design must remain usable in dark appearance through the corresponding token values.

## Verification contract

The v6.14 release identity is versionName 6.14 / versionCode 114.

The checkpoint is not considered fully verified until GitHub Actions passes:
1. Build + Unit Test
2. APK verification/version gate
3. Instrumentation + Upgrade Gate
