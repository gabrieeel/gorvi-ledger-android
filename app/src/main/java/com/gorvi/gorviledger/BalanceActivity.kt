package com.gorvi.gorviledger

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat

class BalanceActivity : AppCompatActivity() {
    private val TAG = BalanceActivity::class.qualifiedName
    private var formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val accessToken = intent.getStringExtra("ACCESS_TOKEN") ?: return
        setContent {
            BalanceScreen(accessToken)
        }
    }

    @Composable
    fun BalanceScreen(accessToken: String) {
        var balances by remember { mutableStateOf<List<BalanceResponse>?>(null) }

        LaunchedEffect(Unit) {
            fetchBalances(accessToken) { fetchedBalances ->
                balances = fetchedBalances
            }
        }

        if (balances == null) {
            Text("Loading...")
        } else {
            BalanceList(balances!!)
        }
    }

    @Composable
    fun BalanceList(balances: List<BalanceResponse>) {
        Column {
            balances.forEach { balanceResponse ->
                Text(text = "ID: ${balanceResponse.id}, Name: ${balanceResponse.name}", fontWeight = FontWeight.ExtraBold)
                balanceResponse.balances.forEach { balance ->
                    Text("Balance: ${balance.balance}, Currency: ${balance.currency.code}")
                    Text("Updated: ${formatter.format(balance.updatedAt)}")
                }
            }
        }
    }

    private suspend fun fetchBalances(accessToken: String, onResult: (List<BalanceResponse>) -> Unit) {
        try {
            val response = RetrofitService.api.getBalances("Bearer $accessToken")
            if (response.isSuccessful && response.body() != null) {
                onResult(response.body()!!)
            } else {
                Log.e(TAG, "Error: ${response.errorBody()?.string()}")

            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
        }
    }

}
