package com.example.examenn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.util.UnstableApi

@UnstableApi
class ServicioReproduccion : Service() {

    private var reproductor: ExoPlayer? = null

    companion object {
        const val ACTION_PLAY = "com.example.examenn.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.examenn.ACTION_PAUSE"
        const val ACTION_STOP = "com.example.examenn.ACTION_STOP"
        const val EXTRA_CANCION_URI = "com.example.examenn.EXTRA_CANCION_URI"
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "ReproduccionChannel"
    }

    override fun onCreate() {
        super.onCreate()
        // Crear el reproductor
        reproductor = ExoPlayer.Builder(this).build()
        crearCanalNotificacion()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_PLAY -> {
                val cancionUriString = intent.getStringExtra(EXTRA_CANCION_URI)
                if (cancionUriString != null) {
                    val cancionUri = Uri.parse(cancionUriString)
                    val itemMedio = MediaItem.fromUri(cancionUri)
                    reproductor?.setMediaItem(itemMedio)
                    reproductor?.prepare()
                }
                reproductor?.play()
                startForeground(NOTIFICATION_ID, crearNotificacion("Reproduciendo..."))
            }
            ACTION_PAUSE -> {
                reproductor?.pause()
                // Opcional: actualizar la notificación para mostrar el estado de pausa
            }
            ACTION_STOP -> {
                reproductor?.stop()
                stopForeground(true)
                stopSelf() // Detener el servicio
            }
        }

        return START_NOT_STICKY // El servicio no se reiniciará automáticamente
    }

    private fun crearNotificacion(contentText: String): Notification {
        // Aquí se podría añadir más información, como el título de la canción
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Mi Reproductor")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Usar un ícono adecuado
            .build()
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Canal de Reproducción",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        reproductor?.release()
        reproductor = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // No usamos binding por ahora
    }
}
