package com.example.examenn

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.media3.common.util.UnstableApi

@UnstableApi
class ReproductorWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, "Ninguna canción", false)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ServicioReproduccion.ACTION_WIDGET_UPDATE) {
            val tituloCancion = intent.getStringExtra("titulo_cancion") ?: "Ninguna canción"
            val isPlaying = intent.getBooleanExtra("is_playing", false)

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, ReproductorWidgetProvider::class.java))

            for (appWidgetId in ids) {
                updateAppWidget(context, appWidgetManager, appWidgetId, tituloCancion, isPlaying)
            }
        }
    }

    companion object {
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            tituloCancion: String,
            isPlaying: Boolean
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_reproductor)

            // PendingIntent para abrir la MainActivity al hacer clic en el widget
            val appIntent = Intent(context, MainActivity::class.java)
            val appPendingIntent = PendingIntent.getActivity(
                context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, appPendingIntent)

            views.setTextViewText(R.id.widget_titulo_cancion, tituloCancion)

            if (isPlaying) {
                views.setImageViewResource(R.id.widget_boton_play_pausa, android.R.drawable.ic_media_pause)
                val pauseIntent = Intent(context, ServicioReproduccion::class.java).apply {
                    action = ServicioReproduccion.ACTION_PAUSE
                }
                val pausePendingIntent = PendingIntent.getService(
                    context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_boton_play_pausa, pausePendingIntent)
            } else {
                views.setImageViewResource(R.id.widget_boton_play_pausa, android.R.drawable.ic_media_play)
                val playIntent = Intent(context, ServicioReproduccion::class.java).apply {
                    action = ServicioReproduccion.ACTION_PLAY
                }
                val playPendingIntent = PendingIntent.getService(
                    context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_boton_play_pausa, playPendingIntent)
            }

            val stopIntent = Intent(context, ServicioReproduccion::class.java).apply {
                action = ServicioReproduccion.ACTION_STOP
            }
            val stopPendingIntent = PendingIntent.getService(
                context, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_boton_stop, stopPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}