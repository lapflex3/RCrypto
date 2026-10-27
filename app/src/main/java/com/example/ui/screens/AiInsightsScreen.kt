package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.model.AiMarketInsight
import com.example.model.PredictivePriceModel
import com.example.model.SocialSentimentItem
import com.example.ui.components.BinaCard
import com.example.ui.components.CryptoIconBadge
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import com.example.viewmodel.BinaViewModel

@Composable
fun AiInsightsScreen(viewModel: BinaViewModel) {
    val riskAudit by viewModel.riskAudit.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val assets by viewModel.assets.collectAsState()

    val insights = remember { viewModel.getMarketInsights() }
    val predictiveModels = remember(assets) { viewModel.getPredictiveModels() }
    val socialSentimentItems = remember { viewModel.getSocialSentimentFeed() }

    var selectedTab by remember { mutableStateOf(0) } // 0: Predictive Modeling, 1: Social & News Sentiment, 2: Risk Assessment
    var userQueryText by remember { mutableStateOf("") }

    val quickQuestions = listOf(
        "Predict BTC & SOL 72h targets",
        "Summarize social media sentiment for Bitcoin",
        "Audit my portfolio risk score",
        "What is the outlook for Monero and LTC?"
    )

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
                        text = "QUANTITATIVE AI INTELLIGENCE",
                        color = BinaTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Market Insights & Predictive AI",
                        color = BinaTextPrimary,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                StatusPill(text = "Real-Time AI", color = BinaPrimary)
            }
        }

        // Sub-Tab Switcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BinaSurface)
                    .padding(4.dp)
            ) {
                TabButton(
                    title = "Predictive AI",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "Social Sentiment",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "Risk Audit",
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // TAB 0: PREDICTIVE MODELING FOR MAJOR COINS (BTC, BCH, SOL, XMR, LTC, ETH)
        if (selectedTab == 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PREDICTIVE PRICE MODELS (MAJOR COINS)",
                        color = BinaTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Machine Learning Forecast",
                        color = BinaSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            items(predictiveModels, key = { it.coinSymbol }) { model ->
                PredictiveModelCard(model = model)
            }
        }

        // TAB 1: SOCIAL & NEWS SENTIMENT ANALYSIS
        else if (selectedTab == 1) {
            item {
                BinaCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "AGGREGATED CRYPTO SENTIMENT SCORE",
                        color = BinaTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "78 / 100",
                                    color = BinaProfitGreen,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusPill(text = "Bullish Sentiment", color = BinaProfitGreen)
                            }
                            Text(
                                text = "Based on 145,000+ social mentions & news articles",
                                color = BinaTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Social Volume", color = BinaTextMuted, fontSize = 11.sp)
                            Text("+38.4% 24h", color = BinaSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sentiment ratio bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        Box(modifier = Modifier.weight(0.78f).fillMaxHeight().background(BinaProfitGreen))
                        Box(modifier = Modifier.weight(0.14f).fillMaxHeight().background(BinaSecondary))
                        Box(modifier = Modifier.weight(0.08f).fillMaxHeight().background(BinaLossRed))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bullish 78%", color = BinaProfitGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Neutral 14%", color = BinaSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Bearish 8%", color = BinaLossRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Text(
                    text = "NEWS & SOCIAL MEDIA INTELLIGENCE FEED",
                    color = BinaTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(socialSentimentItems, key = { it.id }) { item ->
                SocialSentimentRowItem(item = item)
            }
        }

        // TAB 2: AUTOMATED PORTFOLIO RISK AUDIT
        else {
            item {
                BinaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(BinaPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = BinaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AUTOMATED RISK AUDIT",
                                color = BinaTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        IconButton(onClick = { viewModel.runRiskAudit() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Audit",
                                tint = BinaSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (riskAudit != null) {
                        val audit = riskAudit!!
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(BinaSurfaceVariant)
                                    .border(
                                        3.dp,
                                        when {
                                            audit.overallRiskScore < 35 -> BinaProfitGreen
                                            audit.overallRiskScore < 65 -> BinaTertiary
                                            else -> BinaLossRed
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${audit.overallRiskScore}",
                                        color = BinaTextPrimary,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(text = "/ 100", color = BinaTextMuted, fontSize = 9.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                StatusPill(
                                    text = audit.riskRatingLabel,
                                    color = when {
                                        audit.overallRiskScore < 35 -> BinaProfitGreen
                                        audit.overallRiskScore < 65 -> BinaTertiary
                                        else -> BinaLossRed
                                    }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = audit.auditSummary,
                                    color = BinaTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        RiskFactorMeter(label = "Asset Concentration Risk", score = audit.concentrationRisk)
                        Spacer(modifier = Modifier.height(6.dp))
                        RiskFactorMeter(label = "Volatility Beta Exposure", score = audit.volatilityExposure)
                        Spacer(modifier = Modifier.height(6.dp))
                        RiskFactorMeter(label = "Smart Contract Risk", score = audit.smartContractRisk)
                        Spacer(modifier = Modifier.height(6.dp))
                        RiskFactorMeter(label = "Multi-Sig Security Coverage", score = audit.multiSigSecurityCoverage, invertColor = true)

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "AI Mitigations & Recommendations:",
                            color = BinaTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        audit.recommendations.forEach { rec ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = BinaPrimary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp)
                                    )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = rec, color = BinaTextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Interactive AI Market Intelligence Assistant (Sticky Bottom Section)
        item {
            BinaCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BinaSecondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = BinaSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI MARKET ASSISTANT",
                        color = BinaTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickQuestions) { q ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    userQueryText = q
                                    viewModel.askAiAssistant(q)
                                },
                            color = BinaSurfaceHighlight,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = q,
                                color = BinaTextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userQueryText,
                        onValueChange = { userQueryText = it },
                        placeholder = { Text("Ask for predictions, sentiment, trends...", color = BinaTextMuted) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_ai_query"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BinaSecondary,
                            unfocusedBorderColor = BinaBorder
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (userQueryText.isNotBlank()) {
                                viewModel.askAiAssistant(userQueryText)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(BinaSecondary)
                            .testTag("btn_send_ai_query")
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                if (aiResponse != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = BinaSurfaceHighlight,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("BINA AI ANALYSIS", color = BinaSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Real-Time Engine", color = BinaProfitGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = aiResponse!!,
                                color = BinaTextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PredictiveModelCard(model: PredictivePriceModel) {
    BinaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CryptoIconBadge(
                    symbol = model.coinSymbol,
                    color = when (model.coinSymbol) {
                        "BTC" -> Color(0xFFF7931A)
                        "BCH" -> Color(0xFF0AC18E)
                        "SOL" -> Color(0xFF14F195)
                        "XMR" -> Color(0xFFFF6600)
                        "LTC" -> Color(0xFF345D9D)
                        "ETH" -> Color(0xFF627EEA)
                        else -> BinaPrimary
                    },
                    sizeDp = 36
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${model.coinName} (${model.coinSymbol})",
                        color = BinaTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Spot: RM %,.2f".format(model.currentPrice),
                        color = BinaTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            StatusPill(
                text = "${model.bullProbability}% Bullish (${model.timeHorizon})",
                color = BinaProfitGreen
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Target Targets Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(BinaSurfaceVariant)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Sasaran Kenaikan (Bull Target)", color = BinaProfitGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("RM %,.2f".format(model.targetBullPrice), color = BinaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Lantai Sokongan (Bear Floor)", color = BinaLossRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("RM %,.2f".format(model.targetBearPrice), color = BinaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ML Key Drivers
        Text("Key Predictive ML Drivers:", color = BinaTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        model.keyDrivers.forEach { driver ->
            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(BinaSecondary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = driver, color = BinaTextMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun SocialSentimentRowItem(item: SocialSentimentItem) {
    BinaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.source,
                color = BinaSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.timeAgo,
                color = BinaTextMuted,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.headline,
            color = BinaTextPrimary,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.engagement,
                color = BinaTextSecondary,
                fontSize = 11.sp
            )
            StatusPill(
                text = item.sentiment,
                color = if (item.sentiment == "BULLISH") BinaProfitGreen else if (item.sentiment == "BEARISH") BinaLossRed else BinaSecondary
            )
        }
    }
}

@Composable
fun RiskFactorMeter(label: String, score: Int, invertColor: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = BinaTextSecondary, fontSize = 11.sp)
            Text(text = "$score%", color = BinaTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        val barColor = if (invertColor) {
            if (score > 70) BinaProfitGreen else BinaTertiary
        } else {
            if (score < 40) BinaProfitGreen else if (score < 70) BinaTertiary else BinaLossRed
        }
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = BinaSurfaceHighlight
        )
    }
}

