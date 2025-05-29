package com.example.zakazaka.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zakazaka.Models.CategoryEntity
import com.example.zakazaka.R

class CategoryAdapter(
    private val categories: List<CategoryEntity>,
    private val onClick: (CategoryEntity) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryTitle: TextView = itemView.findViewById(R.id.categoryTitle1)
        val amountLeft: TextView = itemView.findViewById(R.id.amountLeft1)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        // Add logging to debug what values we're getting
        Log.d("CategoryAdapter", "Binding category at position $position:")
        Log.d("CategoryAdapter", "Name: ${category.name}")
        Log.d("CategoryAdapter", "Budget Limit: ${category.budgetLimit}")
        Log.d("CategoryAdapter", "Current Amount: ${category.currentAmount}")

        holder.categoryTitle.text = "Expense: ${category.name}"

        // Add null safety and better error handling
        val budgetLimit = category.budgetLimit
        val currentAmount = category.currentAmount

        if (budgetLimit > 0) {
            val amountLeft = budgetLimit - currentAmount
            holder.amountLeft.text = "R${String.format("%.2f", amountLeft)} left of R${String.format("%.2f", budgetLimit)}"

            // Calculate progress percentage
            val progress = if (budgetLimit > 0) {
                ((currentAmount / budgetLimit) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }
            holder.progressBar.progress = progress

            Log.d("CategoryAdapter", "Amount left: $amountLeft, Progress: $progress%")
        } else {
            holder.amountLeft.text = "Budget not set"
            holder.progressBar.progress = 0
            Log.w("CategoryAdapter", "Budget limit is 0 or negative for category: ${category.name}")
        }

        holder.itemView.setOnClickListener {
            onClick(category)
        }
    }

    override fun getItemCount(): Int = categories.size
}
