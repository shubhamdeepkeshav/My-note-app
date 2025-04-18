package com.geekhub.mynote

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.room.*
import com.geekhub.mynote.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ----------------------
// Entity
// ----------------------
@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String, // ✅ NEW: for category
    val content: String
)

// ----------------------
// DAO
// ----------------------
@Dao
interface NoteDao {
    @Insert suspend fun insert(note: Note)
    @Update suspend fun update(note: Note)
    @Delete suspend fun delete(note: Note)

    @Query("SELECT * FROM Note ORDER BY id DESC")
    fun getAll(): LiveData<List<Note>>
}

// ----------------------
// Database
// ----------------------
@Database(entities = [Note::class], version = 1)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "notes_db"
                ).fallbackToDestructiveMigration() // ✅ Allow schema change (for dev only)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ----------------------
// MainActivity
// ----------------------
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: NoteDatabase
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = NoteDatabase.getDatabase(this)
        adapter = NoteAdapter { note, action -> handleAction(note, action) }

        binding.recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerView.adapter = adapter

        db.noteDao().getAll().observe(this) {
            adapter.submitList(it)
        }

        // FAB opens dialog to enter title & content
        binding.fabAdd.setOnClickListener {
            showAddNoteDialog()
        }
    }

    private fun showAddNoteDialog() {
        val titleInput = EditText(this).apply { hint = "Title (e.g. Coding)" }
        val contentInput = EditText(this).apply { hint = "Note content" }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 0)
            addView(titleInput)
            addView(contentInput)
        }

        AlertDialog.Builder(this)
            .setTitle("New Note")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString().trim()
                val content = contentInput.text.toString().trim()
                if (title.isNotEmpty() && content.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        db.noteDao().insert(Note(title = title, content = content))
                    }
                } else {
                    Toast.makeText(this, "Title and content required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleAction(note: Note, action: String) {
        when (action) {
            "edit" -> {
                val titleInput = EditText(this).apply { setText(note.title) }
                val contentInput = EditText(this).apply { setText(note.content) }

                val layout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(32, 32, 32, 0)
                    addView(titleInput)
                    addView(contentInput)
                }

                AlertDialog.Builder(this)
                    .setTitle("Edit Note")
                    .setView(layout)
                    .setPositiveButton("Update") { _, _ ->
                        val newTitle = titleInput.text.toString()
                        val newContent = contentInput.text.toString()
                        if (newTitle.isNotEmpty() && newContent.isNotEmpty()) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                db.noteDao().update(note.copy(title = newTitle, content = newContent))
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            "delete" -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    db.noteDao().delete(note)
                }
            }
        }
    }
}
