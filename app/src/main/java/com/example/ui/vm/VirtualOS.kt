package com.example.ui.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data model for messages/chat
data class ChatMessage(
    val sender: String,
    val text: String,
    val time: String,
    val isUser: Boolean
)

data class Contact(
    val id: String,
    val name: String,
    val initialMessage: String,
    val avatarColor: androidx.compose.ui.graphics.Color
)

// Main layout screen routes inside the Virtual Device
sealed class VMRoute {
    object BootScreen : VMRoute()
    object LockScreen : VMRoute()
    object HomeScreen : VMRoute()
    data class AppOpen(val appId: String) : VMRoute()
}

class VirtualOSViewModel : ViewModel() {
    // Hardware states of the simulated phone bezel
    var isPowerOn by mutableStateOf(false)
    var isBooting by mutableStateOf(false)
    var bootTimeLeft by mutableStateOf(10)
    var isScreenLocked by mutableStateOf(true)

    // Current route inside the Android OS
    var currentRoute by mutableStateOf<VMRoute>(VMRoute.BootScreen)
    var previousRoute by mutableStateOf<VMRoute>(VMRoute.BootScreen)

    // Active wallpaper index
    var selectedWallpaperIndex by mutableStateOf(0)

    // Settings States
    var rootAccess by mutableStateOf(false)
    var developerOptions by mutableStateOf(true)
    var virtualRamGb by mutableStateOf(8f)
    var animationsSpeed by mutableStateOf(1.0f)
    var currentThemeIndex by mutableStateOf(0) // 0: Auto, 1: Dark, 2: Light, 3: Cyberpunk

    // App Store - Installed App IDs state
    var installedApps by mutableStateOf(
        setOf("play_store", "dialer", "settings", "calculator")
    )

    // Simulated filesystem directories list
    var fileSystem by mutableStateOf(
        mutableMapOf(
            "/system/build.prop" to "ro.build.version.release=16\nro.product.model=Pixel VM\nro.hardware=sysvm.google.v3\nro.secure.permissions=root",
            "/sdcard/documents/readme.txt" to "Bem-vindo ao Virtual Android OS v16!\nEste e um ambiente virtual seguro.\nUse o Terminal/Termux para rodar comandos como 'neofetch', 'ls', 'mkdir'.",
            "/sdcard/photos/welcome.jpg" to "[SIMULATED_BITMAP_DATA_512KB]"
        )
    )

    // Messages App States
    val contacts = listOf(
        Contact("google_support", "Suporte Play Store", "Como posso ajudar com seus apps?", androidx.compose.ui.graphics.Color(0xFF4285F4)),
        Contact("sys_admin", "Virtual SysAdmin", "Olá! Terminal aberto para o núcleo.", androidx.compose.ui.graphics.Color(0xFF34A853)),
        Contact("assistant", "Google Assistente VM", "Estou pronta para responder perguntas.", androidx.compose.ui.graphics.Color(0xFFFBBC05))
    )

    var chatHistories by mutableStateOf(
        mapOf(
            "google_support" to mutableListOf(
                ChatMessage("Suporte Play Store", "Olá! Bem-vindo ao sistema de Máquina Virtual Original.", "21:07", false),
                ChatMessage("Suporte Play Store", "Você pode abrir a Play Store para baixar o Messenger, Terminal, Câmera e Galeria!", "21:08", false)
            ),
            "sys_admin" to mutableListOf(
                ChatMessage("Virtual SysAdmin", "Olá desenvolvedor! Segurança ativada no root de depuração.", "21:01", false),
                ChatMessage("Virtual SysAdmin", "Acesse as Opções do Desenvolvedor nas 'Configurações' do sistema para alternar privilégios de ROOT.", "21:02", false)
            ),
            "assistant" to mutableListOf(
                ChatMessage("Google Assistente VM", "Olá, eu sou correspondente da sua Máquina Virtual! Como posso te ajudar hoje?", "21:07", false)
            )
        )
    )

    // Current active chat thread contact ID
    var activeChatContactId by mutableStateOf<String?>("google_support")

    // Terminal log list
    var terminalHistory by mutableStateOf(
        listOf(
            "SysVM Virtual Kernel v6.1.0-sysvm x86_64",
            "Type 'help' to see list of available shell commands.",
            "sysvm@android:~$ "
        )
    )
    var currentTerminalInput by mutableStateOf("")

    // Secret Dialer Code triggers
    var dialerInput by mutableStateOf("")
    var isShowingImeiDialog by mutableStateOf(false)
    var isShowingHardwareDiagnosticTest by mutableStateOf(false)
    var currentDiagnosticTestState by mutableStateOf<String?>(null) // "RED", "GREEN", "BLUE", "TOUCH", "VERSION"

    fun resetDiagnostic() {
        currentDiagnosticTestState = null
        isShowingHardwareDiagnosticTest = false
    }

    // Toggle simulated system power
    fun pressPowerButton() {
        if (!isPowerOn) {
            isPowerOn = true
            isBooting = true
            bootTimeLeft = 4
            currentRoute = VMRoute.BootScreen
        } else {
            // Turning off
            isPowerOn = false
            isBooting = false
            currentRoute = VMRoute.BootScreen
        }
    }

    fun finishBooting() {
        isBooting = false
        isScreenLocked = true
        currentRoute = VMRoute.LockScreen
    }

    fun unlockScreen() {
        isScreenLocked = false
        currentRoute = VMRoute.HomeScreen
    }

    // OS navigation handlers
    fun navigateBack() {
        when (val route = currentRoute) {
            is VMRoute.AppOpen -> {
                currentRoute = VMRoute.HomeScreen
            }
            is VMRoute.HomeScreen -> {
                // Stay or go to Lock
                currentRoute = VMRoute.HomeScreen
            }
            is VMRoute.LockScreen -> {}
            is VMRoute.BootScreen -> {}
        }
    }

    fun navigateHome() {
        if (isPowerOn && !isBooting) {
            isScreenLocked = false
            currentRoute = VMRoute.HomeScreen
        }
    }

    fun openApp(appId: String) {
        if (isPowerOn && !isBooting && !isScreenLocked) {
            if (installedApps.contains(appId)) {
                currentRoute = VMRoute.AppOpen(appId)
            }
        }
    }

    fun installApp(appId: String) {
        installedApps = installedApps + appId
    }

    fun uninstallApp(appId: String) {
        if (appId != "play_store" && appId != "settings") {
            installedApps = installedApps - appId
            // If current open app was uninstalled, redirect to Home
            val route = currentRoute
            if (route is VMRoute.AppOpen && route.appId == appId) {
                currentRoute = VMRoute.HomeScreen
            }
        }
    }

    // Send mock messages
    fun sendChatMessage(contactId: String, text: String) {
        if (text.isBlank()) return
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeNow = formatter.format(Date())

        val currentList = chatHistories[contactId] ?: mutableListOf()
        val newList = currentList.toMutableList()
        newList.add(ChatMessage("Você", text, timeNow, true))

        val updatedHistories = chatHistories.toMutableMap()
        updatedHistories[contactId] = newList
        chatHistories = updatedHistories

        // Trigger bot smart response
        triggerBotResponse(contactId, text, timeNow)
    }

    private fun triggerBotResponse(contactId: String, messageText: String, timeStr: String) {
        val botTypingAnswer = when (contactId) {
            "google_support" -> {
                if (messageText.lowercase(Locale.ROOT).contains("original") || messageText.lowercase(Locale.ROOT).contains("play store")) {
                    "Sim! Os icones deste sistema foram projetados baseando-se na Play Store original com design limpo e adaptivo de alta qualidade!"
                } else if (messageText.lowercase(Locale.ROOT).contains("instalar") || messageText.lowercase(Locale.ROOT).contains("remover")) {
                    "Para instalar/remover aplicativos, clique para navegar de volta ao menu principal, abra a 'Play Store' e aperte o botao do app desejado."
                } else {
                    "Excelente! Nosso simulador virtual de aplicativos funciona perfeitamente offline para testar suas ideias mobile rapidamente."
                }
            }
            "sys_admin" -> {
                if (messageText.lowercase(Locale.ROOT).contains("root") || messageText.lowercase(Locale.ROOT).contains("permissao")) {
                    "Se o ROOT for ativado nos desenvolvedores (em Configurações), o terminal apresentara 'sudo' e '#' em vez de '$'."
                } else if (messageText.lowercase(Locale.ROOT).contains("terminal") || messageText.lowercase(Locale.ROOT).contains("comandos")) {
                    "No Terminal, voce pode criar e ler arquivos de verdade usand 'touch' e 'cat'. Teste 'mkdir docs' e 'ls'!"
                } else {
                    "Kernel VM em execucao: Thread ID-22. Seguranca intacta. Tudo rodando a toda velocidade!"
                }
            }
            "assistant" -> {
                val inputClean = messageText.lowercase(Locale.ROOT)
                if (inputClean.contains("quem é") || inputClean.contains("criador")) {
                    "Eu sou a Google Virtual Assistência integrada com esta incrível VM de Android com design Play Store!"
                } else if (inputClean.contains("horas") || inputClean.contains("tempo")) {
                    "A hora atual da maquina virtual esta sincronizada com " + SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                } else if (inputClean.contains("piada")) {
                    "Por que o computador foi ao medico? Porque ele estava com virus e com problemas de memoria virtual! Haha!"
                } else {
                    "Entendo perfeitamente! Como maquina virtual de IA, posso processar os recursos simulados. Quer tentar uma piada ou perguntar as horas?"
                }
            }
            else -> "Mensagem recebida com sucesso pela VM."
        }

        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.postDelayed({
            val currentList = chatHistories[contactId] ?: mutableListOf()
            val newList = currentList.toMutableList()
            newList.add(ChatMessage(contacts.firstOrNull { it.id == contactId }?.name ?: "Assistente", botTypingAnswer, timeStr, false))

            val updatedHistories = chatHistories.toMutableMap()
            updatedHistories[contactId] = newList
            chatHistories = updatedHistories
        }, 1200)
    }

    // Execute Terminal commands
    fun executeTerminalCommand(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return

        val userLine = "sysvm@android:~" + (if (rootAccess) "#" else "$") + " " + trimmed
        val newHistory = terminalHistory.toMutableList()
        // Remove current prompt line
        if (newHistory.isNotEmpty() && newHistory.last() == "sysvm@android:~$ " || newHistory.last() == "sysvm@android:~# ") {
            newHistory.removeAt(newHistory.size - 1)
        }
        newHistory.add(userLine)

        val parts = trimmed.split(" ")
        val command = parts[0].lowercase(Locale.ROOT)
        val args = parts.drop(1)

        when (command) {
            "help" -> {
                newHistory.add("Disponiveis comandos locais:")
                newHistory.add("  help      - Explica os comandos.")
                newHistory.add("  neofetch  - Mostra os detalhes de hardware da VM.")
                newHistory.add("  ls        - Lista os arquivos simulados.")
                newHistory.add("  mkdir     - Cria um diretorio (ex: 'mkdir doc')")
                newHistory.add("  touch     - Cria um arquivo (ex: 'touch nota.txt')")
                newHistory.add("  cat       - Le o conteudo do arquivo (ex: 'cat /system/build.prop')")
                newHistory.add("  rm        - Remove um arquivo (ex: 'rm /sdcard/photos/welcome.jpg')")
                newHistory.add("  sysinfo   - Imprime as configuracoes do Kernel.")
                newHistory.add("  ping      - Faz ping em URLs (ex: 'ping google.com')")
                newHistory.add("  su        - Ativa o modo super usuario se ROOT estiver ligado.")
                newHistory.add("  clear     - Limpa o terminal.")
            }
            "neofetch" -> {
                newHistory.add("      .---.         sysvm@android-virtual-machine")
                newHistory.add("     |o_o  |        -----------------------------")
                newHistory.add("     |:_/  |        OS: Android Virtual SysOS v16.0")
                newHistory.add("    //   \\ \\        Kernel: Linux 6.1.0-sysvm (x86_64)")
                newHistory.add("   (|     | )       Uptime: " + (System.currentTimeMillis() % 60) + " minutos")
                newHistory.add("  /'\\_   _/`\\       Resolution: Pixel Rendering Engine")
                newHistory.add("  \\___)=(___/_      Shell: Sh / Termux Virtual Console")
                newHistory.add("                    CPU: Tensor V3 Core Virtualized (8 vCPUs)")
                newHistory.add("                    RAM Allocated: ${String.format(Locale.US, "%.1f", virtualRamGb)} GB")
                newHistory.add("                    Storage: 256 GB virtual (ext4 file system)")
                newHistory.add("                    Root Options: " + (if (rootAccess) "ATIVADO (root)" else "DESATIVADO"))
            }
            "ls" -> {
                newHistory.add("Listagem de arquivos da Maquina Virtual:")
                val dirs = fileSystem.keys.map { it.substringBeforeLast("/") }.toSet()
                dirs.forEach { newHistory.add("d--r--r--   $it") }
                fileSystem.forEach { (path, raw) ->
                    val sizeString = "${raw.length * 2} Bytes"
                    newHistory.add("-rwxr-xr-x   $path  ($sizeString)")
                }
            }
            "mkdir" -> {
                if (args.isEmpty()) {
                    newHistory.add("Erro: mkdir precisa receber o nome da pasta.")
                } else {
                    val folderName = args[0]
                    fileSystem["/sdcard/$folderName/"] = "[EMPTY_FOLDER]"
                    newHistory.add("Pasta '/sdcard/$folderName/' criada com sucesso.")
                }
            }
            "touch" -> {
                if (args.isEmpty()) {
                    newHistory.add("Erro: touch precisa receber o nome do arquivo.")
                } else {
                    var fileName = args[0]
                    if (!fileName.startsWith("/")) {
                        fileName = "/sdcard/documents/$fileName"
                    }
                    fileSystem[fileName] = "Arquivo criado via console Termux em " + SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    newHistory.add("Arquivo '$fileName' gravado na memoria virtual.")
                }
            }
            "cat" -> {
                if (args.isEmpty()) {
                    newHistory.add("Erro: cat precisa de um arquivo. Ex: cat /system/build.prop")
                } else {
                    var searchFile = args[0]
                    if (!searchFile.startsWith("/")) {
                        searchFile = "/sdcard/documents/$searchFile"
                    }
                    val content = fileSystem[searchFile]
                    if (content != null) {
                        content.split('\n').forEach { newHistory.add(it) }
                    } else {
                        newHistory.add("cat: $searchFile: Arquivo ou diretorio nao encontrado.")
                    }
                }
            }
            "rm" -> {
                if (args.isEmpty()) {
                    newHistory.add("Erro: rm precisa de um arquivo.")
                } else {
                    var searchFile = args[0]
                    if (!searchFile.startsWith("/")) {
                        searchFile = "/sdcard/documents/$searchFile"
                    }
                    if (fileSystem.containsKey(searchFile)) {
                        fileSystem.remove(searchFile)
                        newHistory.add("Arquivo '$searchFile' removido.")
                    } else {
                        newHistory.add("rm: nao foi possivel remover '$searchFile': Arquivo nao encontrado.")
                    }
                }
            }
            "sysinfo" -> {
                newHistory.add("--- VM ARCHITECTURE PROPERTIES ---")
                newHistory.add("Virtualization Agent: Android Studio Native")
                newHistory.add("OS Branch: Google Play Authentic Experience")
                newHistory.add("Secure Boot: Enabled")
                newHistory.add("Dynamic Memory allocation: Sized is ${virtualRamGb}GB")
                newHistory.add("Clock speed: 2.80 GHz virtualized")
            }
            "ping" -> {
                if (args.isEmpty()) {
                    newHistory.add("Erro: utilize 'ping <host/site>'")
                } else {
                    val host = args[0]
                    newHistory.add("PING $host (10.0.2.2) 56(84) bytes of data.")
                    newHistory.add("64 bytes from 10.0.2.2: icmp_seq=1 ttl=64 time=10.2 ms")
                    newHistory.add("64 bytes from 10.0.2.2: icmp_seq=2 ttl=64 time=8.45 ms")
                    newHistory.add("--- $host ping statistics ---")
                    newHistory.add("2 packets transmitted, 2 received, 0% packet loss, time 1002ms")
                    newHistory.add("rtt min/avg/max/mdev = 8.45/9.32/10.21/0.88 ms")
                }
            }
            "su" -> {
                if (rootAccess) {
                    newHistory.add("Modo Super Usuario (root) ativado no terminal.")
                } else {
                    newHistory.add("su: negado. Ative as permissões de 'ROOT' nas Configurações da VM.")
                }
            }
            "clear" -> {
                newHistory.clear()
            }
            else -> {
                newHistory.add("sysvm: comando nao reconhecido: '$command'. Digite 'help' para comandos.")
            }
        }

        // Add prompt line again
        val promptMarker = if (rootAccess) "sysvm@android:~# " else "sysvm@android:~$ "
        newHistory.add(promptMarker)

        terminalHistory = newHistory
        currentTerminalInput = ""
    }

    // Capture simulated photos
    fun requestCameraShot(filterApplied: String): String {
        val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val name = "IMG_" + formatter.format(Date()) + ".jpg"
        val photoKey = "/sdcard/photos/$name"
        val desc = "[SIMULATED_CAMERA_SHOT: $filterApplied Filter applied on ${Date().toString()}]"
        
        fileSystem[photoKey] = desc
        return photoKey
    }
}
