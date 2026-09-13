## v5.02 — Resilient Bulk Import
- Bulk Import now isolates ordinary per-item creation failures instead of aborting the entire batch.
- A failed item remains visible as `FAILED` with its error message, while later valid items continue importing.
- The final import state reports the number of failed items without losing successful/duplicate/incomplete results.
- Duplicate and incomplete handling remains unchanged.
- Parser metadata projection remains available in the per-item result/UI layer.
- No database schema, learning algorithm, or scheduling changes were introduced in this checkpoint.

## v5.01 — Import metadata projection
- Bulk Import اکنون confidence parser و تعداد breakdown/relationship/variant را برای هر مدخل و به‌صورت aggregate در preview نشان می‌دهد.
- رفتار persistence، duplicate، incomplete و notes تغییر نکرد.
- تست UI state برای projection متادیتای parser اضافه شد.

## v4.97 — Bulk Import result tracking
- Bulk Import اکنون برای تک‌تک مدخل‌ها نتیجه‌ی مستقل نگه می‌دارد: آماده، ناقص، واردشده، تکراری یا خطادار.
- Preview مدخل‌های ناقص را قبل از ورود مشخص می‌کند و بعد از Import، دلیل ردشدن یا خطای هر مدخل نمایش داده می‌شود.
- تکراری داخل همان batch از مدخلی که از قبل در Library وجود داشته تفکیک می‌شود.
- با تغییر متن، نتایج Import قبلی نیز reset می‌شوند تا نتیجه‌ی قدیمی به batch جدید نسبت داده نشود.
- تست UI state برای statusهای جدید اضافه شد.
- الگوریتم یادگیری، scheduling و schema دیتابیس تغییر نکرد.
