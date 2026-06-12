package com.example.waterio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import androidx.room.Room
import com.example.waterio.data.AppDatabase
import com.example.waterio.data.TokenManager
import com.example.waterio.network.WaterApiService
import com.example.waterio.ui.AuthState
import com.example.waterio.ui.WaterViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "water-database")
            .fallbackToDestructiveMigration().build()
        val tokenManager = TokenManager(applicationContext)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(WaterApiService::class.java)

        val viewModel = WaterViewModel(db.waterDao(), api, tokenManager, applicationContext)

        setContent {
            MaterialTheme {
                AppNavigation(viewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: WaterViewModel) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState == AuthState.SUCCESS) {
            navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
        } else if (authState == AuthState.UNAUTHENTICATED) {
            navController.navigate("login") { popUpTo(0) }
        }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(viewModel) }
        composable("dashboard") { DashboardScreen(viewModel, navController) }
        composable("history") { HistoryScreen(viewModel, navController) }
        composable("stats") { StatsScreen(viewModel, navController) }
        composable("profile") { ProfileScreen(viewModel, navController) }
    }
}

// ======= 1. EKRAN LOGOWANIA / REJESTRACJI =======
@Composable
fun LoginScreen(viewModel: WaterViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val error by viewModel.errorMessage.collectAsState()
    val authState by viewModel.authState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("WaterIO 💧", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Hasło") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (error != null) {
            Text(error!!, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
        }

        if (authState == AuthState.LOADING) {
            CircularProgressIndicator()
        } else {
            Button(onClick = { viewModel.login(email, password) }, modifier = Modifier.fillMaxWidth()) { Text("Zaloguj się") }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { viewModel.register(email, password) }) { Text("Załóż nowe konto") }
        }
    }
}

// ======= 2. GLÓWNY DIALOG / DASHBOARD =======
@Composable
fun DashboardScreen(viewModel: WaterViewModel, navController: androidx.navigation.NavController) {
    val currentWater by viewModel.totalWater.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val streak by viewModel.streak.collectAsState()
    var customAmount by remember { mutableStateOf("") }

    val progress = (currentWater.toFloat() / dailyGoal).coerceIn(0f, 1f)

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Streak: 🔥 $streak dni", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.refreshData() }) { Icon(Icons.Default.Refresh, "Odśwież") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Wypito dzisiaj:", fontSize = 20.sp)
            Text("$currentWater / $dailyGoal ml", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(200.dp), strokeWidth = 16.dp)
            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { viewModel.addWater(250) }) { Text("+250ml 🥛") }
                Button(onClick = { viewModel.addWater(500) }) { Text("+500ml 🍾") }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = customAmount, onValueChange = { customAmount = it },
                    label = { Text("Inna ilość (ml)") }, modifier = Modifier.width(150.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    viewModel.addWater(customAmount.toIntOrNull() ?: 0)
                    customAmount = ""
                }) { Text("Dodaj") }
            }
        }
    }
}

// ======= 3. HISTORIA WPISÓW =======
@Composable
fun HistoryScreen(viewModel: WaterViewModel, navController: androidx.navigation.NavController) {
    val history by viewModel.history.collectAsState()

    Scaffold(bottomBar = { BottomNavigationBar(navController) }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Historia Nawodnienia", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(history) { entry ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Ilość: ${entry.amountMl} ml", fontWeight = FontWeight.Bold)
                                Text(if (entry.isSynced) "Zsynchronizowano ✨" else "Oczekuje offline ☁️", fontSize = 12.sp)
                            }
                            IconButton(onClick = { viewModel.deleteEntry(entry.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ======= 4. WYKRES STATYSTYK =======
@Composable
fun StatsScreen(viewModel: WaterViewModel, navController: androidx.navigation.NavController) {
    val stats by viewModel.stats.collectAsState()

    Scaffold(bottomBar = { BottomNavigationBar(navController) }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Statystyki Ostatnich Dni", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))

            // Uproszczony wykres kolumnowy w czystym Compose
            Row(
                modifier = Modifier.fillMaxWidth().height(250.dp).padding(16.dp),
                horizontalArrangement = Arrangement.spaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                stats.forEach { stat ->
                    val barHeight = (stat.totalMl / 3000f).coerceIn(0.1f, 1f) * 200
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${stat.totalMl}", fontSize = 10.sp)
                        Box(modifier = Modifier.width(30.dp).height(barHeight.dp).background(MaterialTheme.colorScheme.primary))
                        Text(stat.date.substring(5), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

// ======= 5. PROFIL / CEL DZIENNY =======
@Composable
fun ProfileScreen(viewModel: WaterViewModel, navController: androidx.navigation.NavController) {
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    var newGoal by remember { mutableStateOf(dailyGoal.toString()) }

    Scaffold(bottomBar = { BottomNavigationBar(navController) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Twój Profil", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(value = newGoal, onValueChange = { newGoal = it }, label = { Text("Dzienny cel (ml)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.updateGoal(newGoal.toIntOrNull() ?: 2000) }, modifier = Modifier.fillMaxWidth()) {
                Text("Zapisz Cel")
            }
            Spacer(modifier = Modifier.height(48.dp))
            Button(onClick = { viewModel.logout() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.ExitToApp, "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Wyloguj się")
            }
        }
    }
}

// ======= KOMPONENT SZEFA: DOLNA NAWIGACJA =======
@Composable
fun BottomNavigationBar(navController: androidx.navigation.NavController) {
    NavigationBar {
        NavigationBarItem(icon = { Icon(Icons.Default.Home, "Główna") }, label = { Text("Panel") }, selected = false, onClick = { navController.navigate("dashboard") })
        NavigationBarItem(icon = { Icon(Icons.Default.List, "Historia") }, label = { Text("Historia") }, selected = false, onClick = { navController.navigate("history") })
        NavigationBarItem(icon = { Icon(Icons.Default.BarChart, "Statystyki") }, label = { Text("Wykres") }, selected = false, onClick = { navController.navigate("stats") })
        NavigationBarItem(icon = { Icon(Icons.Default.Person, "Profil") }, label = { Text("Profil") }, selected = false, onClick = { navController.navigate("profile") })
    }
}