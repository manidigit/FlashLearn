package com.flashlearn.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class VocabularyRelationTypeContractTest {
    @Test
    fun frozenRelationTypesAreComplete() {
        assertEquals(
            setOf("USED_IN", "DERIVED_FROM", "INFLECTED_FORM", "SYNONYM", "ANTONYM", "CONTRAST", "EXAMPLE_OF", "RELATED_TO"),
            VocabularyRelationType.entries.map { it.name }.toSet()
        )
    }
}
