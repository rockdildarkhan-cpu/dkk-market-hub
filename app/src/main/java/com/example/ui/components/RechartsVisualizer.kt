package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DkkEmerald
import com.example.ui.theme.DkkGold
import com.example.ui.theme.DkkNavy
import com.example.ui.theme.DkkSlate
import kotlin.math.cos
import kotlin.math.sin

/**
 * Data item model for Recharts multi-seller sales comparison
 */
data class SellerSalesChartItem(
    val sellerId: String,
    val sellerName: String,
    val storeName: String,
    val grossSales: Double,
    val platformCommission: Double,
    val netPayout: Double,
    val ordersCount: Int,
    val color: Color = DkkEmerald
)

/**
 * Data point for Recharts time-series Area/Line chart
 */
data class TimeSeriesSalesPoint(
    val periodLabel: String,
    val grossSales: Double,
    val platformCommission: Double
)

/**
 * Data slice for Recharts Donut / Pie chart
 */
data class RechartsDonutSlice(
    val label: String,
    val value: Double,
    val color: Color,
    val subLabel: String = ""
)

/**
 * Recharts Bar Chart: Multi-seller Gross Sales & Platform Commission Comparative Chart
 * Features Cartesian grid, grouped dual bars, interactive tap tooltip popover, and Recharts Legend.
 */
@Composable
fun RechartsBarChart(
    items: List<SellerSalesChartItem>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(DkkSlate.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No sales data available to chart", color = Color(0xFF94A3B8), fontSize = 12.sp)
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxSales = remember(items) {
        items.maxOfOrNull { it.grossSales }?.coerceAtLeast(1000.0) ?: 1000.0
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DkkSlate),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header & Recharts Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "<BarChart />",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = DkkGold,
                            fontSize = 13.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF22C55E).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF22C55E))
                        ) {
                            Text("Recharts", color = Color(0xFF22C55E), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                    Text(
                        "Multi-Seller Comparative (Gross Sales vs Platform Commission)",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = DkkGold, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recharts Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(10.dp, 10.dp).background(Color(0xFF10B981), RoundedCornerShape(2.dp)))
                    Text("Gross Sales (PKR)", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(10.dp, 10.dp).background(DkkGold, RoundedCornerShape(2.dp)))
                    Text("Commission (PKR)", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Canvas & Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(items) {
                            detectTapGestures { offset ->
                                val availableWidth = size.width - 40f
                                val count = items.size
                                val slotWidth = availableWidth / count
                                val tappedIndex = ((offset.x - 40f) / slotWidth).toInt()
                                selectedIndex = if (tappedIndex in items.indices) {
                                    if (selectedIndex == tappedIndex) null else tappedIndex
                                } else null
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val paddingLeft = 45f
                    val paddingBottom = 30f
                    val chartWidth = w - paddingLeft
                    val chartHeight = h - paddingBottom

                    // Draw Cartesian Grid Lines
                    val gridLines = 4
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    for (i in 0..gridLines) {
                        val y = chartHeight * (i.toFloat() / gridLines)
                        drawLine(
                            color = Color(0xFF334155).copy(alpha = 0.5f),
                            start = Offset(paddingLeft, y),
                            end = Offset(w, y),
                            strokeWidth = 1f,
                            pathEffect = pathEffect
                        )
                    }

                    // Draw Bars
                    val count = items.size
                    val slotWidth = chartWidth / count
                    val barWidth = (slotWidth * 0.32f).coerceIn(10f, 28f)
                    val barGap = 4f

                    items.forEachIndexed { i, item ->
                        val slotCenter = paddingLeft + (i * slotWidth) + (slotWidth / 2f)
                        val salesHeight = ((item.grossSales / maxSales) * chartHeight).toFloat().coerceIn(4f, chartHeight)
                        val commHeight = ((item.platformCommission / maxSales) * chartHeight).toFloat().coerceIn(2f, chartHeight)

                        val isSelected = selectedIndex == i

                        // Column Highlight Scrim if Selected
                        if (isSelected) {
                            drawRoundRect(
                                color = Color(0xFF38BDF8).copy(alpha = 0.12f),
                                topLeft = Offset(paddingLeft + (i * slotWidth) + 2f, 0f),
                                size = Size(slotWidth - 4f, chartHeight),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                        }

                        // Bar 1: Gross Sales (Emerald)
                        val salesX = slotCenter - barWidth - (barGap / 2f)
                        val salesY = chartHeight - salesHeight
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(
                                    if (isSelected) Color(0xFF34D399) else Color(0xFF10B981),
                                    Color(0xFF065F46)
                                )
                            ),
                            topLeft = Offset(salesX, salesY),
                            size = Size(barWidth, salesHeight),
                            cornerRadius = CornerRadius(4f, 4f)
                        )

                        // Bar 2: Commission (Gold)
                        val commX = slotCenter + (barGap / 2f)
                        val commY = chartHeight - commHeight
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(
                                    if (isSelected) Color(0xFFFDE047) else DkkGold,
                                    Color(0xFF854D0E)
                                )
                            ),
                            topLeft = Offset(commX, commY),
                            size = Size(barWidth, commHeight),
                            cornerRadius = CornerRadius(4f, 4f)
                        )

                        // Base axis baseline
                        drawLine(
                            color = Color(0xFF64748B),
                            start = Offset(paddingLeft, chartHeight),
                            end = Offset(w, chartHeight),
                            strokeWidth = 1.5f
                        )
                    }
                }

                // Y-Axis labels (Static overlay)
                Column(
                    modifier = Modifier
                        .height(170.dp)
                        .padding(end = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val kMax = (maxSales / 1000.0).toInt()
                    Text("${kMax}k", color = Color(0xFF64748B), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("${(kMax * 0.66).toInt()}k", color = Color(0xFF64748B), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("${(kMax * 0.33).toInt()}k", color = Color(0xFF64748B), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("0", color = Color(0xFF64748B), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }

                // X-Axis Seller Names
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 45.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    items.forEachIndexed { idx, item ->
                        val isSelected = selectedIndex == idx
                        Text(
                            text = item.storeName.ifBlank { item.sellerName }.take(8),
                            color = if (isSelected) DkkGold else Color(0xFF94A3B8),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable {
                                selectedIndex = if (selectedIndex == idx) null else idx
                            }
                        )
                    }
                }
            }

            // Interactive Recharts Tooltip Popover (Appears on bar tap)
            AnimatedVisibility(
                visible = selectedIndex != null && selectedIndex in items.indices,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedIndex?.let { idx ->
                    if (idx in items.indices) {
                        val sel = items[idx]
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DkkGold.copy(alpha = 0.8f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(modifier = Modifier.size(8.dp).background(DkkGold, CircleShape))
                                        Text(
                                            text = "${sel.storeName} (${sel.sellerName})",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Text(
                                        text = "${sel.ordersCount} Orders",
                                        color = DkkGold,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Gross Sales:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text("Rs %,.0f".format(sel.grossSales), color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Column {
                                        Text("Platform Commission:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text("Rs %,.0f".format(sel.platformCommission), color = DkkGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Column {
                                        Text("Net Seller Payout:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text("Rs %,.0f".format(sel.netPayout), color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Recharts Area Chart: Revenue & Commission Trend Timeline
 * Features smooth bezier curves, gradient fills, data point nodes, and interactive timeline scrubber.
 */
@Composable
fun RechartsAreaChart(
    dataPoints: List<TimeSeriesSalesPoint>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    var activeScrubIndex by remember { mutableStateOf<Int?>(null) }
    val maxVal = remember(dataPoints) {
        dataPoints.maxOfOrNull { it.grossSales }?.coerceAtLeast(100.0) ?: 100.0
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DkkSlate),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "<AreaChart />",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            fontSize = 13.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF38BDF8))
                        ) {
                            Text("Recharts", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                    Text("Sales & Platform Revenue Curve Over Time", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                    Text("Sales", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                    Box(modifier = Modifier.size(8.dp).background(DkkGold, CircleShape))
                    Text("Comm", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dataPoints) {
                            detectTapGestures { offset ->
                                val step = size.width / (dataPoints.size - 1).coerceAtLeast(1)
                                val idx = (offset.x / step).toInt().coerceIn(0, dataPoints.lastIndex)
                                activeScrubIndex = if (activeScrubIndex == idx) null else idx
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val chartBottom = h - 24f
                    val step = w / (dataPoints.size - 1).coerceAtLeast(1)

                    // Cartesian horizontal dashed grid lines
                    val gridPathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    for (g in 0..3) {
                        val gy = chartBottom * (g / 3f)
                        drawLine(
                            color = Color(0xFF334155).copy(alpha = 0.5f),
                            start = Offset(0f, gy),
                            end = Offset(w, gy),
                            strokeWidth = 1f,
                            pathEffect = gridPathEffect
                        )
                    }

                    // Compute points for Gross Sales
                    val salesCoords = dataPoints.mapIndexed { idx, pt ->
                        val x = idx * step
                        val y = chartBottom - ((pt.grossSales / maxVal) * (chartBottom - 10f)).toFloat()
                        Offset(x, y)
                    }

                    // Build smooth bezier path
                    val salesPath = Path().apply {
                        if (salesCoords.isNotEmpty()) {
                            moveTo(salesCoords[0].x, salesCoords[0].y)
                            for (i in 0 until salesCoords.size - 1) {
                                val p0 = salesCoords[i]
                                val p1 = salesCoords[i + 1]
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                        }
                    }

                    // Filled Area under Gross Sales curve
                    val filledPath = Path().apply {
                        addPath(salesPath)
                        lineTo(salesCoords.last().x, chartBottom)
                        lineTo(salesCoords.first().x, chartBottom)
                        close()
                    }

                    drawPath(
                        path = filledPath,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF10B981).copy(alpha = 0.35f),
                                Color(0xFF10B981).copy(alpha = 0.02f)
                            )
                        )
                    )

                    // Draw Stroke on Gross Sales
                    drawPath(
                        path = salesPath,
                        color = Color(0xFF10B981),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )

                    // Draw Commission curve (Gold dashed)
                    val commCoords = dataPoints.mapIndexed { idx, pt ->
                        val x = idx * step
                        val y = chartBottom - ((pt.platformCommission / maxVal) * (chartBottom - 10f)).toFloat()
                        Offset(x, y)
                    }
                    val commPath = Path().apply {
                        if (commCoords.isNotEmpty()) {
                            moveTo(commCoords[0].x, commCoords[0].y)
                            for (i in 0 until commCoords.size - 1) {
                                val p0 = commCoords[i]
                                val p1 = commCoords[i + 1]
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                        }
                    }
                    drawPath(
                        path = commPath,
                        color = DkkGold,
                        style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                    )

                    // Draw Data Point Nodes
                    salesCoords.forEachIndexed { i, pt ->
                        val isScrubbed = activeScrubIndex == i
                        drawCircle(
                            color = if (isScrubbed) Color.White else Color(0xFF10B981),
                            radius = if (isScrubbed) 6f else 3.5f,
                            center = pt
                        )
                        if (isScrubbed) {
                            // Vertical scrubber line
                            drawLine(
                                color = Color.White.copy(alpha = 0.8f),
                                start = Offset(pt.x, 0f),
                                end = Offset(pt.x, chartBottom),
                                strokeWidth = 1.5f,
                                pathEffect = gridPathEffect
                            )
                        }
                    }

                    // Axis Line
                    drawLine(
                        color = Color(0xFF64748B),
                        start = Offset(0f, chartBottom),
                        end = Offset(w, chartBottom),
                        strokeWidth = 1f
                    )
                }

                // X-Axis Labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dataPoints.forEachIndexed { i, pt ->
                        val isSelected = activeScrubIndex == i
                        Text(
                            text = pt.periodLabel,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 9.sp,
                            modifier = Modifier.clickable {
                                activeScrubIndex = if (activeScrubIndex == i) null else i
                            }
                        )
                    }
                }
            }

            // Interactive Tooltip on Area Node Scrub
            AnimatedVisibility(
                visible = activeScrubIndex != null && activeScrubIndex in dataPoints.indices,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                activeScrubIndex?.let { idx ->
                    if (idx in dataPoints.indices) {
                        val pt = dataPoints[idx]
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(pt.periodLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("Sales: Rs %,.0f".format(pt.grossSales), color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("Cut: Rs %,.0f".format(pt.platformCommission), color = DkkGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Recharts Donut / Pie Chart: Seller / Category Market Share Breakdown
 * Features high-contrast donut arcs, central KPI summary, slice selection, and legend pills.
 */
@Composable
fun RechartsDonutChart(
    slices: List<RechartsDonutSlice>,
    centerTitle: String = "Total Sales",
    modifier: Modifier = Modifier
) {
    if (slices.isEmpty()) return

    val total = remember(slices) { slices.sumOf { it.value }.coerceAtLeast(1.0) }
    var selectedSliceIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = DkkSlate),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "<PieChart />",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = DkkGold,
                            fontSize = 13.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DkkGold.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, DkkGold)
                        ) {
                            Text("Recharts", color = DkkGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                    Text("Seller GMV Share Distribution", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Donut Center + Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(160.dp)
                        .pointerInput(slices) {
                            detectTapGestures { offset ->
                                // Cycle through slices on tap
                                selectedSliceIndex = if (selectedSliceIndex == null) {
                                    0
                                } else {
                                    val next = (selectedSliceIndex!! + 1) % slices.size
                                    next
                                }
                            }
                        }
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f
                    val strokeWidth = 26f

                    var startAngle = -90f
                    slices.forEachIndexed { idx, slice ->
                        val sweepAngle = ((slice.value / total) * 360f).toFloat()
                        val isSelected = selectedSliceIndex == idx
                        val currentStroke = if (isSelected) strokeWidth + 6f else strokeWidth

                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle - 2f,
                            useCenter = false,
                            topLeft = Offset(center.x - radius + (strokeWidth / 2f), center.y - radius + (strokeWidth / 2f)),
                            size = Size((radius - strokeWidth / 2f) * 2f, (radius - strokeWidth / 2f) * 2f),
                            style = Stroke(width = currentStroke, cap = StrokeCap.Round)
                        )
                        startAngle += sweepAngle
                    }
                }

                // Central KPI
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(centerTitle, color = Color(0xFF94A3B8), fontSize = 10.sp)
                    Text("Rs %,.0f".format(total), color = DkkGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Across ${slices.size} Sellers", color = Color(0xFF64748B), fontSize = 9.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recharts Legend Grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                slices.forEachIndexed { idx, slice ->
                    val isSelected = selectedSliceIndex == idx
                    val pct = (slice.value / total) * 100.0
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) slice.color.copy(alpha = 0.15f) else Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 1.2.dp else 0.5.dp,
                            if (isSelected) slice.color else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedSliceIndex = if (selectedSliceIndex == idx) null else idx
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.size(10.dp).background(slice.color, CircleShape))
                                Text(slice.label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                if (slice.subLabel.isNotBlank()) {
                                    Text("(${slice.subLabel})", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Rs %,.0f".format(slice.value), color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Surface(shape = RoundedCornerShape(4.dp), color = slice.color.copy(alpha = 0.2f)) {
                                    Text("%.1f%%".format(pct), color = slice.color, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
