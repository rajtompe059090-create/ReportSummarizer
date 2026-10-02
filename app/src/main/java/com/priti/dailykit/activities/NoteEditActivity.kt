package com.priti.dailykit.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.priti.dailykit.R
import com.priti.dailykit.ads.AdsManager
import com.priti.dailykit.databinding.ActivityNoteEditBinding
import com.priti.dailykit.models.NoteItem
import com.priti.dailykit.storage.NotesDatabaseHelper
import com.priti.dailykit.utils.FormatUtils

class NoteEditActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
    }

    private lateinit var binding: ActivityNoteEditBinding
    private lateinit var dbHelper: NotesDatabaseHelper
    private var noteId: Long = -1L
    private var existingNote: NoteItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = NotesDatabaseHelper(this)
        noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)

        setupUI()
        setupListeners()

        AdsManager.loadBanner(this, binding.adBannerContainer)
    }

    private fun setupUI() {
        if (noteId != -1L) {
            existingNote = dbHelper.getNoteById(noteId)
            existingNote?.let { note ->
                binding.tvHeaderTitle.text = "Edit Note"
                binding.etNoteTitle.setText(note.title)
                binding.etNoteContent.setText(note.content)
                binding.tvNoteTimestamp.text = "Last edited: ${FormatUtils.formatDate(note.timestamp)}"
                binding.btnDeleteNote.visibility = View.VISIBLE
            } ?: run {
                binding.tvHeaderTitle.text = "New Note"
                binding.tvNoteTimestamp.text = "Created just now"
                binding.btnDeleteNote.visibility = View.GONE
            }
        } else {
            binding.tvHeaderTitle.text = "New Note"
            binding.tvNoteTimestamp.text = "Created just now"
            binding.btnDeleteNote.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnSaveNote.setOnClickListener {
            saveNote()
        }

        binding.btnDeleteNote.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.action_delete))
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton(getString(R.string.action_delete)) { _, _ ->
                    if (noteId != -1L) {
                        dbHelper.deleteNote(noteId)
                        Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
        }
    }

    private fun saveNote() {
        var title = binding.etNoteTitle.text?.toString()?.trim() ?: ""
        val content = binding.etNoteContent.text?.toString()?.trim() ?: ""

        if (title.isBlank() && content.isBlank()) {
            Toast.makeText(this, "Note cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (title.isBlank()) {
            // Auto-generate title from first line of content
            val firstLine = content.lines().firstOrNull()?.trim() ?: "Untitled Note"
            title = if (firstLine.length > 30) firstLine.substring(0, 30) + "..." else firstLine
        }

        val timestamp = System.currentTimeMillis()
        if (noteId != -1L) {
            val updated = NoteItem(noteId, title, content, timestamp)
            dbHelper.updateNote(updated)
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
        } else {
            val newNote = NoteItem(title = title, content = content, timestamp = timestamp)
            dbHelper.insertNote(newNote)
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
