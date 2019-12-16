package com.kaltura.kflow.entity

import com.kaltura.kflow.presentation.ui.expandableRecyclerView.ParentListItem

class ParentRecyclerViewItem<P, C : Any>(val parent: P, override val children: List<C>) : ParentListItem {
    override val isInitiallyExpanded = false
}