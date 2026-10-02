package com.flashlearn.domain.parser

/** Configurable marker dictionary for the deterministic vocabulary parser. */
data class ParserMarkers(
    val notes: Set<String>,
    val grammar: Set<String>,
    val breakdown: Set<String>,
    val derivative: Set<String>,
    val variant: Set<String>,
    val relation: Set<String>
) {
    companion object {
        val DEFAULT = ParserMarkers(
            notes = setOf(
                "نکته:", "نکته", "توضیحات:", "توضیحات", "احتمال اشتباه:", "احتمال اشتباه",
                "توجه:", "توجه", "مثال:", "مثال", "Examples:", "Example:", "Note:", "Notes:", "Usage:",
                "Ejemplo:", "Ejemplos:", "Nota:", "Uso:"
            ),
            grammar = setOf(
                "نکته گرامری:", "نکته گرامری", "Grammar:", "Grammar Note:", "Grammatical Note:",
                "Nota gramatical:"
            ),
            breakdown = setOf("تجزیه:", "تجزیه", "Breakdown:", "Breakdown"),
            derivative = setOf("مشتق شده از:", "مشتق شده از", "Derived from:", "Derived from"),
            variant = setOf("Variant:", "Variants:", "حالت دیگر:", "حالت دیگر"),
            relation = setOf("مرتبط:", "مرتبط", "Related:", "Related", "Synonym:", "Synonym:")
        )
    }
}
