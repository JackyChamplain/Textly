package com.example.messageapp.utilities

import androidx.compose.runtime.mutableFloatStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class SettingsViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    // Retrieve font size from SavedStateHandle, default to 16f if not present
    var fontSize = mutableFloatStateOf(
        savedStateHandle.get<Float>("fontSize") ?: 18f
    )

    // Set and save the font size
    fun setFontSize(size: Float) {
        fontSize.value = size
        savedStateHandle["fontSize"] = size
    }
}
