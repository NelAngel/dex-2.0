package com.sdlygane.tv

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val Bg = Color(0xFF090D14)
private val Panel = Color(0xFF151D29)
private val Muted = Color(0xFF9BAABD)
private val Accent = Color(0xFF70D6C2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9, 13, 20)
        window.navigationBarColor = android.graphics.Color.rgb(9, 13, 20)
        setContent { SDLYGANEApp() }
    }
}

private data class InstalledApp(
    val label: String,
    val packageName: String,
    val icon: Bitmap,
    val isGame: Boolean
)

private enum class Section(val title: String) {
    HOME("Inicio"), APPS("Aplicaciones"), GAMES("Juegos"), SETTINGS("Configuración")
}

@Composable
private fun SDLYGANEApp() {
    val context = LocalContext.current
    var section by remember { mutableStateOf(Section.HOME) }
    var apps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var search by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        apps = withContext(Dispatchers.IO) { loadLaunchableApps(context.packageManager) }
        loading = false
    }

    val filtered = apps.filter {
        it.label.contains(search, ignoreCase = true) || it.packageName.contains(search, ignoreCase = true)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Bg) {
        Row(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF172333), Bg, Color(0xFF080A0F))))) {
            NavigationRail(section = section, onSelect = { section = it })
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 30.dp, vertical = 22.dp)) {
                Header()
                Spacer(Modifier.height(24.dp))
                when (section) {
                    Section.HOME -> HomeScreen(
                        appCount = apps.size,
                        onOpenApps = { section = Section.APPS },
                        onOpenGames = { section = Section.GAMES },
                        onLaunch = { launchNamedApp(context.packageManager, context, it) { message = it } }
                    )
                    Section.APPS -> AppLibrary(
                        title = "Todas tus aplicaciones", apps = filtered, loading = loading,
                        search = search, onSearch = { search = it }, onLaunch = { launchApp(context, it) { message = it } }
                    )
                    Section.GAMES -> AppLibrary(
                        title = "Tu biblioteca de juegos", apps = filtered.filter { it.isGame }, loading = loading,
                        search = search, onSearch = { search = it }, onLaunch = { launchApp(context, it) { message = it } }
                    )
                    Section.SETTINGS -> SettingsScreen(appCount = apps.size)
                }
            }
        }
    }
    message?.let { text ->
        AlertDialog(onDismissRequest = { message = null }, confirmButton = {
            TextButton(onClick = { message = null }) { Text("Aceptar") }
        }, title = { Text("SD LYGANE") }, text = { Text(text) })
    }
}

@Composable
private fun NavigationRail(section: Section, onSelect: (Section) -> Unit) {
    Column(
        modifier = Modifier.width(190.dp).fillMaxHeight().background(Color(0xCC0D131D)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("SD", color = Accent, fontSize = 32.sp, fontWeight = FontWeight.Black)
        Text("LYGANE", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(22.dp))
        Section.values().forEach { item ->
            val selected = section == item
            Text(
                text = (if (selected) "●  " else "   ") + item.title,
                color = if (selected) Accent else Muted,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(if (selected) Color(0xFF203A43) else Color.Transparent)
                    .clickable { onSelect(item) }.focusable().padding(horizontal = 12.dp, vertical = 14.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        Text("SAMSUNG DeX READY", color = Color(0xFF6F8298), fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun Header() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("CENTRO DE ENTRETENIMIENTO", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
            Text("Tu pantalla. Tu espacio.", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }
        Text("SD LYGANE  •  0.2.0", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HomeScreen(appCount: Int, onOpenApps: () -> Unit, onOpenGames: () -> Unit, onLaunch: (String) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxWidth().weight(0.48f).clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF214A55), Color(0xFF17263A), Color(0xFF25213C))))
                .padding(28.dp), contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text("BIENVENIDO A SD LYGANE", color = Accent, fontSize = 11.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(12.dp))
                Text("Entretenimiento\nsin complicaciones.", color = Color.White, fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(10.dp))
                Text("Tus aplicaciones y juegos disponibles desde una sola pantalla.", color = Color(0xFFD1DBE5), fontSize = 14.sp)
                Spacer(Modifier.height(18.dp))
                Text("$appCount aplicaciones detectadas", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("ACCESO RÁPIDO", color = Muted, fontSize = 11.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            FeatureCard("YouTube", "Videos y canales", "▶", listOf(Color(0xFF7C2028), Color(0xFF3D1824)), Modifier.weight(1f)) { onLaunch("com.google.android.youtube") }
            FeatureCard("Netflix", "Películas y series", "N", listOf(Color(0xFF6D101D), Color(0xFF24131D)), Modifier.weight(1f)) { onLaunch("com.netflix.mediaclient") }
            FeatureCard("Juegos", "Tu biblioteca", "🎮", listOf(Color(0xFF214C70), Color(0xFF17263B)), Modifier.weight(1f), onGames = onOpenGames)
            FeatureCard("Aplicaciones", "Explorar $appCount apps", "▦", listOf(Color(0xFF315A58), Color(0xFF1C3439)), Modifier.weight(1f), onGames = onOpenApps)
        }
    }
}

@Composable
private fun FeatureCard(title: String, subtitle: String, symbol: String, colors: List<Color>, modifier: Modifier, onGames: (() -> Unit)? = null, onClick: (() -> Unit)? = null) {
    var focused by remember { mutableStateOf(false) }
    Column(
        modifier.height(145.dp).clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(colors))
            .then(if (focused) Modifier.border(2.dp, Accent, RoundedCornerShape(18.dp)) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .clickable { (onGames ?: onClick)?.invoke() }.focusable().padding(17.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(symbol, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Column {
            Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFFD2DCE5), fontSize = 11.sp)
        }
    }
}

@Composable
private fun AppLibrary(
    title: String, apps: List<InstalledApp>, loading: Boolean, search: String,
    onSearch: (String) -> Unit, onLaunch: (InstalledApp) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text(title, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = search, onValueChange = onSearch, singleLine = true,
            placeholder = { Text("Buscar aplicación…", color = Muted) },
            modifier = Modifier.fillMaxWidth(0.55f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedBorderColor = Accent, unfocusedBorderColor = Color(0xFF435165), cursorColor = Accent
            )
        )
        Spacer(Modifier.height(16.dp))
        when {
            loading -> Text("Cargando aplicaciones…", color = Muted)
            apps.isEmpty() -> Text("No se encontraron aplicaciones en esta categoría.", color = Muted)
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 155.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(apps, key = { it.packageName }) { app -> AppCard(app, onClick = { onLaunch(app) }) }
            }
        }
    }
}

@Composable
private fun AppCard(app: InstalledApp, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().height(82.dp).clip(RoundedCornerShape(15.dp))
            .background(if (focused) Color(0xFF263B48) else Panel)
            .then(if (focused) Modifier.border(2.dp, Accent, RoundedCornerShape(15.dp)) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && (event.key == Key.Enter || event.key == Key.NumPadEnter || event.key == Key.DirectionCenter)) { onClick(); true } else false
            }
            .focusable().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(bitmap = app.icon.asImageBitmap(), contentDescription = null, modifier = Modifier.size(42.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(app.label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(if (app.isGame) "Juego" else "Aplicación", color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SettingsScreen(appCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Configuración", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        SettingRow("Dispositivo", "Android / Samsung Galaxy")
        SettingRow("Modo de pantalla", "Interfaz adaptable para Samsung DeX")
        SettingRow("Aplicaciones detectadas", appCount.toString())
        SettingRow("Versión", "0.2.0 · Desarrollo")
        Spacer(Modifier.height(8.dp))
        Text("Al conectar la pantalla HDMI, abre SD LYGANE desde Samsung DeX. Android mantiene el control del escritorio y de la selección de aplicaciones.", color = Muted, fontSize = 13.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun SettingRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Panel).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted, fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun loadLaunchableApps(pm: PackageManager): List<InstalledApp> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, 0).mapNotNull { resolve ->
        val info = resolve.activityInfo.applicationInfo
        val packageName = resolve.activityInfo.packageName
        if (packageName == "com.sdlygane.tv") return@mapNotNull null
        try {
            val drawable = pm.getApplicationIcon(info)
            InstalledApp(
                label = resolve.loadLabel(pm).toString(), packageName = packageName,
                icon = drawable.toBitmap(), isGame = info.category == ApplicationInfo.CATEGORY_GAME
            )
        } catch (_: Exception) { null }
    }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
}

private fun Drawable.toBitmap(): Bitmap {
    val width = intrinsicWidth.coerceAtLeast(1).coerceAtMost(192)
    val height = intrinsicHeight.coerceAtLeast(1).coerceAtMost(192)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

private fun launchApp(activity: ComponentActivity, app: InstalledApp, showMessage: (String) -> Unit) {
    launchNamedApp(activity.packageManager, activity, app.packageName, showMessage)
}

private fun launchNamedApp(pm: PackageManager, activity: ComponentActivity, packageName: String, showMessage: (String) -> Unit) {
    try {
        val intent = pm.getLaunchIntentForPackage(packageName)
        if (intent == null) showMessage("Esta aplicación no tiene una actividad de inicio disponible.")
        else activity.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (error: Exception) {
        showMessage("No se pudo abrir la aplicación: ${error.localizedMessage ?: "error desconocido"}")
    }
}
