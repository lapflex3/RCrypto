package com.example.ui.screens

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.components.TransparentWatermark
import com.example.ui.theme.BinaPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    var hasFinished by remember { mutableStateOf(false) }

    fun triggerFinish() {
        if (!hasFinished) {
            hasFinished = true
            onFinished()
        }
    }

    // Safety timeout in case video fails to fire callback
    LaunchedEffect(Unit) {
        delay(6000)
        triggerFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Fullscreen Video View
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    try {
                        val videoUri = Uri.parse("android.resource://" + ctx.packageName + "/" + R.raw.splash_video)
                        setVideoURI(videoUri)
                        setOnPreparedListener { mp ->
                            mp.isLooping = false
                            start()
                        }
                        setOnCompletionListener {
                            triggerFinish()
                        }
                        setOnErrorListener { _, _, _ ->
                            triggerFinish()
                            true
                        }
                    } catch (e: Exception) {
                        triggerFinish()
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Skip Button
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable { triggerFinish() }
                .testTag("btn_skip_splash"),
            color = Color.Black.copy(alpha = 0.55f),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Langkau",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Skip",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Bottom Loading & Branding Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = BinaPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Memuatkan RCrypto Platform...",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transparent Watermark as requested
            TransparentWatermark()
        }
    }
}
