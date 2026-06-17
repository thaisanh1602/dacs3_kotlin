package com.example.angrismart.ui.theme

import androidx.compose.ui.graphics.Color

// ===== BRAND PALETTE – Plant Care Professional Style =====

// Primary greens – deep forest tones
val ForestGreen   = Color(0xFF2D5A27)  // Primary brand – deep forest
val SageGreen     = Color(0xFF4A7C59)  // Secondary – sage
val MintGreen     = Color(0xFF5BAD6F)  // Accent – fresh mint
val LightMint     = Color(0xFFE8F5E9)  // Light tint for badges / chips
val PaleGreen     = Color(0xFFF0F7F1)  // Very light surface tint

// Glassmorphism styling tokens
val GlassBgStart    = Color(0xFFE6F0EA) // Soft mint green background gradient start
val GlassBgEnd      = Color(0xFFD3EAE0) // Soft mint green background gradient end
val GlassCardBg     = Color(0xCCFFFFFF) // Translucent white for glassmorphism card (80% opacity)
val GlassCardBorder = Color(0x4DFFFFFF) // White border with 30% opacity for glass reflection
val GlassCardBorderDark = Color(0x1F2D5A27) // Forest green tinted border with 12% opacity
val GlassNavBg      = Color(0xE6FFFFFF) // Highly opaque translucent white for bottom bar

// Semantic colors
val WarningAmber  = Color(0xFFF59E0B)  // Warning states
val DangerRed     = Color(0xFFDC2626)  // Error / danger
val InfoBlue      = Color(0xFF0EA5E9)  // Info / sky

// Neutral palette
val NeutralBg     = Color(0xFFF5F7F5)  // Screen background
val SurfaceWhite  = Color(0xFFFFFFFF)  // Card / surface
val DividerLine   = Color(0xFFE8EDE8)  // Dividers
val TextPrimary   = Color(0xFF1A2E1A)  // Dark text
val TextSecondary = Color(0xFF6B7B6B)  // Muted text
val TextLight     = Color(0xFFFFFFFF)  // White text on dark backgrounds

// Legacy aliases (kept for backward-compat with existing screens)
val GreenPrimary   = ForestGreen
val GreenSecondary = MintGreen
val YellowWarning  = WarningAmber
val RedError       = DangerRed
val BackgroundLight = NeutralBg