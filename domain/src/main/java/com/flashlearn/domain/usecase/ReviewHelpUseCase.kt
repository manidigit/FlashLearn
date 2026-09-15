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
    fun hintFor(sourceText: String): String {
        require(sourceText.isNotBlank()) { "sourceText must not be blank" }
        return "راهنما: به معنای واژه، نقش آن در جمله و بافتی که در آن دیده‌اید فکر کنید."
    }

    fun noteFor(sourceNotes: String?): String? = sourceNotes
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}
