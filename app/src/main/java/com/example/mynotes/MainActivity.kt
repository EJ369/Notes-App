package com.example.mynotes

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mynotes.databinding.ActivityMainBinding
import com.example.mynotes.databinding.BottomAddNotesBinding
import com.example.mynotes.databinding.NotesDialogBinding
import com.example.mynotes.utils.NotesEntity
import com.example.mynotes.utils.NotesViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class MainActivity : AppCompatActivity(), onNoteClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var notesViewModel: NotesViewModel
    private lateinit var adapter: NotesAdapter
    private var selectedDate = ""
    private var selectedColor = "Blue"
    private var selectedCategory = "Personal"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.layout_bg, typedValue, true)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, typedValue.resourceId)

        notesViewModel = ViewModelProvider(this)[NotesViewModel::class.java]

        adapter = NotesAdapter(this)
        binding.notesRecyclerview.adapter = adapter
        binding.notesRecyclerview.layoutManager = GridLayoutManager(this, 2)

        notesViewModel.allNotes.observe(this) { notesList ->
            if (notesList.isNotEmpty()) {
                adapter.setNotes(notesList)
            }
        }

        binding.notesSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                getSearchData(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        binding.notesFilter.setOnClickListener {
            showFilterDialog()
        }

        binding.notesAdd.setOnClickListener {
            addNotes()
        }
    }

    private fun showFilterDialog() {
        val popupMenu = PopupMenu(this, binding.notesFilter, R.style.AlertDialogTheme)

        val list = resources.getStringArray(R.array.note_categories)
        list.forEachIndexed { index, category ->
            popupMenu.menu.add(Menu.NONE, index, Menu.NONE, category)
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            selectedCategory = list[menuItem.itemId]
            if (selectedCategory == "All") {
                binding.notesFilterTextLayout.visibility = View.GONE
            } else {
                binding.notesFilterTextLayout.visibility = View.VISIBLE
                binding.notesFilterText.text = "By $selectedCategory"
            }

            CoroutineScope(Dispatchers.IO).launch {
                val filteredData = notesViewModel.notesByCategory(selectedCategory)
                withContext(Dispatchers.Main) {
                    adapter.setNotes(filteredData)
                }
            }
            true
        }

        popupMenu.show()
    }

    private fun getSearchData(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val searchData = notesViewModel.searchNotes(query)
            withContext(Dispatchers.Main) {
                adapter.setNotes(searchData)
            }
        }
    }

    private fun addNotes() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomAddNotesBinding = BottomAddNotesBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bottomAddNotesBinding.root)

        var category = "Personal"
        var color = "Blue"

        bottomAddNotesBinding.bottomAddNotesBlueColor.setOnClickListener {
            color = "Blue"
            setImage(bottomAddNotesBinding, bottomAddNotesBinding.bottomAddNotesBlueColorImage)
        }

        bottomAddNotesBinding.bottomAddNotesGreenColor.setOnClickListener {
            color = "Green"
            setImage(bottomAddNotesBinding, bottomAddNotesBinding.bottomAddNotesGreenColorImage)
        }

        bottomAddNotesBinding.bottomAddNotesYellowColor.setOnClickListener {
            color = "Yellow"
            setImage(bottomAddNotesBinding, bottomAddNotesBinding.bottomAddNotesYellowColorImage)
        }

        bottomAddNotesBinding.bottomAddNotesRedColor.setOnClickListener {
            color = "Red"
            setImage(bottomAddNotesBinding, bottomAddNotesBinding.bottomAddNotesRedColorImage)
        }

        bottomAddNotesBinding.bottomAddNotesPurpleColor.setOnClickListener {
            color = "Purple"
            setImage(bottomAddNotesBinding, bottomAddNotesBinding.bottomAddNotesPurpleColorImage)
        }

        val currentDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getCurrentDate()
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        val list = ArrayList(resources.getStringArray(R.array.note_categories).toList())
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bottomAddNotesBinding.itemCategory.adapter = adapter

        bottomAddNotesBinding.itemCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                category = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle the case where nothing is selected if needed
            }
        }

        bottomAddNotesBinding.bottomAddNotesSaveBtn.setOnClickListener {
            val title = bottomAddNotesBinding.bottomAddNotesTitle.text.toString()
            val message = bottomAddNotesBinding.bottomAddNotesMessage.text.toString()
            val notes = NotesEntity(0, title, message, currentDate, category,false, color)

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val result = notesViewModel.insertNote(notes)
                    withContext(Dispatchers.Main) {
                        if (result > 0) {
                            Toast.makeText(this@MainActivity, "Notes Added", Toast.LENGTH_SHORT).show()
                            bottomSheetDialog.dismiss()
                        } else {
                            Toast.makeText(this@MainActivity, "Notes Not Added", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        bottomSheetDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return currentDate.format(formatter)
    }

    private fun setImage(bottomAddNotesBinding: BottomAddNotesBinding, bottomAddNotesColorImage: ImageView) {
        bottomAddNotesBinding.bottomAddNotesBlueColorImage.setImageResource(0)
        bottomAddNotesBinding.bottomAddNotesGreenColorImage.setImageResource(0)
        bottomAddNotesBinding.bottomAddNotesYellowColorImage.setImageResource(0)
        bottomAddNotesBinding.bottomAddNotesRedColorImage.setImageResource(0)
        bottomAddNotesBinding.bottomAddNotesPurpleColorImage.setImageResource(0)

        bottomAddNotesColorImage.setImageResource(R.drawable.baseline_check_24)
    }

    override fun onNoteClick(notesEntity: NotesEntity) {
        val notesDialogBinding = NotesDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this, R.style.PopupDialogTheme).setView(notesDialogBinding.root).create()
        var edit = false

        when (notesEntity.color) {
            "Green" -> {
                notesDialogBinding.notesDialogGreenColorImage.setImageResource(R.drawable.baseline_check_24)
                notesDialogBinding.notesDialogMain.setBackgroundColor(ContextCompat.getColor(this, R.color.cardGreen))
            }

            "Yellow" -> {
                notesDialogBinding.notesDialogYellowColorImage.setImageResource(R.drawable.baseline_check_24)
                notesDialogBinding.notesDialogMain.setBackgroundColor(ContextCompat.getColor(this, R.color.cardYellow))
            }

            "Red" -> {
                notesDialogBinding.notesDialogRedColorImage.setImageResource(R.drawable.baseline_check_24)
                notesDialogBinding.notesDialogMain.setBackgroundColor(ContextCompat.getColor(this, R.color.cardRed))
            }

            "Purple" -> {
                notesDialogBinding.notesDialogPurpleColorImage.setImageResource(R.drawable.baseline_check_24)
                notesDialogBinding.notesDialogMain.setBackgroundColor(ContextCompat.getColor(this, R.color.cardPurple))
            }

            else -> {
                notesDialogBinding.notesDialogBlueColorImage.setImageResource(R.drawable.baseline_check_24)
                notesDialogBinding.notesDialogMain.setBackgroundColor(ContextCompat.getColor(this, R.color.cardBlue))
            }
        }
        notesDialogBinding.notesDialogDate.text = formattedDate(notesEntity.date)
        notesDialogBinding.notesDialogTitle.text = notesEntity.title
        notesDialogBinding.notesDialogTitleEdt.setText(notesEntity.title)
        notesDialogBinding.notesDialogMessage.text = notesEntity.message
        notesDialogBinding.notesDialogMessageEdt.setText(notesEntity.message)

        notesDialogBinding.notesDialogDate.setOnClickListener {
            if (edit) {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                    val mDay = if (selectedDay < 10) "0$selectedDay" else selectedDay
                    val mMonth = if (selectedMonth < 9) "0${selectedMonth + 1}" else (selectedMonth + 1)
                    selectedDate = "$selectedYear-$mMonth-$mDay"
                    notesDialogBinding.notesDialogDate.text = formattedDate(selectedDate)
                }, year, month, day)
                datePickerDialog.show()
            }
        }

        notesDialogBinding.notesDialogBlueColor.setOnClickListener {
            if (edit) {
                selectedColor = "Blue"
                setSelectedImage(notesDialogBinding, notesDialogBinding.notesDialogBlueColorImage, selectedColor)
            }
        }

        notesDialogBinding.notesDialogGreenColor.setOnClickListener {
            if (edit) {
                selectedColor = "Green"
                setSelectedImage(notesDialogBinding, notesDialogBinding.notesDialogGreenColorImage, selectedColor)
            }
        }

        notesDialogBinding.notesDialogYellowColor.setOnClickListener {
            if (edit) {
                selectedColor = "Yellow"
                setSelectedImage(notesDialogBinding, notesDialogBinding.notesDialogYellowColorImage, selectedColor)
            }
        }

        notesDialogBinding.notesDialogRedColor.setOnClickListener {
            if (edit) {
                selectedColor = "Red"
                setSelectedImage(notesDialogBinding, notesDialogBinding.notesDialogRedColorImage, selectedColor)
            }
        }

        notesDialogBinding.notesDialogPurpleColor.setOnClickListener {
            if (edit) {
                selectedColor = "Purple"
                setSelectedImage(notesDialogBinding, notesDialogBinding.notesDialogPurpleColorImage, selectedColor)
            }
        }

        notesDialogBinding.notesDialogEdit.setOnClickListener {
            notesDialogBinding.notesDialogEdit.visibility = View.GONE
            notesDialogBinding.notesDialogTitle.visibility = View.GONE
            notesDialogBinding.notesDialogMessage.visibility = View.GONE
            notesDialogBinding.notesDialogTitleEdt.visibility = View.VISIBLE
            notesDialogBinding.notesDialogMessageEdt.visibility = View.VISIBLE
            notesDialogBinding.notesDialogBtnCard.visibility = View.VISIBLE
            edit = true
        }

        notesDialogBinding.notesDialogSaveBtn.setOnClickListener {
            val title = notesDialogBinding.notesDialogTitleEdt.text.toString()
            val message = notesDialogBinding.notesDialogMessageEdt.text.toString()
            val date = selectedDate.ifEmpty { notesEntity.date }
            val color = selectedColor.ifEmpty { notesEntity.color }
            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                val notes = NotesEntity(notesEntity.id, title, message, date, notesEntity.category, notesEntity.pin, color)
                CoroutineScope(Dispatchers.IO).launch {
                    val result = notesViewModel.updateNote(notes)
                    withContext(Dispatchers.Main) {
                        if (result > 0) {
                            dialog.dismiss()
                        }
                    }
                }
            }
        }
        dialog.show()
        val window = dialog.window
        window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun setSelectedImage(notesDialogBinding: NotesDialogBinding, notesDialogColorImage: ImageView, color: String) {
        notesDialogBinding.notesDialogBlueColorImage.setImageResource(0)
        notesDialogBinding.notesDialogGreenColorImage.setImageResource(0)
        notesDialogBinding.notesDialogYellowColorImage.setImageResource(0)
        notesDialogBinding.notesDialogRedColorImage.setImageResource(0)
        notesDialogBinding.notesDialogPurpleColorImage.setImageResource(0)

        notesDialogColorImage.setImageResource(R.drawable.baseline_check_24)

        when (color) {
            "Green" -> {
                notesDialogBinding.notesDialogMain.setBackgroundColor(ContextCompat.getColor(this, R.color.cardGreen))
            }

            "Yellow" -> {
                notesDialogBinding.notesDialogMain.setBackgroundColor(ContextCompat.getColor(this, R.color.cardYellow))
            }

            "Red" -> {
                notesDialogBinding.notesDialogMain.setBackgroundColor(ContextCompat.getColor(this, R.color.cardRed))
            }

            "Purple" -> {
                notesDialogBinding.notesDialogMain.setBackgroundColor(ContextCompat.getColor(this, R.color.cardPurple))
            }

            else -> {
                notesDialogBinding.notesDialogMain.setBackgroundColor(ContextCompat.getColor(this, R.color.cardBlue))
            }
        }
    }

    private fun formattedDate(date: String): String {
        val parsedDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val outputFormatter = DateTimeFormatter.ofPattern("dd\nMMM")
        return parsedDate.format(outputFormatter)
    }
}