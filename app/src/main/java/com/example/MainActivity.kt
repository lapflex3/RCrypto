package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.BinaMainScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.BinaTheme
import com.example.viewmodel.AppFlowState
import com.example.viewmodel.BinaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BinaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BinaTheme {
                val flowState by viewModel.appFlowState.collectAsState()
                when (flowState) {
                    AppFlowState.SPLASH -> SplashScreen(
                        onFinished = { viewModel.finishSplash() }
                    )
                    AppFlowState.AUTH -> AuthScreen(
                        viewModel = viewModel
                    )
                    AppFlowState.MAIN -> BinaMainScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
