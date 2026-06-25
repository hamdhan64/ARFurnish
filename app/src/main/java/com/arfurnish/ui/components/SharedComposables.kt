package com.arfurnish.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun AlphabetItem(alphabet: String, onClick: () -> Unit) {
    val color = remember(alphabet) {
        generateRandomLightColor()
    }
    Box(modifier = Modifier
        .padding(16.dp)
        .size(60.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(color)
        .clickable { onClick() }) {
        Text(
            text = alphabet,
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.Center),
            color = Color.Black
        )
    }
}

fun generateRandomLightColor(): Color {
    val random = Random(System.currentTimeMillis())
    val red = random.nextInt(150, 256)
    val green = random.nextInt(200, 256)
    val blue = random.nextInt(200, 256)
    val color = Color(red, green, blue)
    return color
}

