package com.example.capstone_2.data

data class AiSummaryResponse(
    val message: String,
    val data: AiSummaryData?
)
data class AiSummaryData(
    val message: String,
    val date: String? = null
)
