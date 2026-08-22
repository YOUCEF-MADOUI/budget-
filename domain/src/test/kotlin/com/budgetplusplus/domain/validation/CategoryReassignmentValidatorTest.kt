package com.budgetplusplus.domain.validation

import com.budgetplusplus.core.model.CategoryKind
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryReassignmentValidatorTest {
    @Test fun `used category requires distinct replacement of same kind`() {
        assertFalse(CategoryReassignmentValidator.categoryReplacementIsValid("a", CategoryKind.EXPENSE, 2, null, null))
        assertFalse(CategoryReassignmentValidator.categoryReplacementIsValid("a", CategoryKind.EXPENSE, 2, "b", CategoryKind.INCOME))
        assertTrue(CategoryReassignmentValidator.categoryReplacementIsValid("a", CategoryKind.EXPENSE, 2, "b", CategoryKind.EXPENSE))
    }
    @Test fun `unused category needs no replacement`() { assertTrue(CategoryReassignmentValidator.categoryReplacementIsValid("a", CategoryKind.EXPENSE, 0, null, null)) }
    @Test fun `subcategory replacement stays under same parent`() {
        assertFalse(CategoryReassignmentValidator.subcategoryReplacementIsValid("s1", "c1", 1, "s2", "c2"))
        assertTrue(CategoryReassignmentValidator.subcategoryReplacementIsValid("s1", "c1", 1, "s2", "c1"))
    }
}
