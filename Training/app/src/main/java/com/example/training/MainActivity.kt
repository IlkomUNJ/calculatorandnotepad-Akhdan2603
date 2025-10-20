package com.example.training

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.training.ui.theme.TrainingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrainingTheme {
                AppNavigation()
            }
        }
    }
}

object NavDestinations {
    const val MAIN_MENU = "main_menu_screen"
    const val CALCULATOR = "calculator_screen"
    const val NOTES_AND_EDITOR = "notes_and_editor_screen"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val calculatorViewModel: CalculatorViewModel = viewModel()
    val isDark by calculatorViewModel::isDark
    MaterialTheme(colorScheme = if (isDark) darkColorScheme() else lightColorScheme()) {
        NavHost(
            navController = navController,
            startDestination = NavDestinations.MAIN_MENU
        ) {
            composable(NavDestinations.MAIN_MENU) {
                MainMenuScreen(
                    onNavigateToCalculator = { navController.navigate(NavDestinations.CALCULATOR) },
                    onNavigateToTextEditor = { navController.navigate(NavDestinations.NOTES_AND_EDITOR) },
                )
            }

            composable(NavDestinations.CALCULATOR) {
                val currentDisplay by calculatorViewModel::expression
                val result by calculatorViewModel::result
                val isScientificMode by calculatorViewModel::isScientificMode

                Screen(
                    currentDisplay = currentDisplay,
                    result = result,
                    isScientificMode = isScientificMode,
                    isDark = isDark,

                    onDigitClick = calculatorViewModel::onKey,
                    onOperatorClick = { op ->
                        if (op == "=") {
                            calculatorViewModel.onEquals()
                        } else {
                            calculatorViewModel.onKey(op)
                        }
                    },

                    onClearClick = calculatorViewModel::onClear,
                    onEraseClick = calculatorViewModel::onBackspace,
                    onToggleScientificMode = calculatorViewModel::toggleScientificMode,
                    onToggleTheme = calculatorViewModel::toggleTheme,

                    onNavigateToMainMenu = { navController.navigate(NavDestinations.MAIN_MENU) },
                    onNavigateToTextEditor = { navController.navigate(NavDestinations.NOTES_AND_EDITOR) }
                )
            }

            composable(NavDestinations.NOTES_AND_EDITOR) {
                val textEditorViewModel: TextEditorViewModel = viewModel()
                val isDark by textEditorViewModel::isDark

                MaterialTheme(
                    colorScheme = if (isDark) darkColorScheme() else lightColorScheme()
                ) {
                    TextAndNotesScreen(
                        viewModel = textEditorViewModel,
                        onBackToMainMenu = { navController.navigate(NavDestinations.MAIN_MENU) },
                        onBackToHome = { navController.popBackStack() },
                        onToggleTheme = textEditorViewModel::toggleTheme
                    )
                }
            }
        }
    }
}


