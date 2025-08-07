
package com.example.capstone_2.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.Response

// ---------- Retrofit 관련 ----------
data class AiSummaryResponse(
    val message: String,
    val data: AiSummaryData?
)

data class AiSummaryData(
    val message: String,
    val date: String
)

interface UsageApiService {
    @GET("/api/summary")
    suspend fun getAiSummary(): Response<AiSummaryResponse>
}

object ApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://211.188.51.35")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val usageApi: UsageApiService = retrofit.create(UsageApiService::class.java)
}

// ---------- Composable UI ----------

@Composable
fun AiSummaryScreen() {
    var summaryText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                Log.d("AI_SUMMARY", "요약 요청 시작")
                val response = ApiClient.usageApi.getAiSummary()
                Log.d("AI_SUMMARY", "응답 성공 여부: ${response.isSuccessful}")
                if (response.isSuccessful) {
                    summaryText = response.body()?.data?.message
                } else {
                    isError = true
                }
            } catch (e: Exception) {
                isError = true
                Log.e("AI_SUMMARY", "요약 호출 실패", e)
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F9FC))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator(color = Color(0xFF4A90E2))
            isError -> Text(
                text = "AI 요약을 불러오는 데 실패했어요.",
                color = Color.Red,
                fontSize = 16.sp
            )
            summaryText != null -> AiSummaryCard(summaryText!!)
            else -> Text("요약 결과가 없습니다.")
        }
    }
}

@Composable
fun AiSummaryCard(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "AI Summary",
            tint = Color(0xFF4A90E2),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AI 요약",
            fontSize = 24.sp,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Text(
                text = message,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                fontSize = 16.sp,
                color = Color(0xFF444444)
            )
        }
    }
}
