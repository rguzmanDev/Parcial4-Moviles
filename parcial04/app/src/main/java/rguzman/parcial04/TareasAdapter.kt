package rguzman.parcial04

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

// Importar la clase Tarea
import rguzman.parcial04.Tarea

class TareasAdapter(private val clickListener: (Tarea) -> Unit) : ListAdapter<Tarea, TareasAdapter.TareaViewHolder>(TareaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = getItem(position)
        holder.bind(tarea, clickListener)
    }

    class TareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tituloTextView: TextView = itemView.findViewById(R.id.task_title)
        private val descripcionTextView: TextView = itemView.findViewById(R.id.task_description)
        private val prioridadTextView: TextView = itemView.findViewById(R.id.task_priority)
        private val estadoTextView: TextView = itemView.findViewById(R.id.task_status)

        fun bind(tarea: Tarea, clickListener: (Tarea) -> Unit) {
            tituloTextView.text = tarea.titulo
            descripcionTextView.text = tarea.descripcion
            prioridadTextView.text = "Prioridad: ${tarea.prioridad}"
            estadoTextView.text = "Estado: ${tarea.estado}"
            itemView.setOnClickListener { clickListener(tarea) }
        }
    }

    class TareaDiffCallback : DiffUtil.ItemCallback<Tarea>() {
        override fun areItemsTheSame(oldItem: Tarea, newItem: Tarea): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tarea, newItem: Tarea): Boolean {
            return oldItem == newItem
        }
    }
}