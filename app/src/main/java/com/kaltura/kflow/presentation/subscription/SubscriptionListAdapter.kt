package com.kaltura.kflow.presentation.subscription

import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import com.kaltura.client.types.Asset
import com.kaltura.client.types.DoubleValue
import com.kaltura.client.types.Subscription
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.ItemEntitlementBinding
import com.kaltura.kflow.databinding.ItemPackageBinding
import com.kaltura.kflow.databinding.ItemSubscriptionBinding
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
import java.text.SimpleDateFormat
import java.util.*
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
            val baseId = (parentRecyclerViewItem.parent).metas["BaseID"]
            if (baseId != null && (baseId as DoubleValue).value == packageBaseId) {
                expandParent(it)
                parentRecyclerViewItem.children.addAll(subscriptions)
                notifyChildItemRangeInserted(parentItemList.indexOf(it), 0, subscriptions.size)
            }
        }
    }
    private var _bindingSubscription: ItemSubscriptionBinding? = null
    private val bindingSubscription get() = _bindingSubscription!!
    private var _bindingPackage: ItemPackageBinding? = null
    private val bindingPackage get() = _bindingPackage!!
    inner class PackageViewHolder(override val containerView: View) : ParentViewHolder(containerView), LayoutContainer {

        private val INITIAL_POSITION = 0.0f
        private val ROTATED_POSITION = 180f

        fun bind(asset: Asset, isExpandable: Boolean) {
            isExpanded = isExpandable
            bindingPackage.packageName.text = asset.name
            bindingPackage.packageId.text = "Package ID: ${asset.id}"
            val metaBaseId = asset.metas["BaseID"]
            bindingPackage.baseId.text = "Base ID: ${(metaBaseId as? DoubleValue)?.value?.toInt() ?: "No ID"}"
            bindingPackage.get.visibleOrGone(metaBaseId != null)
            bindingPackage.get.setOnClickListener {
                packageGetSubscriptionListener((metaBaseId as DoubleValue).value)
                bindingPackage.get.gone()
                bindingPackage.arrowIcon.visible()
            }
        }

        override var isExpanded: Boolean
            get() = super.isExpanded
            set(value) {
                super.isExpanded = value
                if (value) bindingPackage.arrowIcon.rotation = ROTATED_POSITION else bindingPackage.arrowIcon.rotation = INITIAL_POSITION
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
            bindingPackage.arrowIcon.startAnimation(rotateAnimation)
        }
    }

    inner class SubscriptionViewHolder(override val containerView: View) : ChildViewHolder(containerView), LayoutContainer {

        fun bind(subscription: Subscription) {
            bindingSubscription.subsName.text = if (subscription.name.isNotEmpty()) subscription.name else "NO NAME"
            bindingSubscription.subsId.text = "Subscription ID: ${subscription.id}"
            bindingSubscription.subsPrice.text = "Subscription Price: ${subscription.price.name}"

            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)

            val startDayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            startDayCalendar.timeInMillis = subscription.startDate * 1000
            bindingSubscription.startDate.text = "Start: ${dateFormat.format(startDayCalendar.time)}"

            val endDayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            endDayCalendar.timeInMillis = subscription.endDate * 1000
            bindingSubscription.endDate.text = "End: ${dateFormat.format(endDayCalendar.time)}"

            bindingSubscription.arrow.visibleOrGone(subscription.channels.isNotEmpty())
            itemView.setOnClickListener {
                val channelsId = arrayListOf<Long>()
                subscription.channels.forEach { channelsId.add(it.id) }
                subscriptionListener(channelsId)
            }
        }
    }
}