package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirebaseConnectionState
import com.example.ui.theme.DkkEmerald
import com.example.ui.theme.DkkGold
import com.example.ui.theme.DkkNavy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseCloudSheet(
    state: FirebaseConnectionState,
    onSyncRequested: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    val packageName = "com.dkk.marketing"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFFFFA000).copy(alpha = 0.15f), CircleShape)
                        .border(1.5.dp, Color(0xFFFFA000), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Firebase Cloud",
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Firebase Cloud Connect",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Realtime Firestore & Authentication Engine",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (state.isOnline) DkkEmerald.copy(alpha = 0.12f) else Color(0xFFFFA000).copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (state.isOnline) DkkEmerald.copy(alpha = 0.4f) else Color(0xFFFFA000).copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (state.isOnline) Icons.Default.CheckCircle else Icons.Default.Cloud,
                        contentDescription = null,
                        tint = if (state.isOnline) DkkEmerald else Color(0xFFFFA000),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (state.isOnline) "Firebase Connected & Active" else "Firebase Ready (Local-First Offline)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (state.isOnline) DkkEmerald else Color(0xFFD97706)
                        )
                        Text(
                            text = state.statusSummary,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cloud Services Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Active Cloud Services:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FirebaseServiceItem(
                        icon = Icons.Default.Storage,
                        title = "Cloud Firestore Database",
                        detail = "Collections: products, orders, users, sellers",
                        statusText = if (state.isConfigured) "Initialized" else "Local Cache Ready"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FirebaseServiceItem(
                        icon = Icons.Default.Lock,
                        title = "Firebase Authentication",
                        detail = state.userEmail ?: "Anonymous / Phone / Email Auth ready",
                        statusText = if (state.isAuthenticated) "Authenticated" else "Ready"
                    )

                    if (state.lastSyncTimestamp > 0L) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val formattedTime = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(state.lastSyncTimestamp))
                        FirebaseServiceItem(
                            icon = Icons.Default.CloudDone,
                            title = "Last Cloud Sync",
                            detail = "$formattedTime (${state.syncedProductsCount} Products, ${state.syncedOrdersCount} Orders)",
                            statusText = "Synchronized"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Button(
                onClick = onSyncRequested,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firebase_sync_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sync All Data with Firebase Cloud", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions & Linked Project Credentials Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DkkEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Firebase Project Linked: ${state.projectId}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            color = DkkEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = DkkEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FirebaseConfigCopyRow(
                        label = "Package Name",
                        value = state.packageName,
                        onCopy = { clipboardManager.setText(AnnotatedString(state.packageName)) }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FirebaseConfigCopyRow(
                        label = "Project ID",
                        value = state.projectId,
                        onCopy = { clipboardManager.setText(AnnotatedString(state.projectId)) }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FirebaseConfigCopyRow(
                        label = "Project Number",
                        value = state.projectNumber,
                        onCopy = { clipboardManager.setText(AnnotatedString(state.projectNumber)) }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FirebaseConfigCopyRow(
                        label = "Firebase App ID",
                        value = state.appId,
                        onCopy = { clipboardManager.setText(AnnotatedString(state.appId)) }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FirebaseConfigCopyRow(
                        label = "SHA-1 Fingerprint",
                        value = state.sha1,
                        onCopy = { clipboardManager.setText(AnnotatedString(state.sha1)) }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FirebaseConfigCopyRow(
                        label = "SHA-256 Fingerprint",
                        value = state.sha256,
                        onCopy = { clipboardManager.setText(AnnotatedString(state.sha256)) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "✓ 'google-services.json' is configured with your project IDs and fingerprints in the app directory.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 15.sp,
                        color = DkkEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun FirebaseServiceItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    detail: String,
    statusText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 12.sp)
            Text(detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Text(
                text = statusText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun FirebaseConfigCopyRow(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy $label",
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

