package com.example.capstone_2.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.capstone_2.R


val NanumGothic = FontFamily(
    Font(R.font.nanumgothic_regular, FontWeight.Normal),
    Font(R.font.nanumgothic_bold, FontWeight.Bold),
    Font(R.font.nanumgothic_extrabold, FontWeight.ExtraBold)
)

val NotoSansCondensed = FontFamily(
    Font(R.font.notosans_condensed_medium, FontWeight.Medium),
    Font(R.font.notosans_semicondensed_bold, FontWeight.Bold),
    Font(R.font.notosans_semicondensed_thin, FontWeight.Thin)
)

// 전체 앱에 적용할 Typography 설정
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = NanumGothic,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    titleLarge = TextStyle(
        fontFamily = NanumGothic,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )
)