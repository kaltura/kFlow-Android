package com.kaltura.kflow.data.entity

import com.kaltura.kflow.presentation.ui.expandableRecyclerView.ParentListItem

class ParentRecyclerViewItem<P, C : Any>(val parent: P, override val children: ArrayList<C>) : ParentListItem {
    override val isInitiallyExpanded = false
}