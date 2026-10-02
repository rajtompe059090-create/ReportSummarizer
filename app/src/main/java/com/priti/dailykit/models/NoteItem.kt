package com.priti.dailykit.models

data class NoteItem(
    val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long,
    val colorHex: String = "#16233B"
)
