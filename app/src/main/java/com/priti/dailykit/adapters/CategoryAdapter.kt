package com.priti.dailykit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.priti.dailykit.R
import com.priti.dailykit.databinding.ItemCategoryChipBinding
import com.priti.dailykit.models.ToolCategory

class CategoryAdapter(
    private val context: Context,
    private val categories: List<ToolCategory>,
    private val onCategorySelected: (ToolCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedCategory: ToolCategory = ToolCategory.ALL

    fun setSelectedCategory(category: ToolCategory) {
        val oldPos = categories.indexOf(selectedCategory)
        val newPos = categories.indexOf(category)
        selectedCategory = category
        if (oldPos != -1) notifyItemChanged(oldPos)
        if (newPos != -1) notifyItemChanged(newPos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryChipBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(private val binding: ItemCategoryChipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: ToolCategory) {
            binding.tvCategoryChip.setText(category.titleRes)
            val isSelected = category == selectedCategory

            if (isSelected) {
                binding.tvCategoryChip.setBackgroundResource(R.drawable.bg_result_card)
                binding.tvCategoryChip.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            } else {
                binding.tvCategoryChip.setBackgroundResource(R.drawable.bg_search_bar)
                binding.tvCategoryChip.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            }

            binding.root.setOnClickListener {
                if (selectedCategory != category) {
                    setSelectedCategory(category)
                    onCategorySelected(category)
                }
            }
        }
    }
}
