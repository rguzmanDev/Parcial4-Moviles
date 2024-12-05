package rguzman.parcial04

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: TareasAdapter
    private var tareasListener: ListenerRegistration? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        adapter = TareasAdapter { tarea: Tarea ->
            val intent = Intent(this, AddEditTaskActivity::class.java)
            intent.putExtra("TAREA_ID", tarea.id)
            startActivityForResult(intent, REQUEST_CODE_EDIT_TASK)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val orderSpinner = findViewById<Spinner>(R.id.order_spinner)
        val orderAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.order_options,
            android.R.layout.simple_spinner_item
        )
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        orderSpinner.adapter = orderAdapter

        orderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val orderBy = when (position) {
                    0 -> "titulo"
                    1 -> "prioridad"
                    2 -> "estado"
                    else -> "titulo"
                }
                loadTareas(orderBy)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                adapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        findViewById<Button>(R.id.add_task_button).setOnClickListener {
            val intent = Intent(this, AddEditTaskActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_TASK)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_more -> {
                    val intent = Intent(this, InfoActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_home -> {
                    true
                }
                R.id.action_logout -> {
                    showLogoutConfirmationDialog()
                    true
                }
                else -> false
            }
        }

        bottomNavigationView.selectedItemId = R.id.action_home

        loadTareas("titulo")
    }

    private fun loadTareas(orderBy: String) {
        val userId = getCurrentUserId()
        tareasListener?.remove()
        tareasListener = firestore.collection("tareas")
            .whereEqualTo("userId", userId)
            .orderBy(orderBy, Query.Direction.ASCENDING)
            .addSnapshotListener { result, e ->
                if (e != null) {
                    // Manejar el error
                    return@addSnapshotListener
                }
                if (result != null) {
                    val tareas = result.toObjects(Tarea::class.java)
                    adapter.submitList(tareas)
                }
            }
    }

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar salida")
            .setMessage("¿Está seguro que desea salir?")
            .setPositiveButton("Sí") { _, _ ->
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        tareasListener?.remove()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == RESULT_OK) {
        when (requestCode) {
            REQUEST_CODE_ADD_TASK -> {
            }
            REQUEST_CODE_EDIT_TASK -> {
                val action = data?.getStringExtra("ACTION")
                when (action) {
                    "EDIT" -> {
                    }
                    "DELETE" -> {
                    }
                }
            }
        }
        loadTareas("titulo")
    }
}

    companion object {
        private const val REQUEST_CODE_ADD_TASK = 1
        private const val REQUEST_CODE_EDIT_TASK = 2
    }
}