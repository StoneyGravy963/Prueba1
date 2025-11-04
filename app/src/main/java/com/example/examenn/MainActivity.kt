package com.example.examenn

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.examenn.databinding.ActivityMainBinding

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var listaCanciones: List<Cancion>

    // Volvemos a añadir las propiedades para la vinculación con el servicio
    private var servicioReproduccion: ServicioReproduccion? = null
    private var vinculado = false

    /** Volvemos a definir los callbacks para la vinculación con el servicio */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ServicioReproduccion.ReproductorBinder
            servicioReproduccion = binder.getServicio()
            vinculado = true

            // Conectamos el reproductor del servicio a nuestra PlayerView en MainActivity
            binding.vistaReproductor.player = servicioReproduccion?.getReproductor()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            vinculado = false
            servicioReproduccion = null
            binding.vistaReproductor.player = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        crearListaCanciones()
        configurarRecyclerView()

        binding.botonDetenerMainActivity.setOnClickListener {
            val intent = Intent(this, ServicioReproduccion::class.java).apply {
                action = ServicioReproduccion.ACTION_STOP
            }
            startService(intent)
        }
    }

    // Volvemos a añadir onStart y onStop para gestionar la vinculación
    override fun onStart() {
        super.onStart()
        Intent(this, ServicioReproduccion::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (vinculado) {
            unbindService(connection)
            vinculado = false
        }
    }

    private fun crearListaCanciones() {
        listaCanciones = listOf(
            Cancion("Canción 1", "03:30", R.raw.cancion1, R.drawable.icono_cancion),
            Cancion("Canción 2", "04:15", R.raw.cancion2, R.drawable.icono_cancion),
            Cancion("Canción 3", "02:50", R.raw.cancion3, R.drawable.icono_cancion)
        )
    }

    private fun configurarRecyclerView() {
        val adapter = CancionesAdapter(listaCanciones, object : CancionesAdapter.OnItemClickListener {
            override fun onItemClick(cancion: Cancion) {
                // Al hacer clic, NO abrimos una nueva actividad.
                // Enviamos la orden de reproducir la playlist al servicio.
                val uris = ArrayList(listaCanciones.map { "android.resource://$packageName/${it.recursoId}" })
                val titulos = ArrayList(listaCanciones.map { it.nombre })
                val startIndex = listaCanciones.indexOf(cancion)

                val serviceIntent = Intent(this@MainActivity, ServicioReproduccion::class.java).apply {
                    action = ServicioReproduccion.ACTION_PLAY_PLAYLIST
                    putStringArrayListExtra("uris_canciones", uris)
                    putStringArrayListExtra("titulos_canciones", titulos)
                    putExtra("start_index", startIndex)
                }
                startService(serviceIntent)
            }
        })
        binding.recyclerViewCanciones.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCanciones.adapter = adapter
    }
}