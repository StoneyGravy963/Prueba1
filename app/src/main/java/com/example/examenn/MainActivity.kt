package com.example.examenn

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.example.examenn.databinding.ActivityMainBinding
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var listaCanciones: List<Cancion>

    // Propiedades para la vinculación con el servicio
    private var servicioReproduccion: ServicioReproduccion? = null
    private var vinculado = false

    /** Define los callbacks para la vinculación con el servicio */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ServicioReproduccion.ReproductorBinder
            servicioReproduccion = binder.getServicio()
            vinculado = true

            // Conectar el reproductor del servicio a nuestra PlayerView
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
        configurarListeners()
    }

    override fun onStart() {
        super.onStart()
        // Vincular al ServicioReproduccion
        Intent(this, ServicioReproduccion::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        // Desvincular del servicio
        if (vinculado) {
            unbindService(connection)
            vinculado = false
        }
    }

    private fun crearListaCanciones() {
        listaCanciones = listOf(
            Cancion("Canción 1", "03:30", R.raw.cancion1, R.drawable.ic_launcher_foreground),
            Cancion("Canción 2", "04:15", R.raw.cancion2, R.drawable.ic_launcher_foreground),
            Cancion("Canción 3", "02:50", R.raw.cancion3, R.drawable.ic_launcher_foreground)
        )
    }

    private fun configurarListeners() {
        binding.botonReproducir.setOnClickListener {
            val intent = Intent(this, ServicioReproduccion::class.java).apply {
                action = ServicioReproduccion.ACTION_PLAY
            }
            startService(intent)
        }

        binding.botonPausar.setOnClickListener {
            val intent = Intent(this, ServicioReproduccion::class.java).apply {
                action = ServicioReproduccion.ACTION_PAUSE
            }
            startService(intent)
        }

        binding.botonDetener.setOnClickListener {
            val intent = Intent(this, ServicioReproduccion::class.java).apply {
                action = ServicioReproduccion.ACTION_STOP
            }
            startService(intent)
        }
    }

    private fun configurarRecyclerView() {
        val adapter = CancionesAdapter(listaCanciones, object : CancionesAdapter.OnItemClickListener {
            override fun onItemClick(cancion: Cancion) {
                val uriCancion = "android.resource://$packageName/${cancion.recursoId}"
                val intent = Intent(this@MainActivity, ServicioReproduccion::class.java).apply {
                    action = ServicioReproduccion.ACTION_PLAY
                    putExtra(ServicioReproduccion.EXTRA_CANCION_URI, uriCancion)
                }
                startService(intent)
            }
        })
        binding.recyclerViewCanciones.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCanciones.adapter = adapter
    }
}