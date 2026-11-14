package com.pixel.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import java.text.SimpleDateFormat
import java.util.*

data class AppInfo(val label: String, val packageName: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LauncherScreen()
        }
    }
}

@Composable
fun LauncherScreen() {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    val time = remember { mutableStateOf(getCurrentTime()) }

    // Обновляем время каждую минуту
    LaunchedEffect(Unit) {
        while (true) {
            time.value = getCurrentTime()
            kotlinx.coroutines.delay(1000)
        }
    }

    // Загружаем приложения
    LaunchedEffect(Unit) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        
        apps = resolveInfos.mapNotNull { ri ->
            if (ri.activityInfo.packageName == context.packageName) null
            else AppInfo(
                label = ri.loadLabel(pm).toString(),
                packageName = ri.activityInfo.packageName
            )
        }.sortedBy { it.label.lowercase() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Часы
        Spacer(modifier = Modifier.height(60.dp))
        Text(
            text = time.value,
            color = Color.White,
            fontSize = 72.sp,
            fontWeight = FontWeight.Thin
        )
        Text(
            text = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()),
            color = Color.Gray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Сетка приложений
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(apps) { app ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                        context.startActivity(launchIntent)
                    }
                ) {
                    // Иконка-заглушка (круг)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2A2A2A))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = app.label,
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

fun getCurrentTime(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
