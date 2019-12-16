package com.kaltura.kflow.presentation.ui.expandableRecyclerView

import kotlin.collections.ArrayList

object ExpandableRecyclerAdapterHelper {
    /**
     * Generates a full list of all [ParentListItem] objects and their
     * children, in order.
     *
     * @param parentItemList A list of the `ParentListItem` objects from
     * the [ExpandableRecyclerAdapter]
     * @return A list of all `ParentListItem` objects and their children, expanded
     */
    @JvmStatic
    fun generateParentChildItemList(parentItemList: List<ParentListItem>): ArrayList<Any> {
        val parentWrapperList = arrayListOf<Any>()
        var parentListItem: ParentListItem
        var parentWrapper: ParentWrapper
        val parentListItemCount = parentItemList.size
        for (i in 0 until parentListItemCount) {
            parentListItem = parentItemList[i]
            parentWrapper = ParentWrapper(parentListItem)
            parentWrapperList.add(parentWrapper)
            if (parentWrapper.isInitiallyExpanded) {
                parentWrapper.isExpanded = true
                val childListItemCount = parentWrapper.childItemList.size
                for (j in 0 until childListItemCount) {
                    parentWrapperList.add(parentWrapper.childItemList[j])
                }
            }
        }
        return parentWrapperList
    }
}