package com.kaltura.kflow.presentation.subscription

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.extension.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_entitlement.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
class EntitlementListAdapter : RecyclerView.Adapter<EntitlementListAdapter.MyViewHolder>() {

    var entitlements: ArrayList<Entitlement> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyViewHolder(parent.inflate(R.layout.item_entitlement))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) =
        holder.bind(entitlements[position])

    override fun getItemCount() = entitlements.size

    inner class MyViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bind(entitlement: Entitlement) {
            val timeFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
            val purchaseDayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            purchaseDayCalendar.timeInMillis = entitlement.purchaseDate * 1000
            val lastViewDayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            lastViewDayCalendar.timeInMillis = entitlement.lastViewDate * 1000
            productId.text = "Product ID: ${entitlement.productId}"
            purchaseDate.text = "Purchase Date: ${timeFormat.format(purchaseDayCalendar.time)}"
            lastViewDate.text = "Last View Date: ${timeFormat.format(lastViewDayCalendar.time)}"

            when (entitlement) {
                is SubscriptionEntitlement -> type.text = "Type: Subscription"
                is PpvEntitlement -> type.text = "Type: PPV"
                is CollectionEntitlement -> type.text = "Type: Collection"
            }
        }
    }
}