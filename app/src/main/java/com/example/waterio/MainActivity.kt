package com.example.waterio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WaterDashboardScreen()
                }
            }
        }
    }
}

@Composable
fun WaterDashboardScreen() {
    var currentWater by remember { mutableStateOf(0) }
    val dailyGoal = 2500
    val progress = (currentWater.toFloat() / dailyGoal).coerceIn(0f, 1f)

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val hasInternet = true

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "WATERIO",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Twój cel: $dailyGoal ml", fontSize = 18.sp)

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.size(180.dp),
                strokeWidth = 16.dp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Wypito: $currentWater ml", fontSize = 24.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(48.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        currentWater += 250
                        coroutineScope.launch {
                            val msg = if (hasInternet) "Zapisano 250ml online!" else "Zapisano offline!"
                            snackbarHostState.showSnackbar(msg)
                        }
                    },
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("+ 250 ml (Szklanka)")
                }

                Button(
                    onClick = {
                        currentWater += 500
                        coroutineScope.launch {
                            val msg = if (hasInternet) "Zapisano 500ml online!" else "Zapisano offline!"
                            snackbarHostState.showSnackbar(msg)
                        }
                    },
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("+ 500 ml (Butelka)")
                }
            }
        }
    }
}