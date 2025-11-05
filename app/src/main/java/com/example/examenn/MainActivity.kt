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

    private var servicioReproduccion: ServicioReproduccion? = null
    private var vinculado = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ServicioReproduccion.ReproductorBinder
            servicioReproduccion = binder.getServicio()
            vinculado = true

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
            Cancion("Feel it", "01:52", R.raw.cancion1, R.drawable.icono1),
            Cancion("Rethink", "02:37", R.raw.cancion2, R.drawable.icono2),
            Cancion("Make it Louder", "01:51", R.raw.cancion3, R.drawable.icono3)
        )
    }

    private fun configurarRecyclerView() {
        val adapter = CancionesAdapter(listaCanciones, object : CancionesAdapter.OnItemClickListener {
            override fun onItemClick(cancion: Cancion) {
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