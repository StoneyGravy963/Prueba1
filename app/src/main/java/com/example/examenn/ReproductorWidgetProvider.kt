package com.example.examenn

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.media3.common.util.UnstableApi

/**
 * Implementation of App Widget functionality.
 */
@UnstableApi
class ReproductorWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Puede haber múltiples widgets activos, así que los actualizamos todos
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Solo creamos la vista y la mostramos, sin configurar botones por ahora.
            val views = RemoteViews(context.packageName, R.layout.widget_reproductor)


            // Configurar el PendingIntent para el botón Play/Pause
            val playPauseIntent = Intent(context, ServicioReproduccion::class.java).apply {
                action = ServicioReproduccion.ACTION_PLAY
            }
            val playPausePendingIntent = PendingIntent.getService(
                context, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_boton_play_pausa, playPausePendingIntent)

            // Configurar el PendingIntent para el botón Stop
            val stopIntent = Intent(context, ServicioReproduccion::class.java).apply {
                action = ServicioReproduccion.ACTION_STOP
            }
            val stopPendingIntent = PendingIntent.getService(
                context, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_boton_stop, stopPendingIntent)


            // Indicar al AppWidgetManager que actualice el widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
