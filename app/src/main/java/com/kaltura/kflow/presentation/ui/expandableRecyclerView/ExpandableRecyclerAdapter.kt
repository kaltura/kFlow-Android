package com.kaltura.kflow.presentation.ui.expandableRecyclerView

import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.kflow.presentation.ui.expandableRecyclerView.ExpandableRecyclerAdapterHelper.generateParentChildItemList
import com.kaltura.kflow.presentation.ui.expandableRecyclerView.ParentViewHolder.ParentListItemExpandCollapseListener
import java.util.*
import kotlin.collections.ArrayList

abstract class ExpandableRecyclerAdapter<PVH : ParentViewHolder, CVH : ChildViewHolder>(val parentItemList: List<ParentListItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ParentListItemExpandCollapseListener {
    /**
     * A [List] of all currently expanded [ParentListItem] objects
     * and their children, in order. Changes to this list should be made through the add/remove methods
     * available in [ExpandableRecyclerAdapter]
     */
    protected var itemList: ArrayList<Any> = generateParentChildItemList(parentItemList)

    /**
     * Gets the list of ParentItems that is backing this adapter.
     * Changes can be made to the list and the adapter notified via the
     * [.notifyParentItemInserted]
     * [.notifyParentItemRemoved]
     * [.notifyParentItemChanged]
     * [.notifyParentItemRangeInserted]
     * [.notifyChildItemInserted]
     * [.notifyChildItemRemoved]
     * [.notifyChildItemChanged]
     * methods.
     *
     *
     * @return The list of ParentListItems that this adapter represents
     */
    private var expandCollapseListener: ExpandCollapseListener? = null
    private val attachedRecyclerViewPool: MutableList<RecyclerView> = ArrayList()

    /**
     * Allows objects to register themselves as expand/collapse listeners to be
     * notified of change events.
     *
     *
     * Implement this in your [android.app.Activity] or [android.app.Fragment]
     * to receive these callbacks.
     */
    interface ExpandCollapseListener {
        /**
         * Called when a list item is expanded.
         *
         * @param position The index of the item in the list being expanded
         */
        fun onListItemExpanded(position: Int)

        /**
         * Called when a list item is collapsed.
         *
         * @param position The index of the item in the list being collapsed
         */
        fun onListItemCollapsed(position: Int)
    }

    /**
     * Implementation of Adapter.onCreateViewHolder(ViewGroup, int)
     * that determines if the list item is a parent or a child and calls through
     * to the appropriate implementation of either [.onCreateParentViewHolder]
     * or [.onCreateChildViewHolder].
     *
     * @param viewGroup The [ViewGroup] into which the new [android.view.View]
     * will be added after it is bound to an adapter position.
     * @param viewType The view type of the new `android.view.View`.
     * @return A new RecyclerView.ViewHolder
     * that holds a `android.view.View` of the given view type.
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_PARENT -> {
                val pvh = onCreateParentViewHolder(viewGroup)
                pvh.parentListItemExpandCollapseListener = this
                pvh
            }
            TYPE_CHILD -> onCreateChildViewHolder(viewGroup)
            else -> throw IllegalStateException("Incorrect ViewType found")
        }
    }

    /**
     * Implementation of Adapter.onBindViewHolder(RecyclerView.ViewHolder, int)
     * that determines if the list item is a parent or a child and calls through
     * to the appropriate implementation of either [.onBindParentViewHolder]
     * or [.onBindChildViewHolder].
     *
     * @param holder The RecyclerView.ViewHolder to bind data to
     * @param position The index in the list at which to bind
     * @throws IllegalStateException if the item in the list is either null or
     * not of type [ParentListItem]
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val listItem = getListItem(position)
        if (listItem is ParentWrapper) {
            val parentViewHolder = holder as PVH
            if (parentViewHolder.isExpandable) {
                parentViewHolder.setMainItemClickToExpand()
            }
            parentViewHolder.isExpanded = listItem.isExpanded
            onBindParentViewHolder(parentViewHolder, position, listItem.parentListItem)
        } else {
            checkNotNull(listItem) { "Incorrect ViewHolder found" }
            onBindChildViewHolder(holder as CVH, position, listItem)
        }
    }

    /**
     * Callback called from [.onCreateViewHolder] when
     * the list item created is a parent.
     *
     * @param parentViewGroup The [ViewGroup] in the list for which a [PVH]
     * is being created
     * @return A `PVH` corresponding to the [ParentListItem] with
     * the `ViewGroup` parentViewGroup
     */
    abstract fun onCreateParentViewHolder(parentViewGroup: ViewGroup): PVH

    /**
     * Callback called from [.onCreateViewHolder] when
     * the list item created is a child.
     *
     * @param childViewGroup The [ViewGroup] in the list for which a [CVH]
     * is being created
     * @return A `CVH` corresponding to the child list item with the
     * `ViewGroup` childViewGroup
     */
    abstract fun onCreateChildViewHolder(childViewGroup: ViewGroup): CVH

    /**
     * Callback called from onBindViewHolder(RecyclerView.ViewHolder, int)
     * when the list item bound to is a parent.
     *
     *
     * Bind data to the [PVH] here.
     *
     * @param parentViewHolder The `PVH` to bind data to
     * @param position The index in the list at which to bind
     * @param parentListItem The [ParentListItem] which holds the data to
     * be bound to the `PVH`
     */
    abstract fun onBindParentViewHolder(parentViewHolder: PVH, position: Int, parentListItem: ParentListItem)

    /**
     * Callback called from onBindViewHolder(RecyclerView.ViewHolder, int)
     * when the list item bound to is a child.
     *
     *
     * Bind data to the [CVH] here.
     *
     * @param childViewHolder The `CVH` to bind data to
     * @param position The index in the list at which to bind
     * @param childListItem The child list item which holds that data to be
     * bound to the `CVH`
     */
    abstract fun onBindChildViewHolder(childViewHolder: CVH, position: Int, childListItem: Any?)

    /**
     * Gets the number of parent and child objects currently expanded.
     *
     * @return The size of [.mItemList]
     */
    override fun getItemCount() = itemList.size

    /**
     * Gets the view type of the item at the given position.
     *
     * @param position The index in the list to get the view type of
     * @return {@value #TYPE_PARENT} for [ParentListItem] and {@value #TYPE_CHILD}
     * for child list items
     * @throws IllegalStateException if the item at the given position in the list is null
     */
    override fun getItemViewType(position: Int): Int {
        return when (getListItem(position)) {
            is ParentWrapper -> TYPE_PARENT
            null -> throw IllegalStateException("Null object added")
            else -> TYPE_CHILD
        }
    }

    /**
     *
     *
     * Called when a [ParentListItem] is triggered to expand.
     *
     * @param position The index of the item in the list being expanded
     */
    override fun onParentListItemExpanded(position: Int) {
        val listItem = getListItem(position)
        if (listItem is ParentWrapper) {
            expandParentListItem(listItem, position, true)
        }
    }

    /**
     *
     *
     * Called when a [ParentListItem] is triggered to collapse.
     *
     * @param position The index of the item in the list being collapsed
     */
    override fun onParentListItemCollapsed(position: Int) {
        val listItem = getListItem(position)
        if (listItem is ParentWrapper) {
            collapseParentListItem(listItem, position, true)
        }
    }

    /**
     * Implementation of Adapter#onAttachedToRecyclerView(RecyclerView).
     *
     *
     * Called when this [ExpandableRecyclerAdapter] is attached to a RecyclerView.
     *
     * @param recyclerView The `RecyclerView` this `ExpandableRecyclerAdapter`
     * is being attached to
     */
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        attachedRecyclerViewPool.add(recyclerView)
    }

    /**
     * Implementation of Adapter.onDetachedFromRecyclerView(RecyclerView)
     *
     *
     * Called when this ExpandableRecyclerAdapter is detached from a RecyclerView.
     *
     * @param recyclerView The `RecyclerView` this `ExpandableRecyclerAdapter`
     * is being detached from
     */
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        attachedRecyclerViewPool.remove(recyclerView)
    }

    fun setExpandCollapseListener(expandCollapseListener: ExpandCollapseListener?) {
        this.expandCollapseListener = expandCollapseListener
    }
    // region Programmatic Expansion/Collapsing
    /**
     * Expands the parent with the specified index in the list of parents.
     *
     * @param parentIndex The index of the parent to expand
     */
    fun expandParent(parentIndex: Int) {
        val parentWrapperIndex = getParentWrapperIndex(parentIndex)
        val listItem = getListItem(parentWrapperIndex)
        val parentWrapper: ParentWrapper
        parentWrapper = if (listItem is ParentWrapper) listItem else return
        expandViews(parentWrapper, parentWrapperIndex)
    }

    /**
     * Expands the parent associated with a specified [ParentListItem] in
     * the list of parents.
     *
     * @param parentListItem The `ParentListItem` of the parent to expand
     */
    fun expandParent(parentListItem: ParentListItem) {
        getParentWrapper(parentListItem)?.let {
            val parentWrapperIndex = itemList.indexOf(it)
            if (parentWrapperIndex == -1) return
            expandViews(it, parentWrapperIndex)
        }
    }

    /**
     * Expands all parents in a range of indices in the list of parents.
     *
     * @param startParentIndex The index at which to to start expanding parents
     * @param parentCount The number of parents to expand
     */
    fun expandParentRange(startParentIndex: Int, parentCount: Int) {
        val endParentIndex = startParentIndex + parentCount
        for (i in startParentIndex until endParentIndex) expandParent(i)
    }

    /**
     * Expands all parents in the list.
     */
    fun expandAllParents() {
        for (parentListItem in parentItemList) expandParent(parentListItem)
    }

    /**
     * Collapses the parent with the specified index in the list of parents.
     *
     * @param parentIndex The index of the parent to collapse
     */
    fun collapseParent(parentIndex: Int) {
        val parentWrapperIndex = getParentWrapperIndex(parentIndex)
        val listItem = getListItem(parentWrapperIndex)
        val parentWrapper: ParentWrapper
        parentWrapper = if (listItem is ParentWrapper) listItem else return
        collapseViews(parentWrapper, parentWrapperIndex)
    }

    /**
     * Collapses the parent associated with a specified [ParentListItem] in
     * the list of parents.
     *
     * @param parentListItem The `ParentListItem` of the parent to collapse
     */
    fun collapseParent(parentListItem: ParentListItem) {
        getParentWrapper(parentListItem)?.let {
            val parentWrapperIndex = itemList.indexOf(it)
            if (parentWrapperIndex == -1) return
            collapseViews(it, parentWrapperIndex)
        }
    }

    /**
     * Collapses all parents in a range of indices in the list of parents.
     *
     * @param startParentIndex The index at which to to start collapsing parents
     * @param parentCount The number of parents to collapse
     */
    fun collapseParentRange(startParentIndex: Int, parentCount: Int) {
        val endParentIndex = startParentIndex + parentCount
        for (i in startParentIndex until endParentIndex) collapseParent(i)
    }

    /**
     * Collapses all parents in the list.
     */
    fun collapseAllParents() {
        for (parentListItem in parentItemList) collapseParent(parentListItem)
    }

    /**
     * Stores the expanded state map across state loss.
     *
     *
     * Should be called from [Activity.onSaveInstanceState] in
     * the [Activity] that hosts the RecyclerView that this
     * [ExpandableRecyclerAdapter] is attached to.
     *
     *
     * This will make sure to add the expanded state map as an extra to the
     * instance state bundle to be used in [.onRestoreInstanceState].
     *
     * @param savedInstanceState The `Bundle` into which to store the
     * expanded state map
     */
    fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putSerializable(EXPANDED_STATE_MAP, generateExpandedStateMap())
    }

    /**
     * Fetches the expandable state map from the saved instance state [Bundle]
     * and restores the expanded states of all of the list items.
     *
     *
     * Should be called from [Activity.onRestoreInstanceState] in
     * the [Activity] that hosts the RecyclerView that this
     * [ExpandableRecyclerAdapter] is attached to.
     *
     *
     * Assumes that the list of parent list items is the same as when the saved
     * instance state was stored.
     *
     * @param savedInstanceState The `Bundle` from which the expanded
     * state map is loaded
     */
    fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null || !savedInstanceState.containsKey(EXPANDED_STATE_MAP)) {
            return
        }
        val expandedStateMap = savedInstanceState.getSerializable(EXPANDED_STATE_MAP) as HashMap<Int, Boolean>
        val parentWrapperList = arrayListOf<Any>()
        var parentListItem: ParentListItem
        var parentWrapper: ParentWrapper
        val parentListItemCount = parentItemList.size
        for (i in 0 until parentListItemCount) {
            parentListItem = parentItemList[i]
            parentWrapper = ParentWrapper(parentListItem)
            parentWrapperList.add(parentWrapper)
            if (expandedStateMap.containsKey(i)) {
                val expanded = expandedStateMap[i]!!
                if (expanded) {
                    parentWrapper.isExpanded = true
                    val childListItemCount = parentWrapper.childItemList.size
                    for (j in 0 until childListItemCount) {
                        parentWrapperList.add(parentWrapper.childItemList[j])
                    }
                }
            }
        }
        itemList = parentWrapperList
        notifyDataSetChanged()
    }

    /**
     * Gets the list item held at the specified adapter position.
     *
     * @param position The index of the list item to return
     * @return The list item at the specified position
     */
    protected fun getListItem(position: Int): Any? {
        val indexInRange = position >= 0 && position < itemList.size
        return if (indexInRange) itemList[position] else null
    }

    /**
     * Calls through to the ParentViewHolder to expand views for each
     * RecyclerView the specified parent is a child of.
     *
     * These calls to the ParentViewHolder are made so that animations can be
     * triggered at the ViewHolder level.
     *
     * @param parentIndex The index of the parent to expand
     */
    private fun expandViews(parentWrapper: ParentWrapper, parentIndex: Int) {
        var viewHolder: PVH?
        for (recyclerView in attachedRecyclerViewPool) {
            viewHolder = recyclerView.findViewHolderForAdapterPosition(parentIndex) as PVH?
            if (viewHolder != null && !viewHolder.isExpanded) {
                viewHolder.isExpanded = true
                viewHolder.onExpansionToggled(false)
            }
            expandParentListItem(parentWrapper, parentIndex, false)
        }
    }

    /**
     * Calls through to the ParentViewHolder to collapse views for each
     * RecyclerView a specified parent is a child of.
     *
     * These calls to the ParentViewHolder are made so that animations can be
     * triggered at the ViewHolder level.
     *
     * @param parentIndex The index of the parent to collapse
     */
    private fun collapseViews(parentWrapper: ParentWrapper, parentIndex: Int) {
        var viewHolder: PVH?
        for (recyclerView in attachedRecyclerViewPool) {
            viewHolder = recyclerView.findViewHolderForAdapterPosition(parentIndex) as PVH
            if (viewHolder.isExpanded) {
                viewHolder.isExpanded = false
                viewHolder.onExpansionToggled(true)
            }
            collapseParentListItem(parentWrapper, parentIndex, false)
        }
    }

    /**
     * Expands a specified parent item. Calls through to the
     * ExpandCollapseListener and adds children of the specified parent to the
     * total list of items.
     *
     * @param parentWrapper The ParentWrapper of the parent to expand
     * @param parentIndex The index of the parent to expand
     * @param expansionTriggeredByListItemClick true if expansion was triggered
     * by a click event, false otherwise.
     */
    private fun expandParentListItem(parentWrapper: ParentWrapper, parentIndex: Int, expansionTriggeredByListItemClick: Boolean) {
        if (!parentWrapper.isExpanded) {
            parentWrapper.isExpanded = true
            val childItemList = parentWrapper.childItemList
            val childListItemCount = childItemList.size
            for (i in 0 until childListItemCount) {
                itemList.add(parentIndex + i + 1, childItemList[i])
            }
            notifyItemRangeInserted(parentIndex + 1, childListItemCount)
            if (expansionTriggeredByListItemClick && expandCollapseListener != null) {
                val expandedCountBeforePosition = getExpandedItemCount(parentIndex)
                expandCollapseListener!!.onListItemExpanded(parentIndex - expandedCountBeforePosition)
            }
        }
    }

    /**
     * Collapses a specified parent item. Calls through to the
     * ExpandCollapseListener and adds children of the specified parent to the
     * total list of items.
     *
     * @param parentWrapper The ParentWrapper of the parent to collapse
     * @param parentIndex The index of the parent to collapse
     * @param collapseTriggeredByListItemClick true if expansion was triggered
     * by a click event, false otherwise.
     */
    private fun collapseParentListItem(parentWrapper: ParentWrapper, parentIndex: Int, collapseTriggeredByListItemClick: Boolean) {
        if (parentWrapper.isExpanded) {
            parentWrapper.isExpanded = false
            val childItemList = parentWrapper.childItemList
            val childListItemCount = childItemList.size
            for (i in childListItemCount - 1 downTo 0) {
                itemList.removeAt(parentIndex + i + 1)
            }
            notifyItemRangeRemoved(parentIndex + 1, childListItemCount)
            if (collapseTriggeredByListItemClick && expandCollapseListener != null) {
                val expandedCountBeforePosition = getExpandedItemCount(parentIndex)
                expandCollapseListener!!.onListItemCollapsed(parentIndex - expandedCountBeforePosition)
            }
        }
    }

    /**
     * Gets the number of expanded child list items before the specified position.
     *
     * @param position The index before which to return the number of expanded
     * child list items
     * @return The number of expanded child list items before the specified position
     */
    private fun getExpandedItemCount(position: Int): Int {
        if (position == 0) {
            return 0
        }
        var expandedCount = 0
        for (i in 0 until position) {
            val listItem = getListItem(i)
            if (listItem !is ParentWrapper) {
                expandedCount++
            }
        }
        return expandedCount
    }
    // endregion
// region Data Manipulation
    /**
     * Notify any registered observers that the ParentListItem reflected at `parentPosition`
     * has been newly inserted. The ParentListItem previously at `parentPosition` is now at
     * position `parentPosition + 1`.
     *
     *
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.
     *
     * @param parentPosition Position of the newly inserted ParentListItem in the data set, relative
     * to list of ParentListItems only.
     *
     * @see .notifyParentItemRangeInserted
     */
    fun notifyParentItemInserted(parentPosition: Int) {
        val parentListItem = parentItemList[parentPosition]
        val wrapperIndex: Int = if (parentPosition < parentItemList.size - 1) {
            getParentWrapperIndex(parentPosition)
        } else {
            itemList.size
        }
        val sizeChanged = addParentWrapper(wrapperIndex, parentListItem)
        notifyItemRangeInserted(wrapperIndex, sizeChanged)
    }

    /**
     * Notify any registered observers that the currently reflected `itemCount`
     * ParentListItems starting at `parentPositionStart` have been newly inserted.
     * The ParentListItems previously located at `parentPositionStart` and beyond
     * can now be found starting at position `parentPositionStart + itemCount`.
     *
     *
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPositionStart Position of the first ParentListItem that was inserted, relative
     * to list of ParentListItems only.
     * @param itemCount Number of items inserted
     *
     * @see .notifyParentItemInserted
     */
    fun notifyParentItemRangeInserted(parentPositionStart: Int, itemCount: Int) {
        val initialWrapperIndex: Int = if (parentPositionStart < parentItemList.size - itemCount) {
            getParentWrapperIndex(parentPositionStart)
        } else {
            itemList.size
        }
        var sizeChanged = 0
        var wrapperIndex = initialWrapperIndex
        var changed: Int
        val parentPositionEnd = parentPositionStart + itemCount
        for (i in parentPositionStart until parentPositionEnd) {
            val parentListItem = parentItemList[i]
            changed = addParentWrapper(wrapperIndex, parentListItem)
            wrapperIndex += changed
            sizeChanged += changed
        }
        notifyItemRangeInserted(initialWrapperIndex, sizeChanged)
    }

    private fun addParentWrapper(wrapperIndex: Int, parentListItem: ParentListItem): Int {
        var sizeChanged = 1
        val parentWrapper = ParentWrapper(parentListItem)
        itemList.add(wrapperIndex, parentWrapper)
        if (parentWrapper.isInitiallyExpanded) {
            parentWrapper.isExpanded = true
            val childItemList = parentWrapper.childItemList
            itemList.addAll(wrapperIndex + sizeChanged, childItemList)
            sizeChanged += childItemList.size
        }
        return sizeChanged
    }

    /**
     * Notify any registered observers that the ParentListItem previously located at `parentPosition`
     * has been removed from the data set. The ParentListItems previously located at and after
     * `parentPosition` may now be found at `oldPosition - 1`.
     *
     *
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPosition Position of the ParentListItem that has now been removed, relative
     * to list of ParentListItems only.
     */
    fun notifyParentItemRemoved(parentPosition: Int) {
        val wrapperIndex = getParentWrapperIndex(parentPosition)
        val sizeChanged = removeParentWrapper(wrapperIndex)
        notifyItemRangeRemoved(wrapperIndex, sizeChanged)
    }

    /**
     * Notify any registered observers that the `itemCount` ParentListItems previously located
     * at `parentPositionStart` have been removed from the data set. The ParentListItems
     * previously located at and after `parentPositionStart + itemCount` may now be found at
     * `oldPosition - itemCount`.
     *
     *
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPositionStart The previous position of the first ParentListItem that was
     * removed, relative to list of ParentListItems only.
     * @param itemCount Number of ParentListItems removed from the data set
     */
    fun notifyParentItemRangeRemoved(parentPositionStart: Int, itemCount: Int) {
        var sizeChanged = 0
        val wrapperIndex = getParentWrapperIndex(parentPositionStart)
        for (i in 0 until itemCount) {
            sizeChanged += removeParentWrapper(wrapperIndex)
        }
        notifyItemRangeRemoved(wrapperIndex, sizeChanged)
    }

    private fun removeParentWrapper(parentWrapperIndex: Int): Int {
        var sizeChanged = 1
        val parentWrapper = itemList.removeAt(parentWrapperIndex) as ParentWrapper?
        if (parentWrapper!!.isExpanded) {
            val childListSize = parentWrapper.childItemList.size
            for (i in 0 until childListSize) {
                itemList.removeAt(parentWrapperIndex)
                sizeChanged++
            }
        }
        return sizeChanged
    }

    /**
     * Notify any registered observers that the ParentListItem at `parentPosition` has changed.
     * This will also trigger an item changed for children of the ParentList specified.
     *
     *
     * This is an item change event, not a structural change event. It indicates that any
     * reflection of the data at `parentPosition` is out of date and should be updated.
     * The ParentListItem at `parentPosition` retains the same identity. This means
     * the number of children must stay the same.
     *
     * @param parentPosition Position of the item that has changed
     */
    fun notifyParentItemChanged(parentPosition: Int) {
        val parentListItem = parentItemList[parentPosition]
        val wrapperIndex = getParentWrapperIndex(parentPosition)
        val sizeChanged = changeParentWrapper(wrapperIndex, parentListItem)
        notifyItemRangeChanged(wrapperIndex, sizeChanged)
    }

    /**
     * Notify any registered observers that the `itemCount` ParentListItems starting
     * at `parentPositionStart` have changed. This will also trigger an item changed
     * for children of the ParentList specified.
     *
     *
     * This is an item change event, not a structural change event. It indicates that any
     * reflection of the data in the given position range is out of date and should be updated.
     * The ParentListItems in the given range retain the same identity. This means
     * the number of children must stay the same.
     *
     * @param parentPositionStart Position of the item that has changed
     * @param itemCount Number of ParentListItems changed in the dataset
     */
    fun notifyParentItemRangeChanged(parentPositionStart: Int, itemCount: Int) {
        var parentPositionStart = parentPositionStart
        val initialWrapperIndex = getParentWrapperIndex(parentPositionStart)
        var wrapperIndex = initialWrapperIndex
        var sizeChanged = 0
        var changed: Int
        var parentListItem: ParentListItem
        for (j in 0 until itemCount) {
            parentListItem = parentItemList[parentPositionStart]
            changed = changeParentWrapper(wrapperIndex, parentListItem)
            sizeChanged += changed
            wrapperIndex += changed
            parentPositionStart++
        }
        notifyItemRangeChanged(initialWrapperIndex, sizeChanged)
    }

    private fun changeParentWrapper(wrapperIndex: Int, parentListItem: ParentListItem): Int {
        val parentWrapper = itemList[wrapperIndex] as ParentWrapper?
        parentWrapper!!.parentListItem = parentListItem
        var sizeChanged = 1
        if (parentWrapper.isExpanded) {
            val childItems = parentWrapper.childItemList
            val childListSize = childItems.size
            var child: Any?
            for (i in 0 until childListSize) {
                child = childItems[i]
                itemList[wrapperIndex + i + 1] = child
                sizeChanged++
            }
        }
        return sizeChanged
    }

    /**
     * Notify any registered observers that the ParentListItem and it's child list items reflected at
     * `fromParentPosition` has been moved to `toParentPosition`.
     *
     *
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.
     *
     * @param fromParentPosition Previous position of the ParentListItem, relative to list of
     * ParentListItems only.
     * @param toParentPosition New position of the ParentListItem, relative to list of
     * ParentListItems only.
     */
    fun notifyParentItemMoved(fromParentPosition: Int, toParentPosition: Int) {
        val fromWrapperIndex = getParentWrapperIndex(fromParentPosition)
        val fromParentWrapper = itemList[fromWrapperIndex] as ParentWrapper
        // If the parent is collapsed we can take advantage of notifyItemMoved otherwise
// we are forced to do a "manual" move by removing and then adding the parent + children
// (no notifyItemRangeMovedAvailable)
        val isCollapsed = !fromParentWrapper.isExpanded
        val isExpandedNoChildren = !isCollapsed && fromParentWrapper.childItemList.isEmpty()
        if (isCollapsed || isExpandedNoChildren) {
            val toWrapperIndex = getParentWrapperIndex(toParentPosition)
            val toParentWrapper = itemList[toWrapperIndex] as ParentWrapper
            itemList.removeAt(fromWrapperIndex)
            var childOffset = 0
            if (toParentWrapper.isExpanded) {
                childOffset = toParentWrapper.childItemList.size
            }
            itemList.add(toWrapperIndex + childOffset, fromParentWrapper)
            notifyItemMoved(fromWrapperIndex, toWrapperIndex + childOffset)
        } else { // Remove the parent and children
            var sizeChanged = 0
            val childListSize = fromParentWrapper.childItemList.size
            for (i in 0 until childListSize + 1) {
                itemList.removeAt(fromWrapperIndex)
                sizeChanged++
            }
            notifyItemRangeRemoved(fromWrapperIndex, sizeChanged)
            // Add the parent and children at new position
            var toWrapperIndex = getParentWrapperIndex(toParentPosition)
            var childOffset = 0
            if (toWrapperIndex != -1) {
                val toParentWrapper = itemList[toWrapperIndex] as ParentWrapper?
                if (toParentWrapper!!.isExpanded) {
                    childOffset = toParentWrapper.childItemList.size
                }
            } else {
                toWrapperIndex = itemList.size
            }
            itemList.add(toWrapperIndex + childOffset, fromParentWrapper)
            val childItemList = fromParentWrapper.childItemList
            sizeChanged = childItemList.size + 1
            itemList.addAll(toWrapperIndex + childOffset + 1, listOf(childItemList))
            notifyItemRangeInserted(toWrapperIndex + childOffset, sizeChanged)
        }
    }

    /**
     * Notify any registered observers that the ParentListItem reflected at `parentPosition`
     * has a child list item that has been newly inserted at `childPosition`.
     * The child list item previously at `childPosition` is now at
     * position `childPosition + 1`.
     *
     *
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.
     *
     * @param parentPosition Position of the ParentListItem which has been added a child, relative
     * to list of ParentListItems only.
     * @param childPosition Position of the child object that has been inserted, relative to children
     * of the ParentListItem specified by `parentPosition` only.
     */
    fun notifyChildItemInserted(parentPosition: Int, childPosition: Int) {
        val parentWrapperIndex = getParentWrapperIndex(parentPosition)
        val parentWrapper = itemList[parentWrapperIndex] as ParentWrapper?
        if (parentWrapper!!.isExpanded) {
            val parentListItem = parentItemList[parentPosition]
            val child: Any = parentListItem.children[childPosition]
            itemList.add(parentWrapperIndex + childPosition + 1, child)
            notifyItemInserted(parentWrapperIndex + childPosition + 1)
        }
    }

    /**
     * Notify any registered observers that the ParentListItem reflected at `parentPosition`
     * has `itemCount` child list items that have been newly inserted at `childPositionStart`.
     * The child list item previously at `childPositionStart` and beyond are now at
     * position `childPositionStart + itemCount`.
     *
     *
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.
     *
     * @param parentPosition Position of the ParentListItem which has been added a child, relative
     * to list of ParentListItems only.
     * @param childPositionStart Position of the first child object that has been inserted,
     * relative to children of the ParentListItem specified by
     * `parentPosition` only.
     * @param itemCount number of children inserted
     */
    fun notifyChildItemRangeInserted(parentPosition: Int, childPositionStart: Int, itemCount: Int) {
        val parentWrapperIndex = getParentWrapperIndex(parentPosition)
        val parentWrapper = itemList[parentWrapperIndex] as ParentWrapper?
        if (parentWrapper!!.isExpanded) {
            val parentListItem = parentItemList[parentPosition]
            val childList: List<Any> = parentListItem.children
            var child: Any
            for (i in 0 until itemCount) {
                child = childList[childPositionStart + i]
                itemList.add(parentWrapperIndex + childPositionStart + i + 1, child)
            }
            notifyItemRangeInserted(parentWrapperIndex + childPositionStart + 1, itemCount)
        }
    }

    /**
     * Notify any registered observers that the ParentListItem located at `parentPosition`
     * has a child list item that has been removed from the data set, previously located at `childPosition`.
     * The child list item previously located at and after `childPosition` may
     * now be found at `childPosition - 1`.
     *
     *
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPosition Position of the ParentListItem which has a child removed from, relative
     * to list of ParentListItems only.
     * @param childPosition Position of the child object that has been removed, relative to children
     * of the ParentListItem specified by `parentPosition` only.
     */
    fun notifyChildItemRemoved(parentPosition: Int, childPosition: Int) {
        val parentWrapperIndex = getParentWrapperIndex(parentPosition)
        val parentWrapper = itemList[parentWrapperIndex] as ParentWrapper?
        if (parentWrapper!!.isExpanded) {
            itemList.removeAt(parentWrapperIndex + childPosition + 1)
            notifyItemRemoved(parentWrapperIndex + childPosition + 1)
        }
    }

    /**
     * Notify any registered observers that the ParentListItem located at `parentPosition`
     * has `itemCount` child list items that have been removed from the data set, previously
     * located at `childPositionStart` onwards. The child list item previously located at and
     * after `childPositionStart` may now be found at `childPositionStart - itemCount`.
     *
     *
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPosition Position of the ParentListItem which has a child removed from, relative
     * to list of ParentListItems only.
     * @param childPositionStart Position of the first child object that has been removed, relative
     * to children of the ParentListItem specified by
     * `parentPosition` only.
     * @param itemCount number of children removed
     */
    fun notifyChildItemRangeRemoved(parentPosition: Int, childPositionStart: Int, itemCount: Int) {
        val parentWrapperIndex = getParentWrapperIndex(parentPosition)
        val parentWrapper = itemList[parentWrapperIndex] as ParentWrapper?
        if (parentWrapper!!.isExpanded) {
            for (i in 0 until itemCount) {
                itemList.removeAt(parentWrapperIndex + childPositionStart + 1)
            }
            notifyItemRangeRemoved(parentWrapperIndex + childPositionStart + 1, itemCount)
        }
    }

    /**
     * Notify any registered observers that the ParentListItem at `parentPosition` has
     * a child located at `childPosition` that has changed.
     *
     *
     * This is an item change event, not a structural change event. It indicates that any
     * reflection of the data at `childPosition` is out of date and should be updated.
     * The ParentListItem at `childPosition` retains the same identity.
     *
     * @param parentPosition Position of the ParentListItem who has a child that has changed
     * @param childPosition Position of the child that has changed
     */
    fun notifyChildItemChanged(parentPosition: Int, childPosition: Int) {
        val parentListItem = parentItemList[parentPosition]
        val parentWrapperIndex = getParentWrapperIndex(parentPosition)
        val parentWrapper = itemList[parentWrapperIndex] as ParentWrapper?
        parentWrapper!!.parentListItem = parentListItem
        if (parentWrapper.isExpanded) {
            val listChildPosition = parentWrapperIndex + childPosition + 1
            val child = parentWrapper.childItemList[childPosition]
            itemList[listChildPosition] = child
            notifyItemChanged(listChildPosition)
        }
    }

    /**
     * Notify any registered observers that the ParentListItem at `parentPosition` has
     * `itemCount` child Objects starting at `childPositionStart` that have changed.
     *
     *
     * This is an item change event, not a structural change event. It indicates that any
     * The ParentListItem at `childPositionStart` retains the same identity.
     * reflection of the set of `itemCount` child objects starting at `childPositionStart`
     * are out of date and should be updated.
     *
     * @param parentPosition Position of the ParentListItem who has a child that has changed
     * @param childPositionStart Position of the first child object that has changed
     * @param itemCount number of child objects changed
     */
    fun notifyChildItemRangeChanged(parentPosition: Int, childPositionStart: Int, itemCount: Int) {
        val parentListItem = parentItemList[parentPosition]
        val parentWrapperIndex = getParentWrapperIndex(parentPosition)
        val parentWrapper = itemList[parentWrapperIndex] as ParentWrapper?
        parentWrapper!!.parentListItem = parentListItem
        if (parentWrapper.isExpanded) {
            val listChildPosition = parentWrapperIndex + childPositionStart + 1
            for (i in 0 until itemCount) {
                val child = parentWrapper.childItemList[childPositionStart + i]
                itemList[listChildPosition + i] = child
            }
            notifyItemRangeChanged(listChildPosition, itemCount)
        }
    }

    /**
     * Notify any registered observers that the child list item contained within the ParentListItem
     * at `parentPosition` has moved from `fromChildPosition` to `toChildPosition`.
     *
     *
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.
     *
     * @param parentPosition Position of the ParentListItem who has a child that has moved
     * @param fromChildPosition Previous position of the child list item
     * @param toChildPosition New position of the child list item
     */
    fun notifyChildItemMoved(parentPosition: Int, fromChildPosition: Int, toChildPosition: Int) {
        val parentListItem = parentItemList[parentPosition]
        val parentWrapperIndex = getParentWrapperIndex(parentPosition)
        val parentWrapper = itemList[parentWrapperIndex] as ParentWrapper?
        parentWrapper!!.parentListItem = parentListItem
        if (parentWrapper.isExpanded) {
            val fromChild: Any = itemList.removeAt(parentWrapperIndex + 1 + fromChildPosition)
            itemList.add(parentWrapperIndex + 1 + toChildPosition, fromChild)
            notifyItemMoved(parentWrapperIndex + 1 + fromChildPosition, parentWrapperIndex + 1 + toChildPosition)
        }
    }
    // endregion
    /**
     * Generates a HashMap used to store expanded state for items in the list
     * on configuration change or whenever onResume is called.
     *
     * @return A HashMap containing the expanded state of all parent list items
     */
    private fun generateExpandedStateMap(): HashMap<Int, Boolean> {
        val parentListItemHashMap = HashMap<Int, Boolean>()
        var childCount = 0
        var listItem: Any?
        var parentWrapper: ParentWrapper
        val listItemCount = itemList.size
        for (i in 0 until listItemCount) {
            listItem = getListItem(i)
            if (listItem is ParentWrapper) {
                parentWrapper = listItem
                parentListItemHashMap[i - childCount] = parentWrapper.isExpanded
            } else {
                childCount++
            }
        }
        return parentListItemHashMap
    }

    /**
     * Gets the index of a ParentWrapper within the helper item list based on
     * the index of the ParentWrapper.
     *
     * @param parentIndex The index of the parent in the list of parent items
     * @return The index of the parent in the list of all views in the adapter
     */
    private fun getParentWrapperIndex(parentIndex: Int): Int {
        var parentCount = 0
        val listItemCount = itemList.size
        for (i in 0 until listItemCount) {
            if (itemList[i] is ParentWrapper) {
                parentCount++
                if (parentCount > parentIndex) {
                    return i
                }
            }
        }
        return -1
    }

    /**
     * Gets the ParentWrapper for a specified ParentListItem from the list of
     * parents.
     *
     * @param parentListItem A ParentListItem in the list of parents
     * @return If the parent exists on the list, returns its ParentWrapper.
     * Otherwise, returns null.
     */
    private fun getParentWrapper(parentListItem: ParentListItem): ParentWrapper? {
        val listItemCount = itemList.size
        for (i in 0 until listItemCount) {
            val listItem = itemList[i]
            if (listItem is ParentWrapper) {
                if (listItem.parentListItem == parentListItem) {
                    return listItem
                }
            }
        }
        return null
    }

    companion object {
        private const val EXPANDED_STATE_MAP = "ExpandableRecyclerAdapter.ExpandedStateMap"
        private const val TYPE_PARENT = 0
        private const val TYPE_CHILD = 1
    }
}