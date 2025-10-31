package com.example.examenn

data class Cancion(
    val nombre: String,
    val duracion: String, // Representación de la duración como String
    val recursoId: Int,   // ID del recurso raw (ej. R.raw.cancion1)
    val iconoId: Int      // ID del recurso drawable para el ícono (ej. R.drawable.ic_launcher_foreground)
)