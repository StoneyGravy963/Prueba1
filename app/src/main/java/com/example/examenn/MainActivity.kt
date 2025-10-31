package com.example.examenn

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.examenn.databinding.ActivityMainBinding
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager

@UnstableApi
class MainActivity : AppCompatActivity(), CancionesAdapter.OnItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private var reproductor: ExoPlayer? = null
    private lateinit var listaCanciones: List<Cancion>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar la lista de canciones
        crearListaCanciones()

        // Configurar RecyclerView ya aquí (para que el adapter reciba 'this' sin error)
        configurarRecyclerView()
    }

    private fun crearListaCanciones() {
        listaCanciones = listOf(
            Cancion("Canción 1", "03:30", R.raw.cancion1, R.drawable.ic_launcher_foreground),
            Cancion("Canción 2", "04:15", R.raw.cancion2, R.drawable.ic_launcher_foreground),
            Cancion("Canción 3", "02:50", R.raw.cancion3, R.drawable.ic_launcher_foreground)
        )
    }

    private fun inicializarReproductor() {
        if (reproductor != null) return

        reproductor = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.vistaReproductor.player = exoPlayer
            // No cargamos un MediaItem por defecto aquí; se cargará cuando el usuario pulse una canción.
        }
        configurarListeners()
    }

    private fun configurarListeners() {
        binding.botonReproducir.setOnClickListener {
            reproductor?.play()
        }

        binding.botonPausar.setOnClickListener {
            reproductor?.pause()
        }

        binding.botonDetener.setOnClickListener {
            reproductor?.stop()
            reproductor?.seekTo(0)
        }
    }

    private fun configurarRecyclerView() {
        binding.recyclerViewCanciones.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCanciones.adapter = CancionesAdapter(listaCanciones, this)
    }

    private fun liberarReproductor() {
        reproductor?.release()
        reproductor = null
        binding.vistaReproductor.player = null
    }

    // Gestionar el ciclo de vida del reproductor
    public override fun onStart() {
        super.onStart()
        inicializarReproductor()
    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onPause() {
        super.onPause()
        reproductor?.pause()
    }

    public override fun onStop() {
        super.onStop()
        liberarReproductor()
    }

    // Implementación de la interfaz del adaptador: se llama cuando el usuario pulsa una canción
    override fun onItemClick(cancion: Cancion) {
        // Asegurarse de que el reproductor está inicializado
        if (reproductor == null) {
            inicializarReproductor()
        }

        val uriCancion = Uri.parse("android.resource://${packageName}/${cancion.recursoId}")
        val itemMedio = MediaItem.fromUri(uriCancion)

        reproductor?.let { exo ->
            exo.setMediaItem(itemMedio)
            exo.prepare()
            exo.play()
        }
    }
}