package com.gorvi.gorviledger

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen()
        }
    }

    @Composable
    fun LoginScreen() {
        var password by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Column (horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
            TextField(
                value = password,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation()
            )
            Button(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        var loginRequest = LoginRequest(password = password)
                        Log.d(TAG, "Login Request $loginRequest")
                        val response = RetrofitService.api.login(loginRequest)
                        Log.d(TAG, "Login Response ${response}")

                        if (response.isSuccessful && response.body() != null) {
                            val accessToken = response.body()!!.access_token

                            val intent = Intent(this@MainActivity, BalanceActivity::class.java).apply {
                                putExtra("ACCESS_TOKEN", accessToken)
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            // Handle login failure
                            val error = "Error logging in. ${response.errorBody()?.string()}"
                            Log.i(TAG, error)
                            withContext(Dispatchers.Main) {
                                errorMessage = error
                            }
                        }
                    } catch (e: Exception) {
                        val error = "Error logging in. Exception: ${e.message}"
                        Log.e(TAG, error, e)
                        withContext(Dispatchers.Main) {
                            errorMessage = error
                        }
                    }
                }
            }) {
                Text("Submit")
            }

            errorMessage?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            AppVersionLabel(getAppVersion())
        }
    }

    @Composable
    fun AppVersionLabel(appVersion: String) {
        Text(text = "App Version: $appVersion")
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        LoginScreen()
    }

    private fun getAppVersion(): String {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }

}
