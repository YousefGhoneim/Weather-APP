package com.example.weathery

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.weathery.R

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pages = listOf(
        painterResource(id = R.drawable.onboard1),
        painterResource(id = R.drawable.onboard2),
        painterResource(id = R.drawable.onboard3)
    )

    val listState = rememberLazyListState()
    val currentPage = remember { derivedStateOf { listState.firstVisibleItemIndex } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.Center
        ) {
            itemsIndexed(pages) { _, painter ->
                OnboardingPage(painter)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            pages.forEachIndexed { index, _ ->
                val isSelected = index == currentPage.value
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 12.dp else 8.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.small
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onDone) {
                Text("Skip")
            }
            Button(onClick = onDone) {
                Text(if (currentPage.value == pages.lastIndex) "Get Started" else "Next")
            }
        }
    }
}

@Composable
fun OnboardingPage(image: Painter) {
    Image(
        painter = image,
        contentDescription = "Onboarding image",
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}
