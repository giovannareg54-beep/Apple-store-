package com.example.ui.vm

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VirtualOSContainer(
    viewModel: VirtualOSViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Battery & WiFi system signals simulating
    var batteryPercent by remember { mutableIntStateOf(92) }
    var signalStrength by remember { mutableIntStateOf(4) }
    var bootProgressLogIndex by remember { mutableIntStateOf(0) }

    // Tick real clock on status bars
    var currentTimeString by remember { mutableStateOf("00:00") }

    LaunchedEffect(Unit) {
        // Clock Ticker
        while (true) {
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            currentTimeString = formatter.format(Date())
            delay(1000)
        }
    }

    // Incremental boot console logs
    val bootLogsList = listOf(
        "Bootloader initialization... OK",
        "Loading CPU Microcode... Tensor V3 virtual",
        "Validating system integrity checksums... OK",
        "Checking security state... SECURE_MODE_ROOT_OFF",
        "Dumping storage mapping: Ext4 virtual disk... OK",
        "Starting Android Runtime engine components...",
        "Allocating heap memory size... ${viewModel.virtualRamGb} GB OK",
        "Zygote process spawned... ID 953",
        "Starting Package Manager & System UI launcher...",
        "Virtual Machine booted entirely. Launching launcher..."
    )

    LaunchedEffect(viewModel.isBooting) {
        if (viewModel.isBooting) {
            bootProgressLogIndex = 0
            while (bootProgressLogIndex < bootLogsList.size - 1) {
                delay(300)
                bootProgressLogIndex++
            }
            delay(600)
            viewModel.finishBooting()
        }
    }

    // Physical Phone Device Layout wrapping the content
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E272C)) // Dark background surrounding the phone
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Entire Phone Bezel Box Frame
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxHeight()
                .shadow(24.dp, shape = RoundedCornerShape(36.dp))
                .border(6.dp, Color(0xFF2C3E50), RoundedCornerShape(36.dp)) // Chrome/Steel bezel ring
                .background(Color.Black, RoundedCornerShape(36.dp))
                .padding(8.dp) // Front glass bezel thickness
        ) {
            // Screen Viewport Mask Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black)
            ) {
                // If power is completely off, show black screen
                if (!viewModel.isPowerOn) {
                    PowerOffScreen(onPowerClick = { viewModel.pressPowerButton() })
                } else if (viewModel.isBooting) {
                    // Running booting sequences
                    BootLoaderConsoleScreen(bootLogsList.take(bootProgressLogIndex + 1))
                } else {
                    // Running System view
                    ImageWallpaperBackground(index = viewModel.selectedWallpaperIndex) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top StatusBar Area
                            VirtualStatusBar(
                                timeString = currentTimeString,
                                batteryPercent = batteryPercent,
                                isRoot = viewModel.rootAccess
                            )

                            // Actual viewport routing content
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                when (val route = viewModel.currentRoute) {
                                    is VMRoute.LockScreen -> {
                                        VirtualLockScreen(
                                            timeString = currentTimeString,
                                            onUnlock = { viewModel.unlockScreen() }
                                        )
                                    }

                                    is VMRoute.HomeScreen -> {
                                        VirtualHomeScreen(viewModel = viewModel)
                                    }

                                    is VMRoute.AppOpen -> {
                                        VirtualAppViewport(appId = route.appId, viewModel = viewModel)
                                    }
                                    else -> {}
                                }
                            }

                            // Bottom Soft Navigation Drawer Row
                            VirtualBottomNavBar(
                                onBackClick = { viewModel.navigateBack() },
                                onHomeClick = { viewModel.navigateHome() },
                                onPowerOffClick = { viewModel.pressPowerButton() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PowerOffScreen(onPowerClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C3E50))
                    .clickable { onPowerClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PowerSettingsNew,
                    contentDescription = "Power VM On",
                    tint = Color(0xFFE74C3C),
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "MÁQUINA VIRTUAL DESLIGADA",
                color = Color.DarkGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                "Aperte o botão vermelhos para ligar a VM",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun BootLoaderConsoleScreen(logs: List<String>) {
    val scrollState = rememberScrollState()
    LaunchedEffect(logs.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F171A))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "=== SYS-VM SECURE BOOT v6.1 ===",
                color = Color(0xFFFFCC00),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            logs.forEach { log ->
                Text(
                    text = log,
                    color = Color(0xFF00FFCC),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                color = Color(0xFF00FFCC),
                strokeWidth = 2.dp,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ImageWallpaperBackground(
    index: Int,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getWallpaperGradient(index))
    ) {
        content()
    }
}

@Composable
fun VirtualStatusBar(
    timeString: String,
    batteryPercent: Int,
    isRoot: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Widgets
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                timeString,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.W600,
                fontFamily = FontFamily.SansSerif
            )
            if (isRoot) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(Color.Red, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("ROOT", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Right Indicators
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.SignalCellular4Bar,
                contentDescription = "Signal",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Filled.Wifi,
                contentDescription = "Wifi",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "$batteryPercent%",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Filled.BatteryChargingFull,
                contentDescription = "Battery charging",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun VirtualLockScreen(
    timeString: String,
    onUnlock: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                timeString,
                fontSize = 72.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                letterSpacing = (-1).sp
            )
            Text(
                "Segunda, 25 de Maio",
                fontSize = 16.sp,
                color = Color.LightGray,
                fontWeight = FontWeight.W400
            )

            Spacer(modifier = Modifier.height(180.dp))

            // Pulse slide handle unlock
            Button(
                onClick = onUnlock,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LockOpen,
                        contentDescription = "Unlock",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DESBLOQUEAR VM", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VirtualHomeScreen(viewModel: VirtualOSViewModel) {
    var showAppDrawer by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Grid Home shortcuts
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // List of apps currently placed on home (any app in installedApps list)
                val homeConfig = listOf(
                    HomeScreenApp("play_store", "Play Store", "play_store"),
                    HomeScreenApp("dialer", "Telefone", "dialer"),
                    HomeScreenApp("settings", "Configurar", "settings"),
                    HomeScreenApp("calculator", "Calculadora", "calculator"),
                    HomeScreenApp("terminal", "Termux", "terminal"),
                    HomeScreenApp("gallery", "Galeria", "gallery"),
                    HomeScreenApp("camera", "Câmera", "camera"),
                    HomeScreenApp("messenger", "Mensagens", "messenger")
                )

                // Only show if installed
                items(homeConfig.filter { viewModel.installedApps.contains(it.id) }) { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { viewModel.openApp(item.id) }
                    ) {
                        AdaptiveLauncherIcon(
                            appId = item.iconKey,
                            contentDescription = item.label,
                            size = 52.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            item.label,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }

            // Bottom drawer pill launcher
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // App Drawer arrow launcher
                IconButton(onClick = { showAppDrawer = true }) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Open Drawer",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    "Todos os Apps",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Standard Dock at home bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dockApps = listOf(
                        HomeScreenApp("play_store", "Play Store", "play_store"),
                        HomeScreenApp("dialer", "Telefone", "dialer"),
                        HomeScreenApp("messenger", "Mensagens", "messenger"),
                        HomeScreenApp("settings", "Configurar", "settings")
                    )

                    dockApps.forEach { item ->
                        val isInst = viewModel.installedApps.contains(item.id)
                        if (isInst) {
                            Box(
                                modifier = Modifier
                                    .clickable { viewModel.openApp(item.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                AdaptiveLauncherIcon(
                                    appId = item.iconKey,
                                    contentDescription = item.label,
                                    size = 46.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Full App Drawer Overlay Sheet
        AnimatedVisibility(
            visible = showAppDrawer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            AppDrawerDialog(
                viewModel = viewModel,
                onClose = { showAppDrawer = false }
            )
        }
    }
}

@Composable
fun AppDrawerDialog(
    viewModel: VirtualOSViewModel,
    onClose: () -> Unit
) {
    val allPossibleApps = listOf(
        HomeScreenApp("play_store", "Play Store", "play_store"),
        HomeScreenApp("dialer", "Telefone", "dialer"),
        HomeScreenApp("settings", "Configurações", "settings"),
        HomeScreenApp("calculator", "Calculadora", "calculator"),
        HomeScreenApp("terminal", "Termux Terminal", "terminal"),
        HomeScreenApp("gallery", "Galeria Fotos", "gallery"),
        HomeScreenApp("camera", "Câmera", "camera"),
        HomeScreenApp("messenger", "SMS Messenger", "messenger")
    )

    // Filter list to show installed only
    val installedList = allPossibleApps.filter { viewModel.installedApps.contains(it.id) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onClose() }
            .padding(top = 80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E272C), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clickable(enabled = false) {} // block click close
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "MÁQUINA VIRTUAL APPS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Filled.KeyboardArrowDown, contentDescription = "Close", tint = Color.White)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(installedList) { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            viewModel.openApp(item.id)
                            onClose()
                        }
                    ) {
                        AdaptiveLauncherIcon(
                            appId = item.iconKey,
                            contentDescription = item.label,
                            size = 52.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            item.label,
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VirtualAppViewport(
    appId: String,
    viewModel: VirtualOSViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when (appId) {
            "play_store" -> PlayStoreScreen(viewModel = viewModel)
            "terminal" -> TerminalEmulatorScreen(viewModel = viewModel)
            "dialer" -> DialerScreen(viewModel = viewModel)
            "messenger" -> MessengerScreen(viewModel = viewModel)
            "settings" -> SettingsScreen(viewModel = viewModel)
            "gallery" -> GalleryScreen(viewModel = viewModel)
            "camera" -> CameraScreen(viewModel = viewModel)
            "calculator" -> CalculatorScreen(viewModel = viewModel)
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Erro: App $appId não instanciado na VM", color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun VirtualBottomNavBar(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onPowerOffClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back soft arrow (triangle)
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Filled.ArrowBack, // simple directional back indicator
                contentDescription = "Back",
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }

        // Home soft circle
        IconButton(onClick = onHomeClick) {
            Icon(
                imageVector = Icons.Filled.RadioButtonChecked,
                contentDescription = "Home",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Power shut / recent toggle icon (square backplane layout)
        IconButton(onClick = onPowerOffClick) {
            Icon(
                imageVector = Icons.Filled.PowerSettingsNew,
                contentDescription = "Shut down",
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

data class HomeScreenApp(val id: String, val label: String, val iconKey: String)
