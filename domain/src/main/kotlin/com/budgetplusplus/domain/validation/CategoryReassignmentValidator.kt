package com.budgetplusplus.domain.validation

import com.budgetplusplus.core.model.CategoryKind

object CategoryReassignmentValidator {
    fun categoryReplacementIsValid(sourceId: String, sourceKind: CategoryKind?, usageCount: Int, replacementId: String?, replacementKind: CategoryKind?): Boolean =
        usageCount == 0 || (replacementId != null && replacementId != sourceId && sourceKind != null && replacementKind == sourceKind)

    fun subcategoryReplacementIsValid(sourceId: String, parentId: String?, usageCount: Int, replacementId: String?, replacementParentId: String?): Boolean =
        usageCount == 0 || (replacementId != null && replacementId != sourceId && parentId != null && replacementParentId == parentId)
}
