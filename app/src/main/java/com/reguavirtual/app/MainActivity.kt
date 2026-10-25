package com.reguavirtual.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(30), dp(24), dp(24))
            setBackgroundColor(Color.rgb(248, 246, 251))
        }
        root.addView(TextView(this).apply {
            text = "Régua Virtual"; textSize = 28f; setTextColor(Color.rgb(35, 30, 45));
        }, LinearLayout.LayoutParams(-1, -2))
        root.addView(TextView(this).apply {
            text = "Linhas de referência sobre qualquer aplicativo. Ative a sobreposição para começar.";
            textSize = 16f; setTextColor(Color.DKGRAY); setPadding(0, dp(12), 0, dp(24))
        })
        val start = Button(this).apply { text = "Ativar régua virtual"; isAllCaps = false }
        root.addView(start, LinearLayout.LayoutParams(-1, -2))
        root.addView(TextView(this).apply {
            text = "Como usar\n1. Ative a permissão de sobreposição.\n2. Use o botão flutuante para abrir os controles.\n3. Toque em + para criar uma linha.\n4. Arraste a linha para posicioná-la; arraste as alças das pontas para ajustar o comprimento.\n5. Escolha a cor e a espessura no painel.";
            textSize = 15f; setTextColor(Color.DKGRAY); setPadding(0, dp(24), 0, 0)
        })
        setContentView(root)
        start.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else {
                startService(Intent(this, OverlayService::class.java))
                finish()
            }
        }
        if (Settings.canDrawOverlays(this)) start.text = "Iniciar sobreposição"
    }
    override fun onResume() {
        super.onResume()
    }
}
