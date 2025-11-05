package com.example.examenn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CancionesAdapter(
    private val listaCanciones: List<Cancion>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<CancionesAdapter.CancionViewHolder>() {


    interface OnItemClickListener {
        fun onItemClick(cancion: Cancion)
    }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CancionViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cancion, parent, false)
        return CancionViewHolder(vista)
    }

    override fun onBindViewHolder(holder: CancionViewHolder, position: Int) {
        val cancionActual = listaCanciones[position]
        holder.imagenIcono.setImageResource(cancionActual.iconoId)
        holder.textoNombre.text = cancionActual.nombre
        holder.textoDuracion.text = cancionActual.duracion
    }

    override fun getItemCount(): Int {
        return listaCanciones.size
    }
}