package com.example.assignment4

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.assignment4.database.AppDatabase
import com.example.assignment4.database.Note
import com.example.assignment4.databinding.ActivityNoteBinding
import kotlinx.coroutines.*
import java.util.*

class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    //global title variable
    private var title =""
    //global purpose variable
    private var purpose : String? = ""
    //sets global noteId variable to -1, used for updating a note
    private var noteId : Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //gets information from intent
        intent = getIntent()
        purpose = intent.getStringExtra(
            getString(R.string.intent_purpose_key)
        )

        if(purpose.equals(getString(R.string.intent_purpose_update_note))){
            noteId = intent.getLongExtra(
                getString(R.string.intent_key_note_id),
                -1
            )

            CoroutineScope(Dispatchers.IO).launch{
                val note = AppDatabase.getDatabase(applicationContext)
                    .noteDao()
                    .getNote(noteId)

                withContext(Dispatchers.Main){
                    binding.titleEditTextText.setText(note.title)
                    binding.noteEditTextTextMultiLine.setText(note.notes)
                }
            }
        }

        setTitle("${purpose} Note")


    }

    //everything inside operates when the back key is pressed
    override fun onBackPressed() {
        title = binding.titleEditTextText.getText().toString()
        Log.i("title", title)
        //Displays message if the title is empty
        if(title.isEmpty()){
            Toast.makeText(
                applicationContext,
                "Note title can't be empty", Toast.LENGTH_LONG
            ).show()
            return
        }


        val notes = binding.noteEditTextTextMultiLine.getText().toString()
        //Displays message if the note is empty
        if(notes.isEmpty()){
            Toast.makeText(
                applicationContext,
                "Note can't be empty", Toast.LENGTH_LONG
            ).show()
            return
        }


        // get the current date and time as a timestamp
        val lastModified = Calendar.getInstance().time.toString()


        CoroutineScope(Dispatchers.IO).launch{
            val noteDao = AppDatabase.getDatabase(applicationContext)
                .noteDao()


            if(purpose.equals(getString(R.string.intent_purpose_add_note))){
                //adds note to database
                val note = Note(0, title, notes,lastModified)
                noteId = noteDao.addNote(note)
            }else{
                //updates note with new values
                val note = Note(noteId, title, notes,lastModified)
                noteDao.updateNote(note)
            }

            val intent = Intent()
            intent.putExtra(
                getString(R.string.intent_purpose_key),
                noteId
            )

            withContext(Dispatchers.Main){
                setResult(RESULT_OK, intent)
                super.onBackPressed()
            }

        }

    }
}