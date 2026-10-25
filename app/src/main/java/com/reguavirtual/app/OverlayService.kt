package com.reguavirtual.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.ScrollView

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var bubble: TextView? = null
    private var panel: View? = null
    private var panelParams: WindowManager.LayoutParams? = null
    private val lines = mutableListOf<LineView>()
    private var selected: LineView? = null
    private var colorIndex = 0
    private var thickness = 3
    private var addBeforeClear = false
    private val colors = intArrayOf(Color.RED, Color.YELLOW, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.WHITE, Color.BLUE, Color.BLACK)
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotification()
        showBubble()
        addLine()
    }
    private fun createNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel("regua_virtual", "Régua Virtual", NotificationManager.IMPORTANCE_LOW))
        val notification = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, "regua_virtual") else Notification.Builder(this)
        startForeground(1701, notification.setContentTitle("Régua Virtual ativa").setContentText("Use o botão flutuante para controlar as linhas.").setSmallIcon(android.R.drawable.ic_menu_crop).build())
    }
    private fun params(w: Int, h: Int, gravity: Int = Gravity.TOP or Gravity.START): WindowManager.LayoutParams =
        WindowManager.LayoutParams(w, h, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT).apply { this.gravity = gravity }

    private fun showBubble() {
        val b = TextView(this).apply {
            text = "≡"; textSize = 25f; gravity = Gravity.CENTER; setTextColor(Color.WHITE)
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.rgb(75, 54, 120)); setStroke(dp(1), Color.WHITE) }
            elevation = dp(8).toFloat()
        }
        val p = params(dp(54), dp(54)).apply { x = dp(12); y = dp(180) }
        b.setOnTouchListener(object : View.OnTouchListener {
            var sx = 0f; var sy = 0f; var ox = 0; var oy = 0; var moved = false
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> { sx=e.rawX; sy=e.rawY; ox=p.x; oy=p.y; moved=false; return true }
                    MotionEvent.ACTION_MOVE -> { val dx=(e.rawX-sx).toInt(); val dy=(e.rawY-sy).toInt(); if (kotlin.math.abs(dx)+kotlin.math.abs(dy)>dp(5)) moved=true; p.x=ox+dx; p.y=oy+dy; wm.updateViewLayout(b,p); return true }
                    MotionEvent.ACTION_UP -> { if (!moved) togglePanel(); return true }
                }; return true
            }
        })
        wm.addView(b, p); bubble = b
    }
    private fun togglePanel() { if (panel == null) showPanel() else hidePanel() }
    private fun showPanel() {
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), dp(10), dp(12), dp(10)); background = GradientDrawable().apply { setColor(Color.argb(245, 250, 248, 253)); cornerRadius = dp(16).toFloat(); setStroke(dp(1), Color.LTGRAY) }; elevation = dp(12).toFloat() }
        content.addView(TextView(this).apply { text="Régua Virtual"; textSize=19f; setTextColor(Color.rgb(40,30,55)); setPadding(0,0,0,dp(6)) })
        val row1 = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        row1.addView(btn("+ Linha") { addLine() }, weight())
        row1.addView(btn("Excluir") { selected?.let { removeLine(it) } }, weight())
        row1.addView(btn("Limpar") { lines.toList().forEach { removeLine(it) } }, weight())
        content.addView(row1)
        content.addView(TextView(this).apply { text="Cor da linha selecionada"; setTextColor(Color.DKGRAY); setPadding(0,dp(8),0,0) })
        val row2 = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        row2.addView(btn("Cor anterior") { colorIndex=(colorIndex+colors.size-1)%colors.size; selected?.setColor(colors[colorIndex]) }, weight())
        row2.addView(btn("Próxima cor") { colorIndex=(colorIndex+1)%colors.size; selected?.setColor(colors[colorIndex]) }, weight())
        content.addView(row2)
        content.addView(TextView(this).apply { text="Espessura"; setTextColor(Color.DKGRAY); setPadding(0,dp(8),0,0) })
        val seek = SeekBar(this).apply { max=19; progress=thickness-1 }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, progress: Int, fromUser: Boolean) { thickness=progress+1; selected?.setThickness(thickness) }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        content.addView(seek)
        content.addView(btn(if(addBeforeClear) "Modo: manter linhas ao adicionar" else "Modo: limpar antes de adicionar") {
            addBeforeClear = !addBeforeClear
            // The selected mode controls whether adding a line preserves existing guides.
            hidePanel(); showPanel()
        })
        content.addView(btn("Fechar painel") { hidePanel() })
        val scroll = ScrollView(this).apply { addView(content) }
        val p = params(dp(290), -2).apply { x=dp(12); y=dp(90) }
        wm.addView(scroll, p); panel=scroll; panelParams=p
    }
    private fun weight() = LinearLayout.LayoutParams(0, dp(44), 1f)
    private fun btn(label: String, action: () -> Unit) = Button(this).apply {
        text=label; isAllCaps=false; textSize=12f; setPadding(dp(3),0,dp(3),0)
        layoutParams=LinearLayout.LayoutParams(-1, dp(44)).apply { setMargins(dp(2),dp(2),dp(2),dp(2)) }
        setOnClickListener { action() }
    }
    private fun hidePanel() { panel?.let { try { wm.removeView(it) } catch (_: Exception) {} }; panel=null; panelParams=null }

    private fun addLine() {
        if (!addBeforeClear && lines.isNotEmpty()) {
            // Optional replacement behavior: the toggle switches between preserving and replacing.
            lines.toList().forEach { removeLine(it) }
        }
        val line = LineView()
        val p = params(-1, dp(48)).apply { x=0; y=dp(260 + lines.size*54); flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL }
        line.layoutParamsOverlay=p
        wm.addView(line, p); lines.add(line); selectLine(line)
    }
    private fun selectLine(line: LineView) { selected?.setSelected(false); selected=line; line.setSelected(true) }
    private fun removeLine(line: LineView) {
        try { wm.removeView(line) } catch (_: Exception) {}
        lines.remove(line)
        if (selected === line) { selected=lines.lastOrNull(); selected?.setSelected(true) }
    }

    private inner class LineView : View(this@OverlayService) {
        var layoutParamsOverlay: WindowManager.LayoutParams? = null
        private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        private var lineColor = colors[colorIndex]
        private var lineThickness = thickness.toFloat()
        private var isSelected = false
        private val handle = dp(12).toFloat()
        private var mode = 0 // 0 move line, 1 resize left, 2 resize right
        private var downX=0f; private var startX=0f; private var startWidth=0
        private var lineWidth=resources.displayMetrics.widthPixels.toFloat()
        private var lineLeft=0f
        init { setLayerType(View.LAYER_TYPE_SOFTWARE, null); setBackgroundColor(Color.TRANSPARENT) }
        fun setColor(c: Int) { lineColor=c; invalidate() }
        fun setThickness(t: Int) { lineThickness=t.toFloat(); invalidate() }
        fun setSelected(s: Boolean) { isSelected=s; invalidate() }
        override fun onDraw(canvas: android.graphics.Canvas) {
            super.onDraw(canvas)
            val cy=height/2f
            paint.color=lineColor; paint.strokeWidth=lineThickness; paint.strokeCap=android.graphics.Paint.Cap.ROUND
            canvas.drawLine(lineLeft, cy, lineLeft+lineWidth, cy, paint)
            if (isSelected) {
                paint.color=Color.WHITE; paint.style=android.graphics.Paint.Style.FILL
                canvas.drawCircle(lineLeft,cy,handle,paint); canvas.drawCircle(lineLeft+lineWidth,cy,handle,paint)
                paint.color=Color.BLACK; paint.style=android.graphics.Paint.Style.STROKE; paint.strokeWidth=dp(1).toFloat()
                canvas.drawCircle(lineLeft,cy,handle,paint); canvas.drawCircle(lineLeft+lineWidth,cy,handle,paint)
                paint.style=android.graphics.Paint.Style.FILL
            }
        }
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val p=layoutParamsOverlay ?: return false
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    selectLine(this); downX=event.rawX; startX=event.rawX; startWidth=lineWidth.toInt()
                    val local=event.x
                    mode=when { isSelected && kotlin.math.abs(local-lineLeft)<handle*2 -> 1; isSelected && kotlin.math.abs(local-(lineLeft+lineWidth))<handle*2 -> 2; else -> 0 }
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx=event.rawX-startX
                    when(mode) {
                        0 -> { p.x=(p.x+dx).toInt().coerceIn(-resources.displayMetrics.widthPixels, resources.displayMetrics.widthPixels); startX=event.rawX }
                        1 -> { val next=(startWidth-dx).coerceAtLeast(dp(30)); lineLeft=(lineLeft+dx).coerceAtLeast(0f); lineWidth=next.toFloat(); startWidth=next; startX=event.rawX }
                        2 -> { lineWidth=(startWidth+dx).coerceAtLeast(dp(30)).toFloat(); startWidth=lineWidth.toInt(); startX=event.rawX }
                    }
                    try { wm.updateViewLayout(this,p) } catch (_: Exception) {}; invalidate(); return true
                }
                MotionEvent.ACTION_UP -> { return true }
            }
            return true
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onDestroy() {
        hidePanel(); lines.toList().forEach { removeLine(it) }
        bubble?.let { try { wm.removeView(it) } catch (_: Exception) {} }; bubble=null
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
