package com.kaltura.kflow.presentation.ui.expandableRecyclerView

/**
 * Interface for implementing required methods in a parent list item.
 */
interface ParentListItem {
    /**
     * Getter for the list of this parent list item's child list items.
     *
     *
     * If list is empty, the parent list item has no children.
     *
     * @return A [List] of the children of this [ParentListItem]
     */
    val children: List<Any>

    /**
     * Getter used to determine if this [ParentListItem]'s
     * [android.view.View] should show up initially as expanded.
     *
     * @return true if expanded, false if not
     */
    val isInitiallyExpanded: Boolean
}