package com.kaltura.kflow.presentation.subscription

import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import com.kaltura.client.types.Asset
import com.kaltura.client.types.DoubleValue
import com.kaltura.client.types.Subscription
import com.kaltura.kflow.R
import com.kaltura.kflow.entity.ParentRecyclerViewItem
import com.kaltura.kflow.presentation.extension.gone
import com.kaltura.kflow.presentation.extension.inflate
import com.kaltura.kflow.presentation.extension.visible
import com.kaltura.kflow.presentation.extension.visibleOrGone
import com.kaltura.kflow.presentation.subscription.SubscriptionListAdapter.PackageViewHolder
import com.kaltura.kflow.presentation.subscription.SubscriptionListAdapter.SubscriptionViewHolder
import com.kaltura.kflow.presentation.ui.expandableRecyclerView.ChildViewHolder
import com.kaltura.kflow.presentation.ui.expandableRecyclerView.ExpandableRecyclerAdapter
import com.kaltura.kflow.presentation.ui.expandableRecyclerView.ParentListItem
import com.kaltura.kflow.presentation.ui.expandableRecyclerView.ParentViewHolder
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_package.*
import kotlinx.android.synthetic.main.item_subscription.*
import kotlin.collections.ArrayList

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
class SubscriptionListAdapter(parentItemList: ArrayList<ParentRecyclerViewItem<Asset, Subscription>>) : ExpandableRecyclerAdapter<PackageViewHolder, SubscriptionViewHolder>(parentItemList) {

    var packageGetSubscriptionListener: (packageBaseId: Double) -> Unit = {}
    var subscriptionListener: (subscriptionChannelsId: ArrayList<Long>) -> Unit = {}

    override fun onCreateParentViewHolder(parentViewGroup: ViewGroup) = PackageViewHolder(parentViewGroup.inflate(R.layout.item_package))

    override fun onCreateChildViewHolder(childViewGroup: ViewGroup) = SubscriptionViewHolder(childViewGroup.inflate(R.layout.item_subscription))

    override fun onBindParentViewHolder(parentViewHolder: PackageViewHolder, position: Int, parentListItem: ParentListItem) {
        val item = parentListItem as ParentRecyclerViewItem<Asset, Subscription>
        parentViewHolder.bind(item.parent, item.children.isNotEmpty())
    }

    override fun onBindChildViewHolder(childViewHolder: SubscriptionViewHolder, position: Int, childListItem: Any?) {
        childViewHolder.bind(childListItem as Subscription)
    }

    fun addSubscriptionToPackage(packageBaseId: Double, subscriptions: ArrayList<Subscription>) {
        parentItemList.forEach {
            val parentRecyclerViewItem = it as ParentRecyclerViewItem<Asset, Subscription>
            val baseId = (parentRecyclerViewItem.parent).metas["Base ID"]
            if (baseId != null && (baseId as DoubleValue).value == packageBaseId) {
                expandParent(it)
                parentRecyclerViewItem.children.addAll(subscriptions)
                notifyChildItemRangeInserted(parentItemList.indexOf(it), 0, subscriptions.size)
            }
        }
    }

    inner class PackageViewHolder(override val containerView: View) : ParentViewHolder(containerView), LayoutContainer {

        private val INITIAL_POSITION = 0.0f
        private val ROTATED_POSITION = 180f

        fun bind(asset: Asset, isExpandable: Boolean) {
            isExpanded = isExpandable
            packageName.text = asset.name
            packageId.text = "Package ID: ${asset.id}"
            val metaBaseId = asset.metas["Base ID"]
            baseId.text = "Base ID: ${(metaBaseId as? DoubleValue)?.value?.toInt() ?: "No ID"}"
            get.visibleOrGone(metaBaseId != null)
            get.setOnClickListener {
                packageGetSubscriptionListener((metaBaseId as DoubleValue).value)
                get.gone()
                arrowIcon.visible()
            }
        }

        override var isExpanded: Boolean
            get() = super.isExpanded
            set(value) {
                super.isExpanded = value
                if (value) arrowIcon.rotation = ROTATED_POSITION else arrowIcon.rotation = INITIAL_POSITION
            }

        override fun onExpansionToggled(expanded: Boolean) {
            super.onExpansionToggled(expanded)
            val rotateAnimation: RotateAnimation =
                    if (expanded) { // rotate clockwise
                        RotateAnimation(ROTATED_POSITION,
                                INITIAL_POSITION,
                                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                                RotateAnimation.RELATIVE_TO_SELF, 0.5f)
                    } else { // rotate counterclockwise
                        RotateAnimation(-1 * ROTATED_POSITION,
                                INITIAL_POSITION,
                                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                                RotateAnimation.RELATIVE_TO_SELF, 0.5f)
                    }
            rotateAnimation.duration = 200
            rotateAnimation.fillAfter = true
            arrowIcon.startAnimation(rotateAnimation)
        }
    }

    inner class SubscriptionViewHolder(override val containerView: View) : ChildViewHolder(containerView), LayoutContainer {

        fun bind(subscription: Subscription) {
            subsName.text = subscription.name
            subsId.text = "Subscription ID: ${subscription.id}"
            val stringBuilder = StringBuilder("Channels ID: [")
            subscription.channels?.forEach {
                if (subscription.channels.indexOf(it) != 0) stringBuilder.append(", ")
                stringBuilder.append(it.id.toString())
            }
            stringBuilder.append("]")
            channelIds.text = stringBuilder
            arrow.visibleOrGone(subscription.channels.isNotEmpty())
            itemView.setOnClickListener {
                val channelsId = arrayListOf<Long>()
                subscription.channels.forEach { channelsId.add(it.id) }
                subscriptionListener(channelsId)
            }
        }
    }
}