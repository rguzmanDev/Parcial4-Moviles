package rguzman.parcial04

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEditTaskActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private var tareaId: String? = null
    private var tareaListener: ListenerRegistration? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_task)

        firestore = FirebaseFirestore.getInstance()

        val tituloEditText = findViewById<EditText>(R.id.titulo)
        val descripcionEditText = findViewById<EditText>(R.id.descripcion)
        val prioridadSpinner = findViewById<Spinner>(R.id.prioridad)
        val estadoSpinner = findViewById<Spinner>(R.id.estado)
        val fechaInicioEditText = findViewById<EditText>(R.id.fecha_inicio)
        val fechaFinEditText = findViewById<EditText>(R.id.fecha_fin)
        val saveButton = findViewById<Button>(R.id.save_button)
        val deleteButton = findViewById<Button>(R.id.delete_button)
        val cancelButton = findViewById<Button>(R.id.cancel_button)

        val prioridadAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.prioridad_array,
            android.R.layout.simple_spinner_item
        )
        prioridadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioridadSpinner.adapter = prioridadAdapter

        val estadoAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.estado_array,
            android.R.layout.simple_spinner_item
        )
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = estadoAdapter

        tareaId = intent.getStringExtra("TAREA_ID")

        if (tareaId != null) {
            loadTarea(tareaId!!)
        }

        saveButton.setOnClickListener {
            val titulo = tituloEditText.text.toString()
            val descripcion = descripcionEditText.text.toString()
            val prioridad = prioridadSpinner.selectedItem.toString()
            val estado = estadoSpinner.selectedItem.toString()
            val fechaInicio = fechaInicioEditText.text.toString().takeIf { it.isNotEmpty() }?.let { dateFormat.parse(it).time }
            val fechaFin = fechaFinEditText.text.toString().takeIf { it.isNotEmpty() }?.let { dateFormat.parse(it).time }
            val userId = getCurrentUserId()

            if (fechaFin != null && fechaInicio != null && fechaFin < fechaInicio) {
                Toast.makeText(this, "La fecha de finalización no puede ser menor que la fecha de inicio.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tarea = Tarea(
                id = tareaId ?: firestore.collection("tareas").document().id,
                titulo = titulo,
                descripcion = descripcion,
                fechaInicio = fechaInicio,
                fechaFin = fechaFin,
                prioridad = prioridad,
                estado = estado,
                userId = userId
            )

            saveTarea(tarea)
        }

        deleteButton.setOnClickListener {
            if (tareaId != null) {
                deleteTarea(tareaId!!)
            }
        }

        cancelButton.setOnClickListener {
            finish()
        }

        fechaInicioEditText.setOnClickListener {
            showDatePickerDialog(fechaInicioEditText)
        }

        fechaFinEditText.setOnClickListener {
            showDatePickerDialog(fechaFinEditText)
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                editText.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun loadTarea(tareaId: String) {
        tareaListener = firestore.collection("tareas").document(tareaId)
            .addSnapshotListener { document, _ ->
                if (document != null && document.exists()) {
                    val tarea = document.toObject(Tarea::class.java)
                    if (tarea != null) {
                        findViewById<EditText>(R.id.titulo).setText(tarea.titulo)
                        findViewById<EditText>(R.id.descripcion).setText(tarea.descripcion)
                        findViewById<EditText>(R.id.fecha_inicio).setText(tarea.fechaInicio?.let { dateFormat.format(it) } ?: "")
                        findViewById<EditText>(R.id.fecha_fin).setText(tarea.fechaFin?.let { dateFormat.format(it) } ?: "")
                        // Set the spinner values accordingly
                    }
                }
            }
    }

    private fun saveTarea(tarea: Tarea) {
        if (tareaId == null) {
            firestore.collection("tareas")
                .add(tarea)
                .addOnSuccessListener {
                    Toast.makeText(this, "Su tarea ha sido creada.", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK, Intent().putExtra("ACTION", "ADD"))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar tarea.", Toast.LENGTH_SHORT).show()
                }
        } else {
            firestore.collection("tareas").document(tarea.id)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        firestore.collection("tareas").document(tarea.id)
                            .set(tarea)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Su Tarea ha sido modificada.", Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK, Intent().putExtra("ACTION", "EDIT"))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al actualizar tarea.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        firestore.collection("tareas").add(tarea)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Su tarea ha sido creada.", Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK, Intent().putExtra("ACTION", "ADD"))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al guardar tarea.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al verificar tarea.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteTarea(tareaId: String) {
        firestore.collection("tareas").document(tareaId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "La tarea ha sido eliminada.", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK, Intent().putExtra("ACTION", "DELETE"))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar tarea.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    override fun onDestroy() {
        super.onDestroy()
        tareaListener?.remove()
    }
}