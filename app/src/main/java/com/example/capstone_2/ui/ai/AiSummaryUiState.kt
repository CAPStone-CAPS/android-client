package com.example.capstone_2.ui.ai

sealed interface AiSummaryUiState {
    object Loading: AiSummaryUiState
    data class Success(val text: String): AiSummaryUiState
    data class Error(val reason: String, val fallback: String): AiSummaryUiState
}
