package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.data.security.OwnerSecurityGuard
import com.example.ui.theme.DkkEmerald
import com.example.ui.theme.DkkGold
import com.example.ui.theme.DkkNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSwitchBottomSheet(
    currentRole: UserRole,
    onDismiss: () -> Unit,
    onRoleSelected: (UserRole) -> Unit,
    onLogout: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showMasterPasswordPrompt by remember { mutableStateOf(false) }
    var masterPasswordInput by remember { mutableStateOf("") }
    var masterPasswordVisible by remember { mutableStateOf(false) }
    var masterPasswordError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = DkkEmerald,
                        modifier = Modifier.clickable { showMasterPasswordPrompt = true }
                    )
                    Column {
                        Text(
                            text = "Account Role Switcher",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Switch between active shopping & selling roles",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Roles List: Standard users see Buyer and Seller. Owner can switch back to Owner.
            val visibleRoles = if (currentRole == UserRole.OWNER) {
                listOf(UserRole.BUYER, UserRole.SELLER, UserRole.OWNER)
            } else {
                listOf(UserRole.BUYER, UserRole.SELLER)
            }

            visibleRoles.forEach { role ->
                val isSelected = currentRole == role
                Card(
                    onClick = {
                        if (role == UserRole.OWNER) {
                            showMasterPasswordPrompt = true
                        } else {
                            onRoleSelected(role)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(role.badgeColorHex).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(role.badgeColorHex)) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("role_item_${role.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(role.badgeColorHex).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (role) {
                                        UserRole.BUYER -> Icons.Default.ShoppingBag
                                        UserRole.SELLER -> Icons.Default.Storefront
                                        UserRole.OWNER -> Icons.Default.AdminPanelSettings
                                    },
                                    contentDescription = null,
                                    tint = Color(role.badgeColorHex),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = role.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = when (role) {
                                        UserRole.BUYER -> "Explore feed, buy with Easypaisa & receive OTP"
                                        UserRole.SELLER -> "Muhammad Aslam (Universe Jewellery) - Manage Listings & Level"
                                        UserRole.OWNER -> "Clear view of users, VIPs, 4-tier commission wallet"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Active",
                                tint = Color(role.badgeColorHex),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (onLogout != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    onClick = {
                        onDismiss()
                        onLogout()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("role_switch_logout_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Log Out",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Log Out (لاگ آؤٹ)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFFDC2626)
                                )
                                Text(
                                    text = "Sign out from this account & return to login screen",
                                    fontSize = 11.sp,
                                    color = Color(0xFF991B1B)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showMasterPasswordPrompt) {
        AlertDialog(
            onDismissRequest = {
                showMasterPasswordPrompt = false
                masterPasswordInput = ""
                masterPasswordError = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = DkkGold)
                    Text("Owner Authentication", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        "Owner Security Protocol (Daro Khan)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DkkGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Yeh option sirf Platform Owner ke liye he. Apna High-Security Alphanumeric Password darj karein:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = masterPasswordInput,
                        onValueChange = {
                            masterPasswordInput = it
                            masterPasswordError = null
                        },
                        label = { Text("Master Password") },
                        placeholder = { Text("Enter Master Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = DkkGold)
                        },
                        trailingIcon = {
                            IconButton(onClick = { masterPasswordVisible = !masterPasswordVisible }) {
                                Icon(
                                    imageVector = if (masterPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (masterPasswordVisible) "Hide password" else "Show password",
                                    tint = DkkGold
                                )
                            }
                        },
                        visualTransformation = if (masterPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = masterPasswordError != null,
                        supportingText = masterPasswordError?.let { { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("roleswitch_master_password_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (OwnerSecurityGuard.verifyOwnerAccess("03330206001", masterPasswordInput)) {
                            showMasterPasswordPrompt = false
                            masterPasswordInput = ""
                            masterPasswordError = null
                            onRoleSelected(UserRole.OWNER)
                        } else {
                            masterPasswordError = "Ghalt Password! Access Denied."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DkkGold)
                ) {
                    Text("Verify & Switch", fontWeight = FontWeight.Bold, color = DkkNavy)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showMasterPasswordPrompt = false
                    masterPasswordInput = ""
                    masterPasswordError = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
