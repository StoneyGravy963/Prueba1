package com.example.examenn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.util.UnstableApi

@UnstableApi
class ServicioReproduccion : Service() {

    private var reproductor: ExoPlayer? = null
    private val binder = ReproductorBinder()
    private var titulosPlaylist: List<String> = emptyList()
    private var tituloCancionActual: String? = "Ninguna canción"

    // Listener para detectar cambios en el reproductor
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            enviarBroadcastDeActualizacion()
            if (isPlaying) {
                startForeground(NOTIFICATION_ID, crearNotificacion(tituloCancionActual ?: "Reproduciendo..."))
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    stopForeground(false)
                }
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            // Cuando la canción cambia, actualizamos el título actual
            val newIndex = reproductor?.currentMediaItemIndex ?: -1
            if (newIndex != -1 && newIndex < titulosPlaylist.size) {
                tituloCancionActual = titulosPlaylist[newIndex]
            }
            enviarBroadcastDeActualizacion()
            // Y actualizamos la notificación con el nuevo título
            startForeground(NOTIFICATION_ID, crearNotificacion(tituloCancionActual ?: "Reproduciendo..."))
        }
    }

    fun getReproductor(): ExoPlayer? {
        return reproductor
    }

    inner class ReproductorBinder : Binder() {
        fun getServicio(): ServicioReproduccion = this@ServicioReproduccion
    }

    companion object {
        const val ACTION_PLAY_PLAYLIST = "com.example.examenn.ACTION_PLAY_PLAYLIST" // Nueva acción
        const val ACTION_PLAY = "com.example.examenn.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.examenn.ACTION_PAUSE"
        const val ACTION_STOP = "com.example.examenn.ACTION_STOP"
        const val ACTION_WIDGET_UPDATE = "com.example.examenn.ACTION_WIDGET_UPDATE"
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "ReproduccionChannel"
    }

    override fun onCreate() {
        super.onCreate()
        reproductor = ExoPlayer.Builder(this).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL // Repetir la playlist
        }
        reproductor?.addListener(playerListener)
        crearCanalNotificacion()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PLAYLIST -> {
                val uris = intent.getStringArrayListExtra("uris_canciones")
                val titulos = intent.getStringArrayListExtra("titulos_canciones")
                val startIndex = intent.getIntExtra("start_index", 0)

                if (uris != null && titulos != null) {
                    this.titulosPlaylist = titulos
                    val mediaItems = uris.map { MediaItem.fromUri(Uri.parse(it)) }

                    reproductor?.setMediaItems(mediaItems, startIndex, 0)
                    reproductor?.prepare()
                    reproductor?.play()

                    // Actualizamos el título inicial
                    tituloCancionActual = titulos[startIndex]
                }
            }
            ACTION_PLAY -> {
                // Si ya hay una playlist cargada, simplemente reanuda la reproducción
                // Si no hay nada cargado, inicia la primera canción por defecto.
                if (reproductor?.currentMediaItem == null) {
                    // Cargar la primera canción por defecto si no hay nada en la playlist
                    // Esto asume que R.raw.cancion1 existe y es la primera canción.
                    val defaultUri = "android.resource://${packageName}/${R.raw.cancion1}"
                    val defaultTitle = "Feel it" // Asegurarse de que coincide con MainActivity
                    val mediaItem = MediaItem.fromUri(Uri.parse(defaultUri))

                    this.titulosPlaylist = listOf(defaultTitle) // Carga una playlist mínima para el listener
                    tituloCancionActual = defaultTitle

                    reproductor?.setMediaItem(mediaItem)
                    reproductor?.prepare()
                }
                reproductor?.play()
            }
            ACTION_PAUSE -> {
                reproductor?.pause()
            }
            ACTION_STOP -> {
                reproductor?.pause()
                reproductor?.seekTo(0)
                // Mantenemos el servicio vivo para recordar la playlist,
                // pero permitimos que la notificación se descarte si el usuario la desliza.
                stopForeground(false)
            }
        }
        enviarBroadcastDeActualizacion()
        return START_NOT_STICKY
    }

    private fun enviarBroadcastDeActualizacion() {
        val intent = Intent(this, ReproductorWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_UPDATE
            putExtra("titulo_cancion", tituloCancionActual)
            putExtra("is_playing", reproductor?.isPlaying ?: false)
        }
        sendBroadcast(intent)
    }

    private fun crearNotificacion(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Mi Reproductor")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent) // Añadimos el PendingIntent para abrir MainActivity
            .build()
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Canal de Reproducción",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        reproductor?.removeListener(playerListener)
        reproductor?.release()
        reproductor = null
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}
