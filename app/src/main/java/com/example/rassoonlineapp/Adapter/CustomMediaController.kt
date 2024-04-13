package com.example.rassoonlineapp.Adapter
import android.content.Context
import android.widget.MediaController

class CustomMediaController(context: Context) : MediaController(context) {

    override fun hide() {
        // Não ocultar os controles
    }
}
