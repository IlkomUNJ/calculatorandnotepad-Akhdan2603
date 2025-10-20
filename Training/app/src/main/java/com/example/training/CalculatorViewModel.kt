package com.example.training

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.math.*

class CalculatorViewModel : ViewModel() {
    var expression by mutableStateOf("")
        private set
    var result by mutableStateOf("")
        private set
    var isScientificMode by mutableStateOf(false)
        private set
    var isDark by mutableStateOf(false)
        private set

    fun onKey(key: String) {
        val inputKey = when (key) {
            "x" -> "*"
            "÷" -> "/"
            else -> key
        }

        if (expression.startsWith("0") && expression.length == 1 && inputKey.first().isDigit()) {
            expression = inputKey
        } else if (inputKey == "0" && expression == "0") {
        }
        else {
            expression += inputKey
        }
    }

    fun onClear() {
        expression = ""
        result = ""
    }

    fun onBackspace() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
        } else {
            expression = ""
        }
    }

    fun toggleScientificMode() {
        isScientificMode = !isScientificMode
    }

    fun toggleTheme() {
        isDark = !isDark
    }

    fun onEquals() {
        result = try {
            if (expression.isNotBlank()) evaluateExpression(expression).toString() else ""
        } catch (e: Exception) {
            "Error: Invalid Expression"
        }
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n)) throw IllegalArgumentException("Factorial only for non-negative integers")
        if (n == 0.0) return 1.0
        var result = 1.0
        for (i in 1..n.toInt()) { result *= i }
        return result
    }

    fun evaluateExpression(exprRaw: String): Double {
        val exprProcessed = if (exprRaw.startsWith("1/")) {
            val numberPart = exprRaw.substring(2)
            try { val innerResult = evaluateExpression(numberPart); (1 / innerResult).toString() }
            catch (e: Exception) { exprRaw }
        } else { exprRaw }

        val expr = exprProcessed.replace("\\s+".toRegex(), "").replace("pi", Math.PI.toString()).replace("e", Math.E.toString())

        val tokens = tokenize(expr)
        val rpn = toRpn(tokens)
        return evalRpn(rpn)
    }

    private fun tokenize(s: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c.isDigit() || c == '.' -> {
                    var j = i + 1
                    while (j < s.length && (s[j].isDigit() || s[j] == '.')) j++
                    tokens.add(s.substring(i, j)); i = j
                }
                c.isLetter() -> {
                    var j = i + 1
                    while (j < s.length && s[j].isLetter()) j++
                    tokens.add(s.substring(i, j)); i = j
                }
                c in "+-*/^()" -> { tokens.add(c.toString()); i++ }
                c == '/' && i > 0 && s[i - 1] == '1' && (i == 1 || !s[i - 2].isDigit()) -> {
                    if (tokens.last() == "1") tokens.removeAt(tokens.lastIndex)
                    tokens.add("1/")
                    i++
                }
                else -> throw IllegalArgumentException("Invalid char: $c")
            }
        }
        return tokens
    }
    private fun precedence(op: String) = when (op) { "^" -> 4; "1/" -> 4; "*" , "/" -> 3; "+" , "-" -> 2; else -> 0 }
    private fun isRightAssociative(op: String) = op == "^"
    private fun isFunction(name: String) = name in setOf("sin", "cos", "tan", "log", "ln", "sqrt", "asin", "acos", "atan", "fact")
    private fun toRpn(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()
        tokens.forEachIndexed { idx, t ->
            when {
                t.toDoubleOrNull() != null -> output.add(t)
                isFunction(t) -> stack.addLast(t)
                t in "+-*/^" || t == "1/" -> {
                    if (t == "-" && (idx == 0 || tokens[idx - 1] in "+-*/^(")) output.add("0")

                    while (stack.isNotEmpty()) {
                        val top = stack.last()
                        if ((top in "+-*/^" || top == "1/") &&
                            (precedence(top) > precedence(t) ||
                                    (precedence(top) == precedence(t) && !isRightAssociative(t)))
                        ) {
                            output.add(stack.removeLast())
                        } else break
                    }
                    stack.addLast(t)
                }
                t == "(" -> stack.addLast(t)
                t == ")" -> {
                    while (stack.isNotEmpty() && stack.last() != "(") {
                        output.add(stack.removeLast())
                    }
                    if (stack.isEmpty()) throw IllegalArgumentException("Mismatched parentheses")
                    stack.removeLast()

                    if (stack.isNotEmpty() && isFunction(stack.last())) {
                        output.add(stack.removeLast())
                    }
                }
            }
        }
        while (stack.isNotEmpty()) {
            val top = stack.removeLast()
            if (top == "(" || top == ")") throw IllegalArgumentException("Mismatched parentheses")
            output.add(top)
        }
        return output
    }

    private fun evalRpn(tokens: List<String>): Double {
        val stack = ArrayDeque<Double>()
        for (token in tokens) {
            val num = token.toDoubleOrNull()
            if (num != null) { stack.addLast(num) }
            else if (isFunction(token)) {
                if (stack.isEmpty()) throw IllegalArgumentException("Missing argument for $token")
                val arg = stack.removeLast()
                val res = when (token) {
                    "sin" -> sin(Math.toRadians(arg)); "cos" -> cos(Math.toRadians(arg)); "tan" -> tan(Math.toRadians(arg)); "log" -> log10(arg); "ln" -> ln(arg); "sqrt" -> sqrt(arg); "asin" -> Math.toDegrees(asin(arg)); "acos" -> Math.toDegrees(acos(arg)); "atan" -> Math.toDegrees(atan(arg)); "fact" -> factorial(arg); else -> throw IllegalArgumentException("Unknown function $token")
                }
                stack.addLast(res)
            } else {
                if (token == "1/") { if (stack.isEmpty()) throw IllegalArgumentException("Missing argument for 1/x"); val a = stack.removeLast(); stack.addLast(1 / a); continue }
                if (stack.size < 2) throw IllegalArgumentException("Missing operands for $token")
                val b = stack.removeLast(); val a = stack.removeLast()
                val res = when (token) {
                    "+" -> a + b; "-" -> a - b; "*" -> a * b; "/" -> a / b; "^" -> a.pow(b); else -> throw IllegalArgumentException("Unknown operator: $token")
                }
                stack.addLast(res)
            }
        }
        if (stack.size != 1) throw IllegalArgumentException("Invalid expression")
        return stack.single()
    }
}