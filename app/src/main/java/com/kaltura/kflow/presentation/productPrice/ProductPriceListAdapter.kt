package com.kaltura.kflow.presentation.productPrice

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.client.types.PpvPrice
import com.kaltura.client.types.ProductPrice
import com.kaltura.client.types.SubscriptionPrice
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.extension.inflate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_product_price.*
import java.util.*

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
class ProductPriceListAdapter(private val productPrices: ArrayList<ProductPrice>) : RecyclerView.Adapter<ProductPriceListAdapter.MyViewHolder>() {

    var onSubscriptionPriceClickListener: (subscriptionPrice: SubscriptionPrice) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(parent.inflate(R.layout.item_product_price))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(productPrices[position])

    override fun getItemCount() = productPrices.size

    inner class MyViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(productPrice: ProductPrice) {
            productPriceType.text = "Product Type: ${productPrice.productType.value}"
            price.text = "Actual price: ${productPrice.fullPrice.currencySign}${productPrice.fullPrice.amount}"
            discountPrice.text = "Discount price: ${productPrice.price.currencySign}${productPrice.price.amount}"
            if (productPrice is SubscriptionPrice) {
                itemView.setOnClickListener { onSubscriptionPriceClickListener(productPrice) }
            } else if (productPrice is PpvPrice) {
                itemView.setOnClickListener(null)
            }
        }
    }
}