package com.rheotv.android.ui.activities.inAppBilling

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.rheotv.android.databinding.ListItemProductSkuBinding
import com.rheotv.android.ui.activities.inAppBilling.model.BillingSkuWrapper
import com.rheotv.android.ui.base.BaseViewHolder

class ProductSkuAdapter constructor(val onSkuClick: ((BillingSkuWrapper?) -> Unit)?) : RecyclerView.Adapter<BaseViewHolder>() {
    var list: List<BillingSkuWrapper> = ArrayList()

    fun submitList(list: List<BillingSkuWrapper>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return SkuViewHolder(ListItemProductSkuBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class SkuViewHolder(val binding: ListItemProductSkuBinding) : BaseViewHolder(binding.root) {
        override fun onBind(position: Int) {
            with(binding) {
                val res = list[position]
                sku = res
                root.setOnClickListener {
                    onSkuClick?.invoke(res)
                }
            }
        }
    }
}