package com.rheotv.android.ui.activities.tabcontainer.profile.wallet

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.data.network.models.useProfile.responses.RedeemStatement
import com.rheotv.android.databinding.ListItemWalletTransactionBinding
import com.rheotv.android.ui.base.BaseViewHolder
import java.util.*

class WalletTransactionRecyclerAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    private val mList: MutableList<RedeemStatement> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return TransactionItemViewHolder(ListItemWalletTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    fun submitList(list: List<RedeemStatement>) {
        val preSize = mList.size
        mList.addAll(list)
        notifyItemRangeInserted(preSize, list.size)
    }

    inner class TransactionItemViewHolder(val binding: ListItemWalletTransactionBinding) : BaseViewHolder(binding.root) {
        override fun onBind(position: Int) {
            val statement: RedeemStatement = mList[position]
            binding.transactionAmount.text = "₹${statement.amount}"
            binding.transactionDate.text = statement.getFormattedDate()
            binding.transactedVia.text = "Transfer to ${statement.transferVia ?: "UPI"}"
            binding.transactedTo.text = statement.transferredTo
            PaymentState.getPaymentState(statement.state).apply {
                binding.transactionsState.text = getStateText()
                binding.transactionsState.setTextColor(getStateColor())
            }
        }
    }
}

sealed class PaymentState {
    object Paid : PaymentState() {
        override fun getStateText(): String = "Paid"
        override fun getStateColor(): Int = Color.parseColor("#04b758")
    }

    object Rejected : PaymentState() {
        override fun getStateText(): String = "Rejected"
        override fun getStateColor(): Int = Color.parseColor("#ff4139")
    }

    object Pending : PaymentState() {
        override fun getStateText(): String = "Pending"
        override fun getStateColor(): Int = Color.parseColor("#f9a719")
    }

    abstract fun getStateText(): String
    abstract fun getStateColor(): Int

    companion object {
        fun getPaymentState(state: String?): PaymentState =
                if (state?.equals("processed", ignoreCase = true) == true ||
                        state?.equals("approved", ignoreCase = true) == true) {
                    Paid
                } else if (state?.equals("rejected", ignoreCase = true) == true) {
                    Rejected
                } else {
                    Pending
                }
    }
}