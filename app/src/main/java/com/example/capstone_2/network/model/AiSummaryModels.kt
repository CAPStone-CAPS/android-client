package com.example.capstone_2.network.model

data class AiSummaryResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: AiSummaryData? = null
)

data class AiSummaryData(
    val message: String? = null,
    val sentiment: String? = null,
    val confidence: Double? = null
)
