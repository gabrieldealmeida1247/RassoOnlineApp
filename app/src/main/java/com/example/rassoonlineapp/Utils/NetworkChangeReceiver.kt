package com.example.rassoonlineapp.Utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.widget.Toast

class NetworkChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (isOnline(context)) {
            // Conexão de rede disponível
            Toast.makeText(context, "Conexão estabelecida", Toast.LENGTH_SHORT).show()
        } else {
            // Conexão de rede indisponível
            Toast.makeText(context, "Sem conexão de rede", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isOnline(context: Context?): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
