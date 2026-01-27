package com.kaltura.kflow.presentation.transactionHistory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.client.types.BillingTransaction
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.ItemProductPriceBinding
import com.kaltura.kflow.databinding.ItemRecordingBinding
import com.kaltura.kflow.presentation.extension.inflate
import com.kaltura.kflow.presentation.recordingList.RecordingListAdapter
import kotlinx.android.extensions.LayoutContainer

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
class TransactionHistoryListAdapter : RecyclerView.Adapter<TransactionHistoryListAdapter.MyViewHolder>() {

    var transactions = arrayListOf<BillingTransaction>()
        set(value) {
            transactions.clear()
            transactions.addAll(value)
            notifyDataSetChanged()
        }
    private var _binding: ItemProductPriceBinding? = null
    private val binding get() = _binding!!
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val bindMe = ItemProductPriceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(bindMe,parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(transactions[position])

    override fun getItemCount() = transactions.size
    inner class MyViewHolder(val binding: ItemProductPriceBinding,override val containerView: View) : RecyclerView.ViewHolder(binding.root), LayoutContainer {

        fun bind(billingTransaction: BillingTransaction) {
            binding.productPriceType.setText("Product Type: " + billingTransaction.billingPriceType);
            binding.price.setText("Price: " + billingTransaction.getPrice().getCurrencySign() + billingTransaction.getPrice().getAmount());
        }
    }
}