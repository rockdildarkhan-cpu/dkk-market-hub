package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserRole
import com.example.data.security.OwnerSecurityGuard
import com.example.data.security.SellerSecurityGuard
import com.example.ui.components.FaceScanVerificationSheet
import com.example.ui.theme.DkkEmerald
import com.example.ui.theme.DkkGold
import com.example.ui.theme.DkkNavy
import com.example.ui.theme.DkkSlate
import com.example.ui.theme.EasypaisaGreen

@Composable
fun AuthScreen(
    onLoginSuccess: (phone: String, name: String, email: String, role: UserRole) -> Unit,
    onQuickPersonaSelected: (UserRole) -> Unit
) {
    var currentStep by remember { mutableStateOf(1) } // 1: Phone, 2: OTP, 3: Seller KYC, 4: Owner Password
    var phoneNumber by remember { mutableStateOf("03273856001") }
    var userName by remember { mutableStateOf("Ahmed Nawaz") }
    var emailAddress by remember { mutableStateOf("buyer.ahmed@gmail.com") }
    var otpCode by remember { mutableStateOf("1234") }
    var selectedRole by remember { mutableStateOf(UserRole.BUYER) }
    var isFaceScanComplete by remember { mutableStateOf(false) }
    var showFaceScanModal by remember { mutableStateOf(false) }

    // Strict High-Security Owner Password Access State
    var showOwnerPasswordModal by remember { mutableStateOf(false) }
    var ownerPasswordInput by remember { mutableStateOf("") }
    var ownerPasswordVisible by remember { mutableStateOf(false) }
    var ownerPasswordError by remember { mutableStateOf<String?>(null) }

    // 1st Seller (Muhammad Aslam - Universe Jewellery) Password State
    var sellerPasswordInput by remember { mutableStateOf("aslamdkk1") }
    var sellerPasswordVisible by remember { mutableStateOf(false) }
    var sellerPasswordError by remember { mutableStateOf<String?>(null) }

    val isOwnerPhone = remember(phoneNumber) { OwnerSecurityGuard.isOwnerPhone(phoneNumber) }
    val isSeller1 = remember(phoneNumber, selectedRole, userName) {
        SellerSecurityGuard.isSeller1Phone(phoneNumber) || (selectedRole == UserRole.SELLER && (userName.contains("Muhammad Aslam") || userName.contains("Aslam") || phoneNumber.contains("3108219408")))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Step 1: DKK Logo Header (Clickable for authorized Owner Master Password)
        Box(
            modifier = Modifier
                .size(115.dp)
                .clip(CircleShape)
                .background(DkkNavy)
                .border(2.5.dp, DkkGold, CircleShape)
                .clickable { showOwnerPasswordModal = true },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.dkk_marketing_logo),
                contentDescription = "DKK Marketing Logo",
                modifier = Modifier.size(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "DKK Marketing",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Secure Unified Role Authentication",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Role Selector Segmented Control (Strictly Public: Buyer & Seller only)
        Text(
            text = "Select Account Role:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(UserRole.BUYER, UserRole.SELLER).forEach { role ->
                val isSelected = selectedRole == role
                Surface(
                    onClick = {
                        selectedRole = role
                        when (role) {
                            UserRole.BUYER -> {
                                phoneNumber = "03273856001"
                                userName = "Ahmed Nawaz"
                                emailAddress = "buyer.ahmed@gmail.com"
                            }
                            UserRole.SELLER -> {
                                phoneNumber = "+92 310 8219408"
                                userName = "Muhammad Aslam"
                                emailAddress = "aslam.universe@dkk.pk"
                            }
                            else -> {}
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(role.badgeColorHex) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("auth_role_tab_${role.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (role) {
                                UserRole.BUYER -> Icons.Default.ShoppingBag
                                UserRole.SELLER -> Icons.Default.Storefront
                                else -> Icons.Default.ShoppingBag
                            },
                            contentDescription = null,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = role.displayName,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step Content Carousel
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "auth_step_anim"
        ) { step ->
            when (step) {
                1 -> {
                    // Step 2: Mobile Number & Name Input
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Step 2: Enter Mobile Number",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "An SMS verification OTP will be sent to your device",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = userName,
                                onValueChange = { userName = it },
                                label = { Text("Full Name / Business Title") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_name_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Mobile Number") },
                                placeholder = { Text("03XXXXXXXXX") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_phone_input")
                            )

                            if (isOwnerPhone) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = DkkNavy,
                                    border = BorderStroke(1.dp, DkkGold),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = DkkGold, modifier = Modifier.size(24.dp))
                                        Column {
                                            Text(
                                                text = "Ufone Identity Guard: Daro Khan (Owner)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = DkkGold
                                            )
                                            Text(
                                                text = "Master Account pehchan liya gaya. Direct High-Security Master Password Auth zaroori hai.",
                                                fontSize = 11.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            if (selectedRole == UserRole.SELLER || isSeller1) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = sellerPasswordInput,
                                    onValueChange = {
                                        sellerPasswordInput = it
                                        sellerPasswordError = null
                                    },
                                    label = { Text("Seller Password") },
                                    placeholder = { Text("Enter seller password (aslamdkk1)") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = DkkGold)
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { sellerPasswordVisible = !sellerPasswordVisible }) {
                                            Icon(
                                                imageVector = if (sellerPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (sellerPasswordVisible) "Hide password" else "Show password"
                                            )
                                        }
                                    },
                                    visualTransformation = if (sellerPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    isError = sellerPasswordError != null,
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_seller_password_input")
                                )

                                if (sellerPasswordError != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = sellerPasswordError ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = DkkNavy,
                                    border = BorderStroke(1.dp, DkkGold.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Default.Storefront, contentDescription = null, tint = DkkGold, modifier = Modifier.size(24.dp))
                                        Column {
                                            Text(
                                                text = "1st Seller: Muhammad Aslam (Universe Jewellery)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = DkkGold
                                            )
                                            Text(
                                                text = "Phone: +92 310 8219408 • ID: SELLER_001 • Password: aslamdkk1",
                                                fontSize = 11.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        if (SellerSecurityGuard.verifySellerPassword(phoneNumber, sellerPasswordInput)) {
                                            sellerPasswordError = null
                                            onLoginSuccess("03108219408", "Muhammad Aslam (Universe Jewellery)", "aslam.universe@dkk.pk", UserRole.SELLER)
                                        } else {
                                            sellerPasswordError = "Ghalat Seller Password! Sahi password 'aslamdkk1' darj karein."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DkkGold),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("auth_seller_password_login_btn")
                                ) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = DkkNavy, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Login with Seller Password (aslamdkk1)", color = DkkNavy, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (isOwnerPhone) {
                                        currentStep = 4 // Direct to Owner Password verification
                                    } else {
                                        currentStep = 2 // Standard user OTP flow
                                    }
                                },
                                enabled = phoneNumber.length >= 10 && userName.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isOwnerPhone) DkkGold else DkkEmerald
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("auth_send_otp_btn")
                            ) {
                                Text(
                                    text = if (isOwnerPhone) "Verify Owner Password 🛡️" else "Send Verification OTP",
                                    color = if (isOwnerPhone) DkkNavy else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (isOwnerPhone) Icons.Default.Shield else Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = if (isOwnerPhone) DkkNavy else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Step 3: OTP Verification
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Step 3: Enter 4-Digit OTP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Code sent to $phoneNumber (Demo default: 1234)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { if (it.length <= 4) otpCode = it },
                                label = { Text("4-Digit Code") },
                                leadingIcon = {
                                    Icon(Icons.Default.Key, contentDescription = null)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_otp_input")
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { currentStep = 1 },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Back")
                                }

                                Button(
                                    onClick = {
                                        if (isOwnerPhone) {
                                            // Owner identity strictly requires high-security password verification
                                            currentStep = 4
                                        } else if (selectedRole == UserRole.SELLER) {
                                            if (isSeller1) {
                                                // Muhammad Aslam is pre-verified 1st seller
                                                onLoginSuccess("03108219408", "Muhammad Aslam (Universe Jewellery)", "aslam.universe@dkk.pk", UserRole.SELLER)
                                            } else {
                                                currentStep = 3 // Move to seller email & face scan step
                                            }
                                        } else {
                                            onLoginSuccess(phoneNumber, userName, emailAddress, selectedRole)
                                        }
                                    },
                                    enabled = otpCode.length == 4,
                                    colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .testTag("auth_verify_otp_btn")
                                ) {
                                    Text("Verify & Continue", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Step 4 (For Seller Only): Email ID + Face Scan
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Step 4: Seller Biometric KYC",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Provide business email & complete face scan to unlock seller dashboard.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = emailAddress,
                                onValueChange = { emailAddress = it },
                                label = { Text("Business Email Address") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_seller_email_input")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Face Scan Trigger Button
                            Surface(
                                onClick = { showFaceScanModal = true },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isFaceScanComplete) Color(0xFFE8F5E9) else DkkNavy,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isFaceScanComplete) EasypaisaGreen else DkkGold
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_trigger_face_scan_card")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFaceScanComplete) Icons.Default.CheckCircle else Icons.Default.Face,
                                            contentDescription = null,
                                            tint = if (isFaceScanComplete) EasypaisaGreen else DkkGold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column {
                                            Text(
                                                text = if (isFaceScanComplete) "Face Scan Verified" else "Biometric Face Scan",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isFaceScanComplete) Color(0xFF1B5E20) else Color.White
                                            )
                                            Text(
                                                text = if (isFaceScanComplete) "Biometric match 100% complete" else "Tap to scan facial landmarks",
                                                fontSize = 11.sp,
                                                color = if (isFaceScanComplete) Color(0xFF2E7D32) else Color(0xFF94A3B8)
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Scan",
                                        tint = if (isFaceScanComplete) EasypaisaGreen else DkkGold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    onLoginSuccess(phoneNumber, userName, emailAddress, UserRole.SELLER)
                                },
                                enabled = emailAddress.isNotBlank() && isFaceScanComplete,
                                colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("auth_complete_seller_btn")
                            ) {
                                Text("Unlock Seller Dashboard", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                4 -> {
                    // Step 4: High-Security Owner Alphanumeric Password Verification
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DkkSlate),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, DkkGold),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("owner_password_verification_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(DkkNavy, CircleShape)
                                    .border(1.5.dp, DkkGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = DkkGold,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Owner High-Security Access",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Identity: Daro Khan (Master Admin)",
                                fontSize = 12.sp,
                                color = DkkGold,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Sirf PIN nahi, ab aik mukammal high-security alphanumeric password darj karein.",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = ownerPasswordInput,
                                onValueChange = {
                                    ownerPasswordInput = it
                                    ownerPasswordError = null
                                },
                                label = { Text("Master Alphanumeric Password") },
                                placeholder = { Text("Enter Master Password") },
                                leadingIcon = {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = DkkGold)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { ownerPasswordVisible = !ownerPasswordVisible }) {
                                        Icon(
                                            imageVector = if (ownerPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (ownerPasswordVisible) "Hide password" else "Show password",
                                            tint = DkkGold
                                        )
                                    }
                                },
                                visualTransformation = if (ownerPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                isError = ownerPasswordError != null,
                                supportingText = {
                                    if (ownerPasswordError != null) {
                                        Text(ownerPasswordError!!, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                    } else {
                                        Text(
                                            "Must contain uppercase, lowercase, numbers, and symbols (@, #, $, !)",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.sp
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("owner_password_input")
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        currentStep = 1
                                        ownerPasswordError = null
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Back")
                                }

                                Button(
                                    onClick = {
                                        if (OwnerSecurityGuard.verifyOwnerAccess(phoneNumber, ownerPasswordInput)) {
                                            ownerPasswordError = null
                                            onLoginSuccess(
                                                OwnerSecurityGuard.MASTER_ADMIN_PHONE,
                                                "Daro Khan (DKK Owner)",
                                                OwnerSecurityGuard.MASTER_ADMIN_EMAIL,
                                                UserRole.OWNER
                                            )
                                        } else {
                                            ownerPasswordError = "Ghalt Password! Access Denied."
                                        }
                                    },
                                    enabled = ownerPasswordInput.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = DkkGold),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1.8f)
                                        .testTag("owner_verify_password_btn")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DkkNavy, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Verify & Unlock", color = DkkNavy, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Switch Direct Demo Personas Box (Public demo: Buyer & Seller only)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Quick Demo Personas:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { onQuickPersonaSelected(UserRole.BUYER) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_persona_buyer")
                    ) {
                        Text("Buyer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onQuickPersonaSelected(UserRole.SELLER) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_persona_seller")
                    ) {
                        Text("Seller", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // High-Security Owner Access Verification Dialog
    if (showOwnerPasswordModal) {
        AlertDialog(
            onDismissRequest = {
                showOwnerPasswordModal = false
                ownerPasswordInput = ""
                ownerPasswordError = null
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = DkkGold)
                    Text("Owner Identity Verification", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        "Owner Identity Guard: Daro Khan",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = DkkGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Sirf PIN nahi, ab mukammal High-Security Alphanumeric Password darj karein:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = ownerPasswordInput,
                        onValueChange = {
                            ownerPasswordInput = it
                            ownerPasswordError = null
                        },
                        label = { Text("Master Password") },
                        placeholder = { Text("Enter Master Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = DkkGold)
                        },
                        trailingIcon = {
                            IconButton(onClick = { ownerPasswordVisible = !ownerPasswordVisible }) {
                                Icon(
                                    imageVector = if (ownerPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (ownerPasswordVisible) "Hide password" else "Show password",
                                    tint = DkkGold
                                )
                            }
                        },
                        visualTransformation = if (ownerPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = ownerPasswordError != null,
                        supportingText = ownerPasswordError?.let { { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("modal_owner_password_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (OwnerSecurityGuard.verifyOwnerAccess("03330206001", ownerPasswordInput)) {
                            showOwnerPasswordModal = false
                            ownerPasswordInput = ""
                            ownerPasswordError = null
                            onLoginSuccess(
                                OwnerSecurityGuard.MASTER_ADMIN_PHONE,
                                "Daro Khan (DKK Owner)",
                                OwnerSecurityGuard.MASTER_ADMIN_EMAIL,
                                UserRole.OWNER
                            )
                        } else {
                            ownerPasswordError = "Ghalt Password! Access Denied."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DkkGold)
                ) {
                    Text("Verify & Open", fontWeight = FontWeight.Bold, color = DkkNavy)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showOwnerPasswordModal = false
                    ownerPasswordInput = ""
                    ownerPasswordError = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFaceScanModal) {
        FaceScanVerificationSheet(
            sellerName = userName,
            onDismiss = { showFaceScanModal = false },
            onVerificationComplete = {
                isFaceScanComplete = true
                showFaceScanModal = false
            }
        )
    }
}
