package com.priti.dailykit.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.priti.dailykit.databinding.ItemQuickToolBinding
import com.priti.dailykit.models.ToolItem
import com.priti.dailykit.storage.DailyKitPreferences

class QuickToolAdapter(
    private val context: Context,
    private val tools: List<ToolItem>
) : RecyclerView.Adapter<QuickToolAdapter.QuickViewHolder>() {

    private val prefs = DailyKitPreferences.getInstance(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickViewHolder {
        val binding = ItemQuickToolBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuickViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int) {
        holder.bind(tools[position])
    }

    override fun getItemCount(): Int = tools.size

    inner class QuickViewHolder(private val binding: ItemQuickToolBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tool: ToolItem) {
            binding.tvQuickTitle.setText(tool.titleRes)
            binding.imgQuickIcon.setImageResource(tool.iconRes)
            binding.imgQuickIcon.setColorFilter(ContextCompat.getColor(context, tool.accentColorRes))

            binding.root.setOnClickListener {
                prefs.addRecentTool(tool.id)
                val intent = Intent(context, tool.targetActivityClass)
                context.startActivity(intent)
            }
        }
    }
}
