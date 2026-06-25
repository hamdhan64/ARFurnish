package com.arfurnish

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.arfurnish.ui.screens.ARScreen
import com.arfurnish.ui.screens.AlphabetScreen
import com.arfurnish.ui.screens.DealsScreen
import com.arfurnish.ui.screens.HomeScreen
import com.arfurnish.ui.screens.MultiPlaceARScreen
import com.arfurnish.ui.theme.ARFurnishTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ARFurnishTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(navController)
                        }
                        composable("ar/{model}") { backStackEntry ->
                            val model = backStackEntry.arguments?.getString("model") ?: "A"
                            ARScreen(navController, model)
                        }
                        composable("alphabet") {
                            AlphabetScreen(navController)
                        }
                        composable("deals") {
                            DealsScreen(navController)
                        }
                        composable("deals/ar/{bundleKeys}") { backStackEntry ->
                            val bundleKeysArg = backStackEntry.arguments?.getString("bundleKeys")
                            MultiPlaceARScreen(
                                navController = navController,
                                dealBundleKeys = bundleKeysArg?.let(Uri::decode)
                            )
                        }
                    }
                }
            }
        }
    }
}

