package com.priti.dailykit.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.priti.dailykit.R
import com.priti.dailykit.databinding.ItemToolCardBinding
import com.priti.dailykit.models.ToolItem
import com.priti.dailykit.storage.DailyKitPreferences

class ToolCardAdapter(
    private val context: Context,
    private var tools: List<ToolItem>,
    private val onFavoriteToggled: ((ToolItem, Boolean) -> Unit)? = null
) : RecyclerView.Adapter<ToolCardAdapter.ToolViewHolder>() {

    private val prefs = DailyKitPreferences.getInstance(context)

    fun updateData(newTools: List<ToolItem>) {
        this.tools = newTools
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val binding = ItemToolCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ToolViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        holder.bind(tools[position])
    }

    override fun getItemCount(): Int = tools.size

    inner class ToolViewHolder(private val binding: ItemToolCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tool: ToolItem) {
            binding.tvTitle.setText(tool.titleRes)
            binding.tvDescription.setText(tool.descRes)
            binding.imgIcon.setImageResource(tool.iconRes)
            binding.imgIcon.setColorFilter(ContextCompat.getColor(context, tool.accentColorRes))

            val isFav = prefs.isFavorite(tool.id)
            updateFavoriteIcon(isFav)

            binding.btnFavorite.setOnClickListener {
                val newState = prefs.toggleFavorite(tool.id)
                updateFavoriteIcon(newState)
                onFavoriteToggled?.invoke(tool, newState)
            }

            binding.root.setOnClickListener {
                prefs.addRecentTool(tool.id)
                val intent = Intent(context, tool.targetActivityClass)
                context.startActivity(intent)
            }
        }

        private fun updateFavoriteIcon(isFav: Boolean) {
            if (isFav) {
                binding.btnFavorite.setImageResource(R.drawable.ic_star_filled)
                binding.btnFavorite.setColorFilter(ContextCompat.getColor(context, R.color.accent_amber))
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_star_border)
                binding.btnFavorite.setColorFilter(ContextCompat.getColor(context, R.color.text_muted))
            }
        }
    }
}
