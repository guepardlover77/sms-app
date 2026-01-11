package com.music.sms

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.music.sms.data.model.Contact
import com.music.sms.ui.navigation.Screen
import com.music.sms.ui.screens.chat.ChatScreen
import com.music.sms.ui.screens.conversations.ConversationListScreen
import com.music.sms.ui.screens.newmessage.NewMessageScreen
import com.music.sms.ui.theme.PastelSmsTheme
import java.net.URLDecoder

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PastelSmsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }

    @Composable
    private fun MainApp() {
        var hasPermissions by remember { mutableStateOf(checkPermissions()) }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            hasPermissions = permissions.values.all { it }
        }

        LaunchedEffect(Unit) {
            if (!hasPermissions) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_CONTACTS
                    )
                )
            }
        }

        if (hasPermissions) {
            MainNavigation()
        } else {
            PermissionRequest(
                onRequestPermission = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_SMS,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.READ_CONTACTS
                        )
                    )
                }
            )
        }
    }

    @Composable
    private fun MainNavigation() {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = Screen.ConversationList.route
        ) {
            composable(Screen.ConversationList.route) {
                ConversationListScreen(
                    onConversationClick = { conversation ->
                        navController.navigate(
                            Screen.Chat.createRoute(
                                threadId = conversation.threadId,
                                address = conversation.address,
                                displayName = conversation.displayName
                            )
                        )
                    },
                    onNewMessageClick = {
                        navController.navigate(Screen.NewMessage.route)
                    }
                )
            }

            composable(
                route = Screen.Chat.route,
                arguments = listOf(
                    navArgument("threadId") { type = NavType.LongType },
                    navArgument("address") { type = NavType.StringType },
                    navArgument("displayName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val threadId = backStackEntry.arguments?.getLong("threadId") ?: -1
                val address = try {
                    URLDecoder.decode(
                        backStackEntry.arguments?.getString("address") ?: "",
                        "UTF-8"
                    )
                } catch (e: Exception) {
                    backStackEntry.arguments?.getString("address") ?: ""
                }
                val displayName = try {
                    URLDecoder.decode(
                        backStackEntry.arguments?.getString("displayName") ?: address,
                        "UTF-8"
                    )
                } catch (e: Exception) {
                    address
                }

                ChatScreen(
                    threadId = threadId,
                    address = address,
                    displayName = displayName,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.NewMessage.route) {
                NewMessageScreen(
                    contacts = emptyList(),
                    onBackClick = { navController.popBackStack() },
                    onContactClick = { contact ->
                        navController.navigate(
                            Screen.Chat.createRoute(
                                threadId = -1,
                                address = contact.phoneNumber,
                                displayName = contact.name
                            )
                        ) {
                            popUpTo(Screen.NewMessage.route) { inclusive = true }
                        }
                    },
                    onPhoneNumberEntered = { phoneNumber ->
                        navController.navigate(
                            Screen.Chat.createRoute(
                                threadId = -1,
                                address = phoneNumber,
                                displayName = phoneNumber
                            )
                        ) {
                            popUpTo(Screen.NewMessage.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return listOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS
        ).all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}

@Composable
private fun PermissionRequest(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Permissions requises",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cette application a besoin d'accéder à vos SMS et contacts pour fonctionner.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRequestPermission) {
                Text("Autoriser")
            }
        }
    }
}
