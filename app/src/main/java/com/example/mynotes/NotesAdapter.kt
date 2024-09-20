package com.example.mynotes

import android.app.AlertDialog
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotes.databinding.ItemLayoutBinding
import com.example.mynotes.utils.NotesEntity
import com.example.mynotes.utils.NotesViewModel
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface onNoteClickListener {
    fun onNoteClick(notesEntity: NotesEntity)
}

class NotesAdapter(private var onNoteClickListener: onNoteClickListener) : RecyclerView.Adapter<NotesAdapter.NotesViewAdapter>() {
    private var notesList: List<NotesEntity> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesAdapter.NotesViewAdapter {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotesViewAdapter(binding)
    }

    override fun onBindViewHolder(holder: NotesAdapter.NotesViewAdapter, position: Int) {
        holder.bind(notesList[position])
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    inner class NotesViewAdapter(private val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notesEntity: NotesEntity) {
            val context = binding.root.context
            val notesViewModel = ViewModelProvider(context as AppCompatActivity)[NotesViewModel::class.java]
            binding.itemTitle.text = notesEntity.title
            binding.itemMessage.text = notesEntity.message
            binding.itemDate.text = formattedDate(notesEntity.date)

            when (notesEntity.color) {
                "Green" -> {
                    binding.itemLayout.setCardBackgroundColor(ContextCompat.getColor(context, R.color.cardGreen));
                }
                "Yellow" -> {
                    binding.itemLayout.setCardBackgroundColor(ContextCompat.getColor(context, R.color.cardYellow));
                }
                "Red" -> {
                    binding.itemLayout.setCardBackgroundColor(ContextCompat.getColor(context, R.color.cardRed));
                }
                "Purple" -> {
                    binding.itemLayout.setCardBackgroundColor(ContextCompat.getColor(context, R.color.cardPurple));
                }
                else -> {
                    binding.itemLayout.setCardBackgroundColor(ContextCompat.getColor(context, R.color.cardBlue));
                }
            }

            if (notesEntity.pin) {
                binding.itemWishlist.setImageResource(R.drawable.pin_icon)
            } else {
                binding.itemWishlist.setImageResource(R.drawable.unpin_icon)
            }

            binding.itemWishlist.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    if (notesEntity.pin) {
                        notesViewModel.updatePin(notesEntity.id, false)
                    } else {
                        notesViewModel.updatePin(notesEntity.id, true)
                    }
                }
            }

            binding.itemLayout.setOnClickListener {
                onNoteClickListener.onNoteClick(notesEntity)
            }

            binding.itemDelete.setOnClickListener {
                deleteNote(context, notesEntity, notesViewModel)
            }
        }
    }

    private fun deleteNote(context: AppCompatActivity, notesEntity: NotesEntity, notesViewModel: NotesViewModel) {
        val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
        builder.setTitle("Delete ${notesEntity.title}")

        builder.setPositiveButton("Yes") { dialog, _ ->
            CoroutineScope(Dispatchers.IO).launch {
                notesViewModel.deleteNote(notesEntity)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    fun formattedDate(date: String): String {
        val parsedDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val outputFormatter = DateTimeFormatter.ofPattern("dd\nMMM")
        return parsedDate.format(outputFormatter)
    }

    fun setNotes(notes: List<NotesEntity>) {
        this.notesList = notes
        notifyDataSetChanged()
    }
}