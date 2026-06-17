package com.example.angrismart.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.input.TextFieldValue
import com.example.angrismart.ui.theme.*
import com.example.angrismart.viewmodel.ChatMessage
import com.example.angrismart.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var textInput by remember { mutableStateOf(TextFieldValue("")) }
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(GlassBgStart, GlassBgEnd)))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Trợ lý Nông nghiệp AI", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở lại", tint = TextPrimary)
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubble(msg)
                    }
                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(GlassCardBg)
                                        .border(1.dp, GlassCardBorder, RoundedCornerShape(16.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ForestGreen, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Chuyên gia đang gõ...", color = TextSecondary, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Box nhập tin nhắn phía dưới cùng
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(0.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.ime), // Tự động co giãn khi hiện bàn phím ảo
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Nhập câu hỏi... (VD: Thuốc sâu cuốn lá?)", fontSize = 14.sp) },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreen,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White.copy(alpha = 0.7f)
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Send
                            ),
                            maxLines = 3 // Cho phép gõ xuống dòng
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = {
                                if (textInput.text.isNotBlank()) {
                                    viewModel.sendMessage(textInput.text)
                                    textInput = TextFieldValue("")
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(ForestGreen, RoundedCornerShape(24.dp))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(76.dp)) // Tránh bị đè bởi Bottom Navigation Bar
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f),
            contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(if (isUser) ForestGreen else GlassCardBg)
                    .border(1.dp, if (isUser) Color.Transparent else GlassCardBorder, RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    ))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = msg.text,
                    color = if (isUser) Color.White else TextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
