package com.jamstudios.amf_example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.jamstudios.amf_example.ui.components.SpectrogramView
import com.jamstudios.amf_example.ui.theme.AudioMicrofrontendExampleTheme

@Composable
fun MainScreen(
    data: FloatArray,
    width: Int,
    height: Int,
    isRunning: Boolean = false,
    onAppear: () -> Unit = {},
    onTryAgainClicked: () -> Unit = {}
){
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {

            Text(
                text = "Audio Microfrontend",
                fontSize = 30.sp
            )

            SpectrogramView(
                modifier = Modifier
                    .fillMaxHeight(fraction = 0.8f)
                    .fillMaxWidth(),
                data = data,
                width = width,
                height = height
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onTryAgainClicked,
                    enabled = !isRunning,
                    content = {
                        Text("Try again")
                    }
                )
            }
        }
    }

    LaunchedEffect(Unit) { onAppear() }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AudioMicrofrontendExampleTheme {
        MainScreen(
            data = FloatArray(40*49){ 0f },
            width = 40,
            height = 49
        )
    }
}