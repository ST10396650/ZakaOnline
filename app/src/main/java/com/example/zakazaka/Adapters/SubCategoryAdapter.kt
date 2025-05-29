package com.example.zakazaka.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zakazaka.R
import com.example.zakazaka.Models.SubCategoryEntity

class SubCategoryAdapter(
    private val subCategories: List<SubCategoryEntity>,
    private val onClick: (SubCategoryEntity) -> Unit
) : RecyclerView.Adapter<SubCategoryAdapter.SubCategoryViewHolder>() {

    inner class SubCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.subcategoryName)
        val budgetText: TextView = itemView.findViewById(R.id.subcategoryBudget)
        val currentText: TextView = itemView.findViewById(R.id.subcategoryCurrent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subcategory_circle, parent, false)
        return SubCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubCategoryViewHolder, position: Int) {
        val subCategory = subCategories[position]
        holder.nameText.text = subCategory.name
        holder.budgetText.text = "Budget : R${subCategory.budgetLimit}"
        holder.currentText.text = "Current : R${subCategory.currentAmount}"
        holder.itemView.setOnClickListener {
            onClick(subCategory)
        }
    }

    override fun getItemCount(): Int = subCategories.size
}


