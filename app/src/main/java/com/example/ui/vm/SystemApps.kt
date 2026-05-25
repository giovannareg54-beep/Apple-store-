package com.example.ui.vm

import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================
// 1. PLAY STORE SIMULATOR SCREEN
// ==========================================
@Composable
fun PlayStoreScreen(
    viewModel: VirtualOSViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Apps, 1: Utilitários, 2: Biblioteca
    val coroutineScope = rememberCoroutineScope()

    // Download/Install progresses states
    var installingAppId by remember { mutableStateOf<String?>(null) }
    var installProgress by remember { mutableFloatStateOf(0f) }

    val allStoreApps = listOf(
        StoreApp("messenger", "SMS Messenger VM", "Mensagens instantâneas e bots inteligentes.", "messenger"),
        StoreApp("terminal", "Termux Console VM", "Terminal Linux virtual com comandos interativos.", "terminal"),
        StoreApp("gallery", "Galeria & Wallpapers", "Gerencie suas fotos e altere planos de fundo.", "gallery"),
        StoreApp("camera", "Câmera Digital", "Simulador fotográfico com filtros cibernéticos.", "camera"),
        StoreApp("calculator", "Calculadora M3", "Operações matemáticas e layout original.", "calculator"),
        StoreApp("dialer", "Telefone & Dialer OS", "Teclado numérico e diagnóstico secreto *#0*#.", "dialer")
    )

    val filteredApps = allStoreApps.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.desc.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Play Store Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .background(Color(0xFFF1F3F4), RoundedCornerShape(24.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = Color(0xFF5F6368),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text("Pesquisar apps da Play Store...", color = Color.Gray, fontSize = 14.sp)
                    }
                    innerTextField()
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Profile circular letter
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF1A73E8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Tabs (Category selectors)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val tabs = listOf("Para você", "Utilitários", "Biblioteca Virtual")
            tabs.forEachIndexed { index, title ->
                val active = selectedTab == index
                Column(
                    modifier = Modifier
                        .clickable { selectedTab = index }
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        color = if (active) Color(0xFF01875F) else Color.Gray,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(40.dp)
                            .background(if (active) Color(0xFF01875F) else Color.Transparent)
                    )
                }
            }
        }

        Divider(color = Color(0xFFE0E0E0))

        // Store active content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (installingAppId != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Instalando aplicativo na VM...", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { installProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF01875F),
                                trackColor = Color(0xFFC8E6C9)
                            )
                        }
                    }
                }
            }

            items(filteredApps) { app ->
                val isInstalled = viewModel.installedApps.contains(app.id)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AdaptiveLauncherIcon(
                        appId = app.iconKey,
                        contentDescription = app.name,
                        size = 52.dp,
                        modifier = Modifier.padding(end = 12.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(app.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                        Text(app.desc, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                        Text("Classificação: ★ 4.9 • 15 MB", fontSize = 10.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (isInstalled) {
                        // Uninstall button (Play Store original design)
                        OutlinedButton(
                            onClick = { viewModel.uninstallApp(app.id) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF01875F)),
                            border = BorderStroke(1.dp, Color(0xFFDACFDF).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                        ) {
                            Text("Remover", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Install Button
                        Button(
                            onClick = {
                                if (installingAppId == null) {
                                    coroutineScope.launch {
                                        installingAppId = app.id
                                        installProgress = 0f
                                        while (installProgress < 1.0f) {
                                            delay(150)
                                            installProgress += 0.1f
                                        }
                                        viewModel.installApp(app.id)
                                        installingAppId = null
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F)),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                        ) {
                            Text("Instalar", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

data class StoreApp(val id: String, val name: String, val desc: String, val iconKey: String)

// Simple Text Field overlay to avoid complex material packaging
@Composable
fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        decorationBox = { inner -> decorationBox(inner) }
    )
}

// ==========================================
// 2. TERMUX TERMINAL EMULATOR
// ==========================================
@Composable
fun TerminalEmulatorScreen(
    viewModel: VirtualOSViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Auto-scroll on terminals
    LaunchedEffect(viewModel.terminalHistory.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(8.dp)
    ) {
        // TOP Header Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Terminal,
                    contentDescription = null,
                    tint = Color(0xFF39FF14),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Termux VM shell (sh)", fontFamily = FontFamily.Monospace, color = Color.White, fontSize = 11.sp)
            }
            Text(
                text = if (viewModel.rootAccess) "root@sysvm" else "sysvm@android",
                fontFamily = FontFamily.Monospace,
                color = if (viewModel.rootAccess) Color.Red else Color(0xFF39FF14),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 6.dp))

        // Simulated Command Log viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Column {
                viewModel.terminalHistory.forEach { line ->
                    val color = when {
                        line.startsWith("sysvm@") -> Color(0xFF3498DB)
                        line.startsWith("root@") -> Color(0xFFE74C3C)
                        line.startsWith("Erro") -> Color(0xFFFF4444)
                        line.contains("-rwxr") || line.contains("d--r") -> Color(0xFFFFD700)
                        else -> Color(0xFF39FF14)
                    }
                    Text(
                        text = line,
                        fontFamily = FontFamily.Monospace,
                        color = color,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }

        // Terminal Tool bar (like native Termux: CTRL, ALT, TAB, etc.)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF222222))
                .padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val tools = listOf("ESC", "TAB", "CTRL", "ALT", "/", "-", "_", "CLEAR")
            tools.forEach { tool ->
                Box(
                    modifier = Modifier
                        .background(Color(0xFF333333), RoundedCornerShape(4.dp))
                        .clickable {
                            when (tool) {
                                "CLEAR" -> viewModel.terminalHistory = listOf("sysvm@android:~$ ")
                                "TAB" -> viewModel.currentTerminalInput += "\t"
                                else -> viewModel.currentTerminalInput += " " + tool.lowercase(Locale.ROOT)
                            }
                        }
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tool,
                        fontFamily = FontFamily.Monospace,
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Command Prompt Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (viewModel.rootAccess) "# " else "$ ",
                fontFamily = FontFamily.Monospace,
                color = if (viewModel.rootAccess) Color.Red else Color(0xFF39FF14),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            androidx.compose.foundation.text.BasicTextField(
                value = viewModel.currentTerminalInput,
                onValueChange = { viewModel.currentTerminalInput = it },
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        viewModel.executeTerminalCommand(viewModel.currentTerminalInput)
                    }
                ),
                modifier = Modifier.weight(1f),
                cursorBrush = Brush.verticalGradient(listOf(Color(0xFF39FF14), Color(0xFF39FF14)))
            )

            IconButton(
                onClick = { viewModel.executeTerminalCommand(viewModel.currentTerminalInput) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Send",
                    tint = Color(0xFF39FF14),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ==========================================
// 3. DIALER & SECRET DIAGNOSTIC HARDWARE CODE
// ==========================================
@Composable
fun DialerScreen(
    viewModel: VirtualOSViewModel,
    modifier: Modifier = Modifier
) {
    if (viewModel.isShowingHardwareDiagnosticTest) {
        HardwareDiagnosticView(viewModel)
    } else {
        DialerPadView(viewModel, modifier)
    }
}

@Composable
fun DialerPadView(
    viewModel: VirtualOSViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Dialer Display Input Screen
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = viewModel.dialerInput,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                maxLines = 1
            )

            // Hidden dynamic hooks
            LaunchedEffect(viewModel.dialerInput) {
                if (viewModel.dialerInput == "*#06#") {
                    viewModel.isShowingImeiDialog = true
                }
                if (viewModel.dialerInput == "*#0*#") {
                    viewModel.isShowingHardwareDiagnosticTest = true
                }
            }
        }

        // IMEI Dialog
        if (viewModel.isShowingImeiDialog) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.isShowingImeiDialog = false
                    viewModel.dialerInput = ""
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.isShowingImeiDialog = false
                        viewModel.dialerInput = ""
                    }) {
                        Text("OK")
                    }
                },
                title = { Text("IMEI") },
                text = {
                    Column {
                        Text("IMEI SV: 01", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("IMEI 1: 35849504-201944-2")
                        Text("IMEI 2: 35849504-201945-9")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Simulated Hardware SKU: G-VM330G", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            )
        }

        // Dialer Number Matrix Grids (3x4)
        val dialButtons = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#")
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            dialButtons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { digit ->
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF0F0F0))
                                .clickable {
                                    viewModel.dialerInput += digit
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = digit,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.W500,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }

            // Bottom commands (Call + Erase)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Erase Key
                IconButton(
                    onClick = {
                        if (viewModel.dialerInput.isNotEmpty()) {
                            viewModel.dialerInput = viewModel.dialerInput.dropLast(1)
                        }
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Backspace,
                        contentDescription = "Erase",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Call circular emerald button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF27AE60))
                        .clickable {
                            // Dial simulated operators
                            if (viewModel.dialerInput.isNotEmpty()) {
                                viewModel.dialerInput = "Chamando VM Operator..."
                                val h = android.os.Handler(android.os.Looper.getMainLooper())
                                h.postDelayed({
                                    viewModel.dialerInput = ""
                                }, 2000)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = "Dial",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Close Dialer
                IconButton(
                    onClick = { viewModel.dialerInput = "" },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ClearAll,
                        contentDescription = "Clear Pad",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// SAMSUNG/PIXEL RETRO HARDWARE DIAGNOSTICS Easter Egg
// ==========================================
@Composable
fun HardwareDiagnosticView(viewModel: VirtualOSViewModel) {
    val state = viewModel.currentDiagnosticTestState

    if (state != null) {
        // Individual full screen test viewport
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when (state) {
                        "RED" -> Color.Red
                        "GREEN" -> Color.Green
                        "BLUE" -> Color.Blue
                        "WHITE" -> Color.White
                        else -> Color.Black
                    }
                )
                .clickable {
                    viewModel.currentDiagnosticTestState = null
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (state == "VERSION") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("DIAGNOSTIC FIRMWARE BUILD", fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("HW Build ID: SYSVM_TEN_V3_026", color = Color.DarkGray)
                            Text("OS Target API Level: 36 (Android 16)", color = Color.DarkGray)
                            Text("Secure Knox Status: 0x0 (Genuine)", color = Color.DarkGray)
                            Text("Baseband: SH-VM_CORE_MODEM_2026", color = Color.DarkGray)
                        }
                    }
                } else if (state == "TOUCH") {
                    // Touch grid simulator
                    Text("Teste Touch Grid. Clique em qualquer local para voltar.", color = Color.DarkGray, fontSize = 12.sp)
                } else {
                    Text(
                        text = "TESTE $state\nToque para Voltar ao Menu",
                        color = if (state == "WHITE") Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } else {
        // Main Diagnostic Grid Menu
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE0E0E0))
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "HARDWARE TEST MENU",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Diagnostic options grid (3 columns)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                val diagButtons = listOf(
                    listOf("RED", "GREEN", "BLUE"),
                    listOf("VIBE", "TOUCH", "LIGHT"),
                    listOf("SPEAKER", "VERSION", "SLEEP")
                )

                diagButtons.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { btn ->
                            Box(
                                modifier = Modifier
                                    .size(width = 96.dp, height = 56.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .clickable {
                                        if (btn == "VIBE") {
                                            // Vibration test (Simulated popup brief)
                                            // In physical devices we would vibrate, we display custom toast simulation
                                        } else {
                                            viewModel.currentDiagnosticTestState = btn
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    btn,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            // Exit diagnostic mode button
            Button(
                onClick = {
                    viewModel.resetDiagnostic()
                    viewModel.dialerInput = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("SAIR DO DIAGNÓSTICO", color = Color.White)
            }
        }
    }
}

// ==========================================
// 4. MESSENGER / CHAT SCREEN
// ==========================================
@Composable
fun MessengerScreen(
    viewModel: VirtualOSViewModel,
    modifier: Modifier = Modifier
) {
    var activeContactId = viewModel.activeChatContactId
    var typedText by remember { mutableStateOf("") }
    val chatsListScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E88E5))
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Forum,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                "SMS Mensagens VM",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Horizontal Contacts list for switching chats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            viewModel.contacts.forEach { contact ->
                val isSelected = activeContactId == contact.id
                Card(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable { viewModel.activeChatContactId = contact.id },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = if (isSelected) BorderStroke(1.dp, Color(0xFF1E88E5)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(contact.avatarColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                contact.name.take(1),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(contact.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        Divider(color = Color(0xFFE0E0E0))

        if (activeContactId != null) {
            val history = viewModel.chatHistories[activeContactId] ?: emptyList()

            // Chat items list
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFECE5DD)) // classic whatsapp wallpaper color
                    .verticalScroll(chatsListScrollState)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // AutoScroll
                LaunchedEffect(history.size) {
                    chatsListScrollState.animateScrollTo(chatsListScrollState.maxValue)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    history.forEach { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (msg.isUser) 12.dp else 0.dp,
                                    bottomEnd = if (msg.isUser) 0.dp else 12.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (msg.isUser) Color(0xFFDCF8C6) else Color.White
                                ),
                                modifier = Modifier.widthIn(max = 240.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    if (!msg.isUser) {
                                        Text(
                                            msg.sender,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF075E54)
                                        )
                                    }
                                    Text(msg.text, fontSize = 13.sp, color = Color.DarkGray)
                                    Text(
                                        msg.time,
                                        fontSize = 9.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input Send Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = typedText,
                    onValueChange = { typedText = it },
                    placeholder = { Text("Digitar mensagem para VM...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1E88E5),
                        unfocusedBorderColor = Color(0xFFCCCCCC)
                    ),
                    maxLines = 2,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (typedText.isNotBlank()) {
                            viewModel.sendChatMessage(activeContactId, typedText)
                            typedText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF1E88E5), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. SETTINGS SCREEN
// ==========================================
@Composable
fun SettingsScreen(
    viewModel: VirtualOSViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // AppHeader
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2C3E50))
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            Column {
                Text(
                    "Configurações",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 22.sp
                )
                Text(
                    "Controle de Recursos da Máquina Virtual",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }

        // Section 1: Virtual hardware settings
        Text(
            "HARDWARE DA VM",
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // RAM allocation slider
                Text(
                    "Alocação de Memória RAM: ${String.format(Locale.ROOT, "%.1f", viewModel.virtualRamGb)} GB",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.Black
                )
                Slider(
                    value = viewModel.virtualRamGb,
                    onValueChange = { viewModel.virtualRamGb = it },
                    valueRange = 2f..16f,
                    steps = 6,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF2C3E50), activeTrackColor = Color(0xFF34495E))
                )
                Text(
                    "Alocar mais memória acelera a inicialização de emuladores dentro da VM.",
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Developer Options toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Modo Desenvolvedor", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                        Text("Habilitar dumps e ADB Virtual.", fontSize = 10.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = viewModel.developerOptions,
                        onCheckedChange = { viewModel.developerOptions = it }
                    )
                }
            }
        }

        // Section 2: Developer parameters
        if (viewModel.developerOptions) {
            Text(
                "MÓDULO DESENVOLVEDOR (ADVANCED)",
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Root Toggle Checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Acesso Privilegiado ROOT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                            Text("Requerido para comandos administrativos (su).", fontSize = 10.sp, color = Color.Gray)
                        }
                        Checkbox(
                            checked = viewModel.rootAccess,
                            onCheckedChange = { viewModel.rootAccess = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated CPU testing trigger
                    Button(
                        onClick = {
                            viewModel.terminalHistory = viewModel.terminalHistory + "Iniciando Teste de Benchmark VM CPU... OK! Stress test completo: U uptime normal."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E22)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("RODAR STRESS TEST DA CPU (STRESS)", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        // Section 3: Device Identity details
        Text(
            "SOBRE O TELEFONE VM",
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                AboutInfoRow("Modelo do Dispositivo", "Pixel Play Emulator G5")
                AboutInfoRow("Sistema operacional", "Virtual Android 16 (Release API 36)")
                AboutInfoRow("Versão Kernel", "Linux 6.1.0-sysvm (Cortex)")
                AboutInfoRow("Código do Hardware", "Tensor V3 Core @2.8Ghz")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

// ==========================================
// 6. GALLERY & WALLPAPER SCREEN
// ==========================================
@Composable
fun GalleryScreen(
    viewModel: VirtualOSViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedPhotoKey by remember { mutableStateOf<String?>(null) }

    // Grab all captured photos inside the simulated map filesystem
    val simulatedPhotosKeys = viewModel.fileSystem.keys.filter { it.startsWith("/sdcard/photos/") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // App header bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF009688))
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoLibrary,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text("Galeria de Fotos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        TabRow(selectedTabIndex = 0, containerColor = Color(0xFFE0F2F1)) {
            Tab(selected = true, onClick = {}, text = { Text("Wallpapers da VM", color = Color(0xFF004D40)) })
        }

        // Grid selection for Wallpapers
        Text(
            "Selecione um plano de fundo para aplicar à VM:",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val wallpapersNames = listOf("Ocean Cyber Blue", "Peach Sunset Gradient", "Console Terminal", "Meia-noite")
            wallpapersNames.forEachIndexed { index, name ->
                item {
                    val isSelected = viewModel.selectedWallpaperIndex == index
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clickable { viewModel.selectedWallpaperIndex = index },
                        shape = RoundedCornerShape(8.dp),
                        border = if (isSelected) BorderStroke(3.dp, Color(0xFF009688)) else null
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(getWallpaperGradient(index)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        Divider(color = Color(0xFFE0E0E0), modifier = Modifier.padding(vertical = 12.dp))

        // Grid for Camera Roll Snapshots
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Filled.PhotoCamera, contentDescription = null, tint = Color(0xFF009688))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Fotos Capturadas via VM Câmera:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
        }

        if (simulatedPhotosKeys.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.CloudQueue,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Nenhuma foto tirada. Vá para a Câmera!", fontSize = 11.sp, color = Color.Gray)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(simulatedPhotosKeys) { key ->
                    val rawData = viewModel.fileSystem[key] ?: ""
                    val filterName = if (rawData.contains("Normal")) "Normal" else if (rawData.contains("Monochrome")) "P&B" else "Matrix"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable { selectedPhotoKey = key },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    when (filterName) {
                                        "P&B" -> Brush.verticalGradient(listOf(Color.DarkGray, Color.LightGray))
                                        "Matrix" -> Brush.verticalGradient(listOf(Color(0xFF0D1B0E), Color(0xFF39FF14).copy(alpha = 0.5f)))
                                        else -> Brush.verticalGradient(listOf(Color(0xFFE0F2F1), Color(0xFF004D40).copy(alpha = 0.5f)))
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Filled.InsertPhoto, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(filterName, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Preview full-screen photo Dialog
        if (selectedPhotoKey != null) {
            val key = selectedPhotoKey ?: ""
            val rawDesc = viewModel.fileSystem[key] ?: ""
            AlertDialog(
                onDismissRequest = { selectedPhotoKey = null },
                confirmButton = {
                    TextButton(onClick = { selectedPhotoKey = null }) {
                        Text("Fechar")
                    }
                },
                title = { Text(key.substringAfterLast("/")) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(Color.Black.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Visualizador de Imagens VM", color = Color.White, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(rawDesc, fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            )
        }
    }
}

// Global gradient accessor for Wallpapers
fun getWallpaperGradient(index: Int): Brush {
    return when (index) {
        0 -> Brush.radialGradient(listOf(Color(0xFF0D47A1), Color(0xFF1565C0), Color(0xFF1A237E)))
        1 -> Brush.verticalGradient(listOf(Color(0xFFFF8A65), Color(0xFFFFCC80), Color(0xFF263238)))
        2 -> Brush.verticalGradient(listOf(Color(0xFF121212), Color(0xFF1E1E1E), Color(0xFF0F2610)))
        else -> Brush.verticalGradient(listOf(Color(0xFF3F51B5), Color(0xFF7986CB), Color(0xFF1A237E)))
    }
}

// ==========================================
// 7. CAMERA SIMULATOR SCREEN
// ==========================================
@Composable
fun CameraScreen(
    viewModel: VirtualOSViewModel,
    modifier: Modifier = Modifier
) {
    var activeFilter by remember { mutableStateOf("Normal") } // Normal, Monochrome, Matrix
    val coroutineScope = rememberCoroutineScope()
    var flashBlink by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Camera Viewport Simulation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (flashBlink) {
                        Color.White
                    } else {
                        when (activeFilter) {
                            "Monochrome" -> Color.DarkGray
                            "Matrix" -> Color(0xFF031004)
                            else -> Color(0xFF2C3E50)
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Lens crosshair overlay simulation
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height / 2
                val d = 60.dp.toPx()

                // Center Circle Target
                drawCircle(
                    color = if (activeFilter == "Matrix") Color(0xFF39FF14) else Color.White,
                    radius = d,
                    style = Stroke(2.dp.toPx())
                )

                // Guides
                drawLine(
                    color = if (activeFilter == "Matrix") Color(0xFF39FF14).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.5f),
                    start = Offset(cx - d * 2, cy),
                    end = Offset(cx + d * 2, cy),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = if (activeFilter == "Matrix") Color(0xFF39FF14).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.5f),
                    start = Offset(cx, cy - d * 2),
                    end = Offset(cx, cy + d * 2),
                    strokeWidth = 1.dp.toPx()
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = null,
                    tint = if (activeFilter == "Matrix") Color(0xFF39FF14) else Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "SIMULADOR DE LENTE (${activeFilter})",
                    color = if (activeFilter == "Matrix") Color(0xFF39FF14) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Filters selectors row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val filters = listOf("Normal", "Monochrome", "Matrix")
            filters.forEach { tag ->
                val active = activeFilter == tag
                Button(
                    onClick = { activeFilter = tag },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) Color.White else Color(0xFF222222)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                ) {
                    Text(
                        tag,
                        color = if (active) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Shooting Trigger and Gallery access
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF333333), CircleShape)
                    .clickable { viewModel.openApp("gallery") },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.PhotoLibrary, contentDescription = "Gallery", tint = Color.LightGray)
            }

            // Big White Circle capture trigger
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White, CircleShape)
                    .border(4.dp, Color.Gray, CircleShape)
                    .clickable {
                        coroutineScope.launch {
                            flashBlink = true
                            delay(100)
                            flashBlink = false
                            viewModel.requestCameraShot(activeFilter)
                        }
                    }
            )

            Box(modifier = Modifier.size(44.dp)) // spacer balance
        }
    }
}

// ==========================================
// 8. WORKING CALCULATOR SCREEN
// ==========================================
@Composable
fun CalculatorScreen(
    viewModel: VirtualOSViewModel,
    modifier: Modifier = Modifier
) {
    var calcScreen by remember { mutableStateOf("0") }
    var runningVal by remember { mutableDoubleStateOf(0.0) }
    var prevOperation by remember { mutableStateOf("") }
    var isStartingNewVal by remember { mutableStateOf(true) }

    val pad = listOf(
        listOf("C", "±", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "=")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Output result LCD Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                if (prevOperation.isNotEmpty()) "$runningVal $prevOperation" else "",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                calcScreen,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }

        // Calculation Pad matrix grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            pad.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { char ->
                        val isOp = char == "÷" || char == "×" || char == "-" || char == "+" || char == "="
                        val isClear = char == "C" || char == "±" || char == "%"

                        val btnColor = if (isOp) Color(0xFFF39C12) else if (isClear) Color(0xFFBDC3C7) else Color(0xFFECEFF1)
                        val txtColor = if (isOp || isClear) Color.White else Color.DarkGray
                        val weightValue = if (char == "0") 2f else 1f

                        Box(
                            modifier = Modifier
                                .weight(weightValue)
                                .aspectRatio(if (char == "0") 2f else 1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(btnColor)
                                .clickable {
                                    // Parser
                                    when (char) {
                                        "C" -> {
                                            calcScreen = "0"
                                            runningVal = 0.0
                                            prevOperation = ""
                                            isStartingNewVal = true
                                        }

                                        "+" -> {
                                            runningVal = calcScreen.toDoubleOrNull() ?: 0.0
                                            prevOperation = "+"
                                            isStartingNewVal = true
                                        }

                                        "-" -> {
                                            runningVal = calcScreen.toDoubleOrNull() ?: 0.0
                                            prevOperation = "-"
                                            isStartingNewVal = true
                                        }

                                        "×" -> {
                                            runningVal = calcScreen.toDoubleOrNull() ?: 0.0
                                            prevOperation = "×"
                                            isStartingNewVal = true
                                        }

                                        "÷" -> {
                                            runningVal = calcScreen.toDoubleOrNull() ?: 0.0
                                            prevOperation = "÷"
                                            isStartingNewVal = true
                                        }

                                        "=" -> {
                                            val sec = calcScreen.toDoubleOrNull() ?: 0.0
                                            val res = when (prevOperation) {
                                                "+" -> runningVal + sec
                                                "-" -> runningVal - sec
                                                "×" -> runningVal * sec
                                                "÷" -> if (sec != 0.0) runningVal / sec else 0.0
                                                else -> sec
                                            }
                                            // Format output nicely
                                            calcScreen =
                                                if (res == res.toLong().toDouble()) res.toLong().toString() else res.toString()
                                            prevOperation = ""
                                            isStartingNewVal = true
                                        }

                                        else -> {
                                            // Handle digit insertions
                                            if (isStartingNewVal && char != ".") {
                                                calcScreen = char
                                                isStartingNewVal = false
                                            } else {
                                                if (char == "." && calcScreen.contains(".")) {
                                                    // no-op
                                                } else {
                                                    calcScreen = if (calcScreen == "0" && char != ".") char else calcScreen + char
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                char,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = txtColor
                            )
                        }
                    }
                }
            }
        }
    }
}
