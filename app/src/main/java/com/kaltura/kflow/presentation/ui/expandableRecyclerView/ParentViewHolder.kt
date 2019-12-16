package com.kaltura.kflow.presentation.ui.expandableRecyclerView

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by alex_lytvynenko on 2019-07-18.
 */
open class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    /**
     * Getter for the [ParentListItemExpandCollapseListener] implemented in
     *
     * @return The [ParentListItemExpandCollapseListener] set in the [ParentViewHolder]
     */
    /**
     * Setter for the [ParentListItemExpandCollapseListener] implemented in
     *
     * @param parentListItemExpandCollapseListener The [ParentListItemExpandCollapseListener] to set on the [ParentViewHolder]
     */
    var parentListItemExpandCollapseListener: ParentListItemExpandCollapseListener? = null
    /**
     * corresponding to this [ParentViewHolder].
     *
     * @return true if expanded, false if not
     */
    /**
     * Setter method for expanded state, used for initialization of expanded state.
     * changes to the state are given in [.onExpansionToggled]
     *
     * @param expanded true if expanded, false if not
     */
    open var isExpanded = false
    var isExpandable = true

    /**
     * implementations to be notified of expand/collapse state change events.
     */
    interface ParentListItemExpandCollapseListener {
        /**
         * Called when a list item is expanded.
         *
         * @param position The index of the item in the list being expanded
         */
        fun onParentListItemExpanded(position: Int)

        /**
         * Called when a list item is collapsed.
         *
         * @param position The index of the item in the list being collapsed
         */
        fun onParentListItemCollapsed(position: Int)
    }

    /**
     * Sets a [android.view.View.OnClickListener] on the entire parent
     * view to trigger expansion.
     */
    fun setMainItemClickToExpand() {
        itemView.setOnClickListener(this)
    }

    /**
     * Callback triggered when expansion state is changed, but not during
     * initialization.
     *
     *
     * Useful for implementing animations on expansion.
     *
     * @param expanded true if view is expanded before expansion is toggled,
     * false if not
     */
    open fun onExpansionToggled(expanded: Boolean) {}

    /**
     * [android.view.View.OnClickListener] to listen for click events on
     * the entire parent [View].
     *
     *
     * Only registered if [.shouldItemViewClickToggleExpansion] is true.
     *
     * @param v The [View] that is the trigger for expansion
     */
    override fun onClick(v: View) {
        if (isExpanded) {
            collapseView()
        } else {
            expandView()
        }
    }

    /**
     * Triggers expansion of the parent.
     */
    protected fun expandView() {
        isExpanded = true
        onExpansionToggled(false)
        if (parentListItemExpandCollapseListener != null) {
            parentListItemExpandCollapseListener!!.onParentListItemExpanded(adapterPosition)
        }
    }

    /**
     * Triggers collapse of the parent.
     */
    protected fun collapseView() {
        isExpanded = false
        onExpansionToggled(true)
        if (parentListItemExpandCollapseListener != null) {
            parentListItemExpandCollapseListener!!.onParentListItemCollapsed(adapterPosition)
        }
    }

}