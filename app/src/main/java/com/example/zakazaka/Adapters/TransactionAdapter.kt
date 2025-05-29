package com.example.zakazaka.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zakazaka.Models.TransactionEntity
import com.example.zakazaka.R
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(private var transactions: List<TransactionEntity> = emptyList(),
                         private val onTransactionClick: (TransactionEntity) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size


    fun updateTransactions(newList: List<TransactionEntity>) {
        transactions = newList
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.transactionTitle)
        private val amountText: TextView = itemView.findViewById(R.id.transactionAmount)
        private val dateText: TextView = itemView.findViewById(R.id.transactionDate)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTransactionClick(transactions[position])
                }
            }
        }

        fun bind(transaction: TransactionEntity) {
            titleText.text = transaction.description
            amountText.text = "Amount: ${transaction.amount} ${transaction.currency}"
            dateText.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(transaction.date)

            // Set color based on transaction type
            val textColor = if (transaction.type == "EXPENSE") {
                ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark)
            } else {
                ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark)
            }
            amountText.setTextColor(textColor)
        }
    }
}