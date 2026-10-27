package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Cosigner
import com.example.model.MultiSigProposal
import com.example.model.ProposalStatus
import com.example.model.ProposalType
import com.example.ui.components.BinaCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import com.example.viewmodel.BinaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSigVaultScreen(viewModel: BinaViewModel) {
    val proposals by viewModel.proposals.collectAsState()
    val vault = viewModel.vault
    var showCreateProposalDialog by remember { mutableStateOf(false) }

    val pendingProposals = proposals.filter { it.status == ProposalStatus.PENDING_APPROVAL }
    val executedProposals = proposals.filter { it.status != ProposalStatus.PENDING_APPROVAL }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BinaBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SECURE MULTI-SIGNATURE VAULT",
                        color = BinaTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Threshold Security",
                        color = BinaTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Button(
                    onClick = { showCreateProposalDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BinaPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_create_proposal")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Proposal", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Vault Quorum & Velocity Limits Card
        item {
            BinaCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = vault.name,
                            color = BinaTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Vault Contract: ${vault.vaultAddress.take(12)}...${vault.vaultAddress.takeLast(6)}",
                            color = BinaTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    StatusPill(text = "${vault.requiredSignatures} of ${vault.totalSigners} Quorum", color = BinaPrimary)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = BinaBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Daily spending velocity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Had Perbelanjaan Harian", color = BinaTextSecondary, fontSize = 11.sp)
                    Text("RM %,.2f / RM %,.2f".format(vault.spentTodayUsd, vault.dailyLimitUsd), color = BinaTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (vault.spentTodayUsd / vault.dailyLimitUsd).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BinaPrimary,
                    trackColor = BinaSurfaceHighlight
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Had Pemicu Multi-Sig", color = BinaTextSecondary, fontSize = 11.sp)
                    Text("Transaksi > RM %,.2f".format(vault.multiSigThresholdUsd), color = BinaTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Cosigners List
        item {
            Text(
                text = "AUTHORIZED COSIGNERS (${vault.cosigners.size})",
                color = BinaTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        items(vault.cosigners, key = { it.id }) { cosigner ->
            CosignerRowItem(cosigner = cosigner)
        }

        // Pending Proposals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PENDING SIGNATURE PROPOSALS (${pendingProposals.size})",
                    color = BinaTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                if (pendingProposals.isNotEmpty()) {
                    StatusPill(text = "Action Required", color = BinaTertiary)
                }
            }
        }

        if (pendingProposals.isEmpty()) {
            item {
                Surface(
                    color = BinaSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No pending multi-sig proposals", color = BinaTextMuted, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(pendingProposals, key = { it.id }) { proposal ->
                ProposalCardItem(
                    proposal = proposal,
                    onSign = { cosignerId, name ->
                        viewModel.signProposal(proposal.id, cosignerId, name)
                    },
                    onReject = { viewModel.rejectProposal(proposal.id) }
                )
            }
        }

        // Executed History
        if (executedProposals.isNotEmpty()) {
            item {
                Text(
                    text = "PROPOSAL HISTORY",
                    color = BinaTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(executedProposals, key = { it.id }) { proposal ->
                ExecutedProposalCard(proposal = proposal)
            }
        }
    }

    // Create Proposal Modal
    if (showCreateProposalDialog) {
        CreateProposalModal(
            onDismiss = { showCreateProposalDialog = false },
            onSubmit = { title, asset, amount, destination, reason ->
                // create proposal
                viewModel.sendCrypto(asset, amount, destination, reason)
                showCreateProposalDialog = false
            }
        )
    }
}

@Composable
fun CosignerRowItem(cosigner: Cosigner) {
    BinaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (cosigner.isLocalDevice) BinaPrimary.copy(alpha = 0.2f) else BinaSecondary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (cosigner.role) {
                        com.example.model.CosignerRole.PRIMARY_DEVICE -> Icons.Default.PhoneAndroid
                        com.example.model.CosignerRole.HARDWARE_KEY -> Icons.Default.Usb
                        com.example.model.CosignerRole.RECOVERY_GUARDIAN -> Icons.Default.Cloud
                        com.example.model.CosignerRole.INSTITUTIONAL_ORACLE -> Icons.Default.Business
                    },
                    contentDescription = null,
                    tint = if (cosigner.isLocalDevice) BinaPrimary else BinaSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cosigner.name,
                        color = BinaTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (cosigner.isLocalDevice) {
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusPill(text = "Local Key", color = BinaPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Key: ${cosigner.publicKey}",
                    color = BinaTextMuted,
                    fontSize = 11.sp
                )
            }

            StatusPill(text = "Active", color = BinaProfitGreen)
        }
    }
}

@Composable
fun ProposalCardItem(
    proposal: MultiSigProposal,
    onSign: (cosignerId: String, cosignerName: String) -> Unit,
    onReject: () -> Unit
) {
    BinaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = proposal.title,
                color = BinaTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            StatusPill(text = "Pending Signatures", color = BinaTertiary)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Destination: ${proposal.destinationAddress}",
            color = BinaTextSecondary,
            fontSize = 11.sp
        )
        Text(
            text = "Trigger: ${proposal.reason}",
            color = BinaTextMuted,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Signatures Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Quorum Progress",
                color = BinaTextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = "${proposal.currentSignatures} of ${proposal.requiredSignatures} Signatures Collected",
                color = if (proposal.isQuorumMet) BinaProfitGreen else BinaTertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (proposal.currentSignatures.toFloat() / proposal.requiredSignatures).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (proposal.isQuorumMet) BinaProfitGreen else BinaTertiary,
            trackColor = BinaSurfaceHighlight
        )

        // List of cosigner signatures collected
        Spacer(modifier = Modifier.height(10.dp))
        proposal.signatures.forEach { sig ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = BinaProfitGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${sig.cosignerName} • Hash: ${sig.signatureHash.take(16)}...",
                    color = BinaTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons: Cosign with Ledger or Cloud Key
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onSign("cosigner_2", "Ledger Nano X (Hardware Vault)")
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("btn_cosign_hardware"),
                colors = ButtonDefaults.buttonColors(containerColor = BinaPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Usb, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sign with Ledger", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onReject,
                modifier = Modifier
                    .height(44.dp)
                    .testTag("btn_reject_proposal"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BinaLossRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExecutedProposalCard(proposal: MultiSigProposal) {
    BinaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = proposal.title,
                    color = BinaTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Jumlah: ${proposal.amount} ${proposal.assetSymbol} (RM %,.2f)".format(proposal.amountUsd),
                    color = BinaTextSecondary,
                    fontSize = 12.sp
                )
            }
            StatusPill(
                text = proposal.status.name,
                color = if (proposal.status == ProposalStatus.EXECUTED) BinaProfitGreen else BinaLossRed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProposalModal(
    onDismiss: () -> Unit,
    onSubmit: (title: String, asset: String, amount: Double, destination: String, reason: String) -> Unit
) {
    var title by remember { mutableStateOf("Cold Storage Vault Transfer") }
    var asset by remember { mutableStateOf("ETH") }
    var amountText by remember { mutableStateOf("1.5") }
    var destination by remember { mutableStateOf("0x39a4...99B2") }
    var reason by remember { mutableStateOf("Strategic treasury staking reallocation") }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = BinaSurface) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Create Multi-Sig Proposal", color = BinaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Proposal Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = asset, onValueChange = { asset = it }, label = { Text("Asset Symbol (e.g. BTC, ETH, SOL)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = destination, onValueChange = { destination = it }, label = { Text("Destination Address") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Governance Justification") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 1.0
                    onSubmit(title, asset, amt, destination, reason)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BinaPrimary)
            ) {
                Text("Submit Multi-Sig Proposal", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
