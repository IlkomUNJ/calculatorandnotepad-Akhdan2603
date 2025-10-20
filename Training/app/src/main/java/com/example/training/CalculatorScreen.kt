package com.example.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

enum class KeyType { NUMBER, OPERATOR, SCIENTIFIC, ACTION, EQUALS }

val ScientificColor = Color(0xFFFFA07A)
val OperatorColor = Color(0xFFFF8C00)
val ClearColor = Color(0xFFDC143C)
val EqualsColor = Color(0xFF00BFFF)

@Composable
fun getButtonColors(type: String): ButtonColors {
    return when (type) {
        "scientific" -> ButtonDefaults.buttonColors(containerColor = ScientificColor, contentColor = Color.White)
        "operator" -> ButtonDefaults.buttonColors(containerColor = OperatorColor, contentColor = Color.White)
        "clear" -> ButtonDefaults.buttonColors(containerColor = ClearColor, contentColor = Color.White)
        "equals" -> ButtonDefaults.buttonColors(containerColor = EqualsColor, contentColor = Color.White)
        else -> ButtonDefaults.buttonColors(containerColor = Color.Gray, contentColor = Color.Black)
    }
}

@Composable
fun CalculatorButton(
    text: String, type: String, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick, colors = getButtonColors(type), modifier = modifier.aspectRatio(1f).padding(4.dp)
    ) {
        Text(text = text, fontSize = 20.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(
    currentDisplay: String, result: String, isScientificMode: Boolean, isDark: Boolean,
    onDigitClick: (String) -> Unit, onClearClick: () -> Unit, onEraseClick: () -> Unit,
    onOperatorClick: (String) -> Unit, onToggleScientificMode: () -> Unit, onToggleTheme: () -> Unit,
    onNavigateToMainMenu: () -> Unit, onNavigateToTextEditor: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculator") },
                navigationIcon = {
                    TextButton(onClick = onToggleScientificMode) { Text(if (isScientificMode) "STD" else "SCI", fontSize = 18.sp) }
                },
                actions = {
                    TextButton(onClick = onNavigateToMainMenu) { Text("MENU", fontSize = 16.sp) }
                    TextButton(onClick = onNavigateToTextEditor) { Text("TEXT", fontSize = 16.sp) }
                    ThemeToggle(isDark = isDark, onToggle = onToggleTheme)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp)) {
            Spacer(modifier = Modifier.weight(0.5f))
            Display(expression = currentDisplay, result = result)
            Spacer(modifier = Modifier.height(12.dp))

            if (isScientificMode) {
                val scientificKeys1 = listOf("sin", "cos", "tan", "log", "ln")
                val scientificKeys2 = listOf("asin", "acos", "atan", "1/x", "x!")
                val constantKeys = listOf("(", ")", "π", "e", "^", "√")

                KeyRow(keys = scientificKeys1, keyType = KeyType.SCIENTIFIC, onKey = { k -> onDigitClick("$k(") })
                Spacer(modifier = Modifier.height(12.dp))
                KeyRow(keys = scientificKeys2, keyType = KeyType.SCIENTIFIC, onKey = { k -> onDigitClick(when (k) { "1/x" -> "1/"; "x!" -> "fact("; else -> "$k(" }) })
                Spacer(modifier = Modifier.height(12.dp))
                KeyRow(keys = constantKeys, keyType = KeyType.SCIENTIFIC, onKey = { k -> onDigitClick(when (k) { "π" -> "pi"; "e" -> "e"; "√" -> "sqrt("; else -> k }) })
                Spacer(modifier = Modifier.height(12.dp))
            }

            CalculatorKeypad(
                onDigitClick = onDigitClick, onClearClick = onClearClick, onEraseClick = onEraseClick,
                onOperatorClick = onOperatorClick, onNavigateToMainMenu = onNavigateToMainMenu,
                onNavigateToTextEditor = onNavigateToTextEditor
            )
        }
    }
}

@Composable
fun CalculatorKeypad(
    onDigitClick: (String) -> Unit, onClearClick: () -> Unit, onEraseClick: () -> Unit,
    onOperatorClick: (String) -> Unit, onNavigateToMainMenu: () -> Unit, onNavigateToTextEditor: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalculatorButton(text = "C", type = "clear", onClick = onClearClick, modifier = Modifier.weight(1f))
            CalculatorButton(text = "⌫", type = "clear", onClick = onEraseClick, modifier = Modifier.weight(1f))
            CalculatorButton(text = "%", type = "operator", onClick = { onOperatorClick("%") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "÷", type = "operator", onClick = { onOperatorClick("/") }, modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalculatorButton(text = "7", type = "digit", onClick = { onDigitClick("7") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "8", type = "digit", onClick = { onDigitClick("8") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "9", type = "digit", onClick = { onDigitClick("9") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "x", type = "operator", onClick = { onOperatorClick("*") }, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalculatorButton(text = "4", type = "digit", onClick = { onDigitClick("4") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "5", type = "digit", onClick = { onDigitClick("5") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "6", type = "digit", onClick = { onDigitClick("6") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "-", type = "operator", onClick = { onOperatorClick("-") }, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalculatorButton(text = "1", type = "digit", onClick = { onDigitClick("1") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "2", type = "digit", onClick = { onDigitClick("2") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "3", type = "digit", onClick = { onDigitClick("3") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "+", type = "operator", onClick = { onOperatorClick("+") }, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalculatorButton(text = "e", type = "scientific", onClick = { onDigitClick("e") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "0", type = "digit", onClick = { onDigitClick("0") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = ".", type = "digit", onClick = { onDigitClick(".") }, modifier = Modifier.weight(1f))
            Button(onClick = { onOperatorClick("=") }, colors = getButtonColors("equals"), modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp)) { Text("=", fontSize = 20.sp) }
        }
    }
}

@Composable
fun Display(expression: String, result: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = expression.ifEmpty { "0" }, fontSize = 36.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), textAlign = TextAlign.End, maxLines = 2)
        Text(text = result, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.End, maxLines = 1)
    }
}

@Composable
fun ThemeToggle(isDark: Boolean, onToggle: () -> Unit) {
    val label = if (isDark) "Dark" else "Light"

    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 8.dp),

        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = textColor)
    }
}

@Composable
fun KeyRow(keys: List<String>, keyType: KeyType, onKey: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        keys.forEach { label ->
            Key(label = label, modifier = Modifier.weight(1f), keyType = keyType, onClick = { onKey(label) })
        }
    }
}

@Composable
fun Key(
    label: String, modifier: Modifier = Modifier, keyType: KeyType, onClick: () -> Unit
) {
    val (bgColor, fgColor) = when (keyType) {
        KeyType.NUMBER -> MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurface
        KeyType.OPERATOR -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        KeyType.SCIENTIFIC -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        KeyType.ACTION -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        KeyType.EQUALS -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    }

    Box(
        modifier = modifier.heightIn(min = 64.dp).clip(RoundedCornerShape(16.dp)).background(bgColor).clickable { onClick() }.padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 22.sp, color = fgColor, fontWeight = FontWeight.Medium)
    }
}