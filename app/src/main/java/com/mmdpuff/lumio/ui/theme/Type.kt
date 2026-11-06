package com.mmdpuff.lumio.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.mmdpuff.lumio.R

val FontInter = FontFamily(
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
)
val FontRobotoMono = FontFamily(
    Font(R.font.robotomono_regular, FontWeight.Normal),
    Font(R.font.robotomono_medium, FontWeight.Medium),

    )
val Typography = Typography().run {

    copy(
        displayLarge = displayLarge.copy(fontFamily = FontInter),
        displayMedium = displayMedium.copy(fontFamily = FontInter),
        displaySmall = displaySmall.copy(fontFamily = FontInter),

        headlineLarge = headlineLarge.copy(fontFamily = FontInter),
        headlineMedium = headlineMedium.copy(fontFamily = FontInter),
        headlineSmall = headlineSmall.copy(fontFamily = FontInter),

        titleLarge = titleLarge.copy(fontFamily = FontInter),
        titleMedium = titleMedium.copy(fontFamily = FontInter),
        titleSmall = titleSmall.copy(fontFamily = FontInter),

        bodyLarge = bodyLarge.copy(fontFamily = FontInter),
        bodyMedium = bodyMedium.copy(fontFamily = FontInter),
        bodySmall = bodySmall.copy(fontFamily = FontInter),

        labelLarge = labelLarge.copy(fontFamily = FontInter),
        labelMedium = labelMedium.copy(fontFamily = FontInter),
        labelSmall = labelSmall.copy(fontFamily = FontInter)
    )
}