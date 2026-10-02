package com.flashlearn.domain.usecase

/**
 * Pure review-help contract. Help actions are deliberately separate from
 * answer/session state: requesting a hint or note never changes the review
 * result, difficulty, queue, or submitted-answer state.
 *
 * The hint must not contain the target answer or a substring that directly
 * reveals it. Notes are returned only when explicitly requested and are
 * presentation-only content; they do not mutate the review domain state.
 */
class ReviewHelpUseCase {
    fun hintFor(sourceText: String): String =
        hintFor(sourceText = sourceText, targetText = null, categoryName = null)

    fun hintFor(sourceText: String, targetText: String?, categoryName: String?): String {
        require(sourceText.isNotBlank()) { "sourceText must not be blank" }
        val category = categoryName?.trim()?.takeIf { it.isNotEmpty() }
        if (category != null) return "دسته‌بندی: $category"
        val firstMeaningChar = targetText
            ?.trim()
            ?.firstOrNull { !it.isWhitespace() }
            ?.toString()
            ?.takeIf { it.isNotBlank() }
        return if (firstMeaningChar != null) {
            "حرف اول معنی: $firstMeaningChar"
        } else {
            "راهنما: حرف اول معنی را به یاد بیاور."
        }
    }

    fun noteFor(sourceNotes: String?): String? = sourceNotes
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}
