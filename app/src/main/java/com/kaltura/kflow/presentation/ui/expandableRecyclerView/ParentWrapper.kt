package com.kaltura.kflow.presentation.ui.expandableRecyclerView

/**
 * Wrapper used to link expanded state with a [ParentListItem].
 */
class ParentWrapper(var parentListItem: ParentListItem) {
    /**
     * Gets the expanded state associated with the [ParentListItem].
     *
     * @return true if expanded, false if not
     */
    /**
     * Sets the expanded state associated with the [ParentListItem].
     *
     * @param expanded true if expanded, false if not
     */
    var isExpanded = false
    /**
     * Gets the [ParentListItem] being wrapped.
     *
     * @return The [ParentListItem] being wrapped
     */

    val isInitiallyExpanded: Boolean
        get() = parentListItem.isInitiallyExpanded

    val childItemList: List<Any>
        get() = parentListItem.children

}