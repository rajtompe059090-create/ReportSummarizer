package com.priti.dailykit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.priti.dailykit.databinding.ItemNoteCardBinding
import com.priti.dailykit.models.NoteItem
import com.priti.dailykit.utils.FormatUtils

class NotesAdapter(
    private val context: Context,
    private var notes: List<NoteItem>,
    private val onNoteClicked: (NoteItem) -> Unit,
    private val onDeleteClicked: (NoteItem) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    fun updateData(newNotes: List<NoteItem>) {
        this.notes = newNotes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int = notes.size

    inner class NoteViewHolder(private val binding: ItemNoteCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: NoteItem) {
            binding.tvNoteTitle.text = note.title
            binding.tvNoteContent.text = note.content
            binding.tvNoteDate.text = FormatUtils.formatDate(note.timestamp)

            binding.btnDeleteNote.setOnClickListener {
                onDeleteClicked(note)
            }

            binding.root.setOnClickListener {
                onNoteClicked(note)
            }
        }
    }
}
