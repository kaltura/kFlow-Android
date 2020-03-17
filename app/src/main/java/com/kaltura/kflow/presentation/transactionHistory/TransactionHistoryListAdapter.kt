//package com.kaltura.kflow.presentation.transactionHistory
//
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.kaltura.client.types.BillingTransaction
//import com.kaltura.kflow.R
//import com.kaltura.kflow.presentation.extension.inflate
//import kotlinx.android.extensions.LayoutContainer
//
///**
// * Created by alex_lytvynenko on 30.11.2018.
// */
//class TransactionHistoryListAdapter : RecyclerView.Adapter<TransactionHistoryListAdapter.MyViewHolder>() {
//
//    var transactions = arrayListOf<BillingTransaction>()
//        set(value) {
//            transactions.clear()
//            transactions.addAll(value)
//            notifyDataSetChanged()
//        }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(parent.inflate(R.layout.item_product_price))
//
//    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(transactions[position])
//
//    override fun getItemCount() = transactions.size
//
//    inner class MyViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
//
//        fun bind(billingTransaction: BillingTransaction) {
//            //mType.setText("Product Type: " + productPrice.getProductType().getValue());
//            //mPrice.setText("Price: " + productPrice.getPrice().getCurrencySign() + productPrice.getPrice().getAmount());
//        }
//    }
//}