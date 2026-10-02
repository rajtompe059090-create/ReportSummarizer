package com.priti.dailykit.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.priti.dailykit.R
import com.priti.dailykit.adapters.NotesAdapter
import com.priti.dailykit.ads.AdsManager
import com.priti.dailykit.databinding.ActivityNotesBinding
import com.priti.dailykit.models.NoteItem
import com.priti.dailykit.storage.NotesDatabaseHelper

class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding
    private lateinit var dbHelper: NotesDatabaseHelper
    private lateinit var adapter: NotesAdapter
    private var searchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = NotesDatabaseHelper(this)

        setupUI()
        setupListeners()

        AdsManager.loadBanner(this, binding.adBannerContainer)
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun setupUI() {
        adapter = NotesAdapter(
            context = this,
            notes = emptyList(),
            onNoteClicked = { note ->
                val intent = Intent(this, NoteEditActivity::class.java).apply {
                    putExtra(NoteEditActivity.EXTRA_NOTE_ID, note.id)
                }
                startActivity(intent)
            },
            onDeleteClicked = { note ->
                showDeleteDialog(note)
            }
        )

        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        val createNote = View.OnClickListener {
            startActivity(Intent(this, NoteEditActivity::class.java))
        }
        binding.btnNewNoteHeader.setOnClickListener(createNote)
        binding.fabNewNote.setOnClickListener(createNote)
        binding.btnCreateFirstNote.setOnClickListener(createNote)

        binding.etSearchNotes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                binding.btnClearSearch.visibility = if (searchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                loadNotes()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearchNotes.setText("")
        }
    }

    private fun loadNotes() {
        val notes = if (searchQuery.isEmpty()) {
            dbHelper.getAllNotes()
        } else {
            dbHelper.searchNotes(searchQuery)
        }

        adapter.updateData(notes)

        if (notes.isEmpty()) {
            binding.layoutEmptyNotes.visibility = View.VISIBLE
            binding.rvNotes.visibility = View.GONE
        } else {
            binding.layoutEmptyNotes.visibility = View.GONE
            binding.rvNotes.visibility = View.VISIBLE
        }
    }

    private fun showDeleteDialog(note: NoteItem) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.action_delete))
            .setMessage("Are you sure you want to permanently delete \"${note.title}\"?")
            .setPositiveButton(getString(R.string.action_delete)) { _, _ ->
                dbHelper.deleteNote(note.id)
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                loadNotes()
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show()
    }
}
