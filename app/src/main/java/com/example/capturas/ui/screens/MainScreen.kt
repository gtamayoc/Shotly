package com.example.capturas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.capturas.R



@Composable
fun MainScreen(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val msg = if (hasPermission) {
        stringResource(id = R.string.granted_msg)
    } else {
        stringResource(id = R.string.not_granted_msg)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = msg)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onRequestPermission() },
                enabled = !hasPermission,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
            ) {
                Text(text = stringResource(id = R.string.request_button))
            }
        }
    }
}
