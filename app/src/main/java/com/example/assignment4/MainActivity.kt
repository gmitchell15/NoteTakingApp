//George Mitchell
//CSCI 4020
//Assignment 4
package com.example.assignment4

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment4.database.AppDatabase
import com.example.assignment4.database.Note
import com.example.assignment4.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MyAdapter
    //creates a mutable list of Note objects
    private val notes = mutableListOf<Note>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)
        binding.myRecyclerview.setLayoutManager(layoutManager)

        //adds divider between each list item
        val divider = DividerItemDecoration(
            applicationContext, layoutManager.orientation
        )
        binding.myRecyclerview.addItemDecoration(divider)

        adapter = MyAdapter()
        binding.myRecyclerview.setAdapter(adapter)

        loadAllNotes()
    }

    //Adds all notes to the adapter
    private fun loadAllNotes() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val dao = db.noteDao()
            val results = dao.getAllNotes()
            for (note in results) {
                Log.i("STATUS_MAIN:", "read ${note}")
            }

            withContext(Dispatchers.Main) {
                notes.clear()
                //add results, which contains all notes, to adapter
                notes.addAll(results)
                //updates adapter
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.option_menu_add_note){
            //calls addNewNote function
            addNewNote()

        }else if(item.itemId == R.id.option_menu_sortTitle){
            //calls sortNotes function
            sortNotes()

        }else if(item.itemId == R.id.option_menu_sort_date){
            //calls sortNotesByDate function
            sortNotesByDate()
        }
        return super.onOptionsItemSelected(item)
    }

    private val startForAddResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result : ActivityResult ->
            loadAllNotes()
        }

    private val startForUpdateResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result : ActivityResult ->

                if(result.resultCode == Activity.RESULT_OK){
                    loadAllNotes()
                }
        }

    //opens the NoteActivity where the user can add a new note
    private fun addNewNote() {
        val intent = Intent(applicationContext, NoteActivity:: class.java)
        intent.putExtra(
            getString(R.string.intent_purpose_key),
            getString(R.string.intent_purpose_add_note)
        )
        startForAddResult.launch(intent)
    }

    //sorts notes based on the title
    private fun sortNotes() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val dao = db.noteDao()
            val results = dao.sortAllNotes()
            for (note in results) {
                Log.i("STATUS_MAIN:", "read ${note}")
            }

            withContext(Dispatchers.Main) {
                notes.clear()
                //add results, which contains all notes, to adapter
                notes.addAll(results)
                //updates adapter
                adapter.notifyDataSetChanged()
            }
        }
    }

    //sorts notes based on the last modified date
    private fun sortNotesByDate(){
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val dao = db.noteDao()
            val results = dao.sortAllNotesByDate()
            for (note in results) {
                Log.i("STATUS_MAIN:", "read ${note}")
            }

            withContext(Dispatchers.Main) {
                notes.clear()
                //add results, which contains all notes, to adapter
                notes.addAll(results)
                //updates adapter
                adapter.notifyDataSetChanged()
            }
        }
    }

    inner class MyViewHolder(val view: View) :
        RecyclerView.ViewHolder(view),
        View.OnClickListener, View.OnLongClickListener {

        init {
            view.setOnClickListener(this)
            view.setOnLongClickListener(this)
        }

        fun setText(text : String){
            view.findViewById<TextView>(R.id.note_textview)
                .setText(text)
        }

        override fun onClick(view: View?) {
            if(view != null){
                val note = notes[adapterPosition]
                //AlertDialog that displays all the note info and lets user edit the note
                val builder = AlertDialog.Builder(view!!.context)
                    .setTitle("View or Edit Note.")
                    .setMessage(" ${note.title}\n ${note.notes}\n ${note.lastModified}")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Edit Note") {
                            //if the user presses the Edit Note button, they will be able to
                            // edit the current note
                            dialogInterface, whichButton ->
                                val intent = Intent(applicationContext, NoteActivity::class.java)
                                intent.putExtra(
                                getString(R.string.intent_purpose_key),
                                getString(R.string.intent_purpose_update_note)
                            )
                            val note = notes[adapterPosition]
                            intent.putExtra(
                                getString(R.string.intent_key_note_id),
                                note.id
                            )

                            startForUpdateResult.launch(intent)
                    }
                builder.show()
            }

        }

        //Prompts user if they want to delete a Note on LongClick
        override fun onLongClick(view: View?): Boolean {
            val note = notes[adapterPosition]

            //Asks user if they want to delete a note
            val builder = AlertDialog.Builder(view!!.context)
                .setTitle("Delete Note?")
                .setMessage("Are you sure you want to delete the note ${note.title}?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok"){
                    dialogInterface, whichButton ->
                    //deletes note from database
                    CoroutineScope(Dispatchers.IO).launch{
                        AppDatabase.getDatabase(applicationContext)
                            .noteDao()
                            .deleteNote(note)

                        loadAllNotes()
                    }

                }
            builder.show()

            return true
        }


    }

    inner class MyAdapter() : RecyclerView.Adapter<MyViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.note_view, parent, false) as TextView


            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val note = notes[position]
            if(notes.size > 0){
                holder.setText("${note.title}")
            }
        }

        override fun getItemCount(): Int {
            return notes.size
        }

    }

}