package com.example.examenn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CancionesAdapter(
    private val listaCanciones: List<Cancion>,
    private val listener: OnItemClickListener // Listener para los clics
) : RecyclerView.Adapter<CancionesAdapter.CancionViewHolder>() {

    // Interfaz para manejar los clics en los elementos
    interface OnItemClickListener {
        fun onItemClick(cancion: Cancion)
    }

    // ViewHolder para cada elemento de la lista
    inner class CancionViewHolder(vistaItem: View) : RecyclerView.ViewHolder(vistaItem), View.OnClickListener {
        val imagenIcono: ImageView = vistaItem.findViewById(R.id.imagen_icono_cancion)
        val textoNombre: TextView = vistaItem.findViewById(R.id.texto_nombre_cancion)
        val textoDuracion: TextView = vistaItem.findViewById(R.id.texto_duracion_cancion)

        init {
            vistaItem.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(listaCanciones[position])
            }
        }
    }

    // Infla el layout de cada elemento de la lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CancionViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cancion, parent, false)
        return CancionViewHolder(vista)
    }

    // Asigna los datos de una canción a los elementos de la vista
    override fun onBindViewHolder(holder: CancionViewHolder, position: Int) {
        val cancionActual = listaCanciones[position]
        holder.imagenIcono.setImageResource(cancionActual.iconoId)
        holder.textoNombre.text = cancionActual.nombre
        holder.textoDuracion.text = cancionActual.duracion
    }

    // Devuelve el número total de elementos en la lista
    override fun getItemCount(): Int {
        return listaCanciones.size
    }
}