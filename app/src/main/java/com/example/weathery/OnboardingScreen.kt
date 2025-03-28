package com.example.weathery

import androidx.compose.runtime.Composable

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    // Replace with real onboarding UI
    androidx.compose.material3.Button(onClick = onDone) {
        androidx.compose.material3.Text("Get Started")
    }
}
