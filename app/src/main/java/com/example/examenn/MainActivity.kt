package com.example.examenn

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.examenn.databinding.ActivityMainBinding
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var listaCanciones: List<Cancion>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        crearListaCanciones()
        configurarRecyclerView()
        configurarListeners()
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
            // Ahora este botón puede usarse para reanudar la reproducción
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

    // Ya no necesitamos gestionar el ciclo de vida del reproductor aquí,
    // el Servicio se encarga de ello.
}
