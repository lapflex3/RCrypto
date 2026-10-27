package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TransparentWatermark
import com.example.ui.dialogs.BankWithdrawalDialog
import com.example.ui.dialogs.ReceiveCryptoDialog
import com.example.ui.dialogs.SendCryptoDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.BinaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinaMainScreen(viewModel: BinaViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()
    val assets by viewModel.assets.collectAsState()
    val proposals by viewModel.proposals.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val isGoogleAuth by viewModel.isGoogleAuth.collectAsState()

    var showSendDialog by remember { mutableStateOf(false) }
    var showReceiveDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val pendingProposalsCount = proposals.count { it.status == com.example.model.ProposalStatus.PENDING_APPROVAL }

    // Handle back button: if not on WALLET screen, return to WALLET screen
    BackHandler(enabled = currentScreen != AppScreen.WALLET) {
        viewModel.selectScreen(AppScreen.WALLET)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            snackbarHostState.showSnackbar(feedbackMessage!!)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showProfileDialog = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BinaPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "R",
                                color = Color.Black,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "RCrypto",
                                    color = BinaTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = BinaPrimary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "MYR",
                                        color = BinaPrimary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(BinaProfitGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${userName ?: "Nasadef"} • Mainnet Live",
                                    color = BinaTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Multi-Sig Shield Indicator
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectScreen(AppScreen.VAULT) },
                        color = if (pendingProposalsCount > 0) BinaTertiary.copy(alpha = 0.2f) else BinaSurfaceVariant,
                        border = if (pendingProposalsCount > 0) androidx.compose.foundation.BorderStroke(1.dp, BinaTertiary) else null,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Multi-Sig",
                                tint = if (pendingProposalsCount > 0) BinaTertiary else BinaPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (pendingProposalsCount > 0) "2/3 ($pendingProposalsCount)" else "2/3 Multi-Sig",
                                color = if (pendingProposalsCount > 0) BinaTertiary else BinaTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // User Account Icon
                    IconButton(
                        onClick = { showProfileDialog = true },
                        modifier = Modifier.testTag("user_profile_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BinaSurfaceHighlight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Akaun Pengguna",
                                tint = BinaTextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BinaBackground
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BinaSurface),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Transparent Watermark as requested
                TransparentWatermark(modifier = Modifier.padding(top = 4.dp))

                NavigationBar(
                    containerColor = BinaSurface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    val screens = listOf(
                        Triple(AppScreen.WALLET, Icons.Default.AccountBalanceWallet, "Dompet"),
                        Triple(AppScreen.TRADE, Icons.Default.ShowChart, "Dagangan"),
                        Triple(AppScreen.SWAP, Icons.Default.SwapHoriz, "Tukar"),
                        Triple(AppScreen.BOTS, Icons.Default.SmartToy, "Bot AI"),
                        Triple(AppScreen.AI_INSIGHTS, Icons.Default.AutoAwesome, "Analisis"),
                        Triple(AppScreen.VAULT, Icons.Default.Security, "Vault")
                    )

                    screens.forEach { (screen, icon, label) ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectScreen(screen) },
                            icon = {
                                if (screen == AppScreen.VAULT && pendingProposalsCount > 0) {
                                    BadgedBox(badge = {
                                        Badge(containerColor = BinaTertiary) {
                                            Text("$pendingProposalsCount", color = Color.Black)
                                        }
                                    }) {
                                        Icon(imageVector = icon, contentDescription = label)
                                    }
                                } else {
                                    Icon(imageVector = icon, contentDescription = label)
                                }
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BinaPrimary,
                                indicatorColor = BinaPrimary,
                                unselectedIconColor = BinaTextSecondary,
                                unselectedTextColor = BinaTextSecondary
                            ),
                            modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = BinaSurfaceHighlight,
                    contentColor = BinaTextPrimary,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BinaBackground)
        ) {
            when (currentScreen) {
                AppScreen.WALLET -> WalletScreen(
                    viewModel = viewModel,
                    onOpenSend = { showSendDialog = true },
                    onOpenReceive = { showReceiveDialog = true },
                    onOpenWithdraw = { showWithdrawDialog = true }
                )
                AppScreen.TRADE -> TradeScreen(viewModel = viewModel)
                AppScreen.SWAP -> SwapScreen(viewModel = viewModel)
                AppScreen.BOTS -> TradingBotsScreen(viewModel = viewModel)
                AppScreen.AI_INSIGHTS -> AiInsightsScreen(viewModel = viewModel)
                AppScreen.VAULT -> MultiSigVaultScreen(viewModel = viewModel)
            }
        }
    }

    // Modal dialogs
    if (showSendDialog) {
        SendCryptoDialog(
            assets = assets,
            onDismiss = { showSendDialog = false },
            onSend = { symbol, amount, toAddress, note ->
                viewModel.sendCrypto(symbol, amount, toAddress, note)
                showSendDialog = false
            }
        )
    }

    if (showReceiveDialog) {
        ReceiveCryptoDialog(
            assets = assets,
            onDismiss = { showReceiveDialog = false }
        )
    }

    if (showWithdrawDialog) {
        BankWithdrawalDialog(
            assets = assets,
            onDismiss = { showWithdrawDialog = false },
            onWithdraw = { bankName, accountNumber, accountHolder, amount, method ->
                viewModel.withdrawToBank(bankName, accountNumber, accountHolder, amount, method)
                showWithdrawDialog = false
            }
        )
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            containerColor = BinaSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = BinaPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Profil Akaun RCrypto",
                        color = BinaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        color = BinaSurfaceHighlight,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Nama Pengguna", color = BinaTextMuted, fontSize = 11.sp)
                            Text(userName ?: "Nasadef Trader", color = BinaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Emel Berdaftar", color = BinaTextMuted, fontSize = 11.sp)
                            Text(userEmail ?: "lapflex100@gmail.com", color = BinaTextSecondary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (isGoogleAuth) Color(0xFF4285F4).copy(alpha = 0.2f) else BinaPrimary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isGoogleAuth) "Google Account Connected" else "Standard Email/Password",
                                        color = if (isGoogleAuth) Color(0xFF669DF6) else BinaPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        color = BinaSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = BinaProfitGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Pengesahan 2-of-3 Multi-Sig Aktif", color = BinaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("Pengeluaran ke bank dilindungi kuorum selamat", color = BinaTextMuted, fontSize = 10.sp)
                            }
                        }
                    }

                    TransparentWatermark(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProfileDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinaLossRed),
                    modifier = Modifier.testTag("logout_button")
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Keluar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Tutup", color = BinaTextSecondary)
                }
            }
        )
    }
}
