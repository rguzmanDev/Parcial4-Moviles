package rguzman.parcial04

data class Tarea(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fechaInicio: Long? = null,
    val fechaFin: Long? = null,
    val prioridad: String = "",
    val estado: String = "",
    val userId: String = ""
)