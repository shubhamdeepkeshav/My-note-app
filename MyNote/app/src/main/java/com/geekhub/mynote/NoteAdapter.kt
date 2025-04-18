package com.geekhub.mynote

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.geekhub.mynote.databinding.NoteItemBinding

class NoteAdapter(
    private val onAction: (Note, String) -> Unit
) : ListAdapter<Note, NoteAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: NoteItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.noteTitle.text = note.title
            binding.noteText.text = note.content

            // Set background color based on title/category
            val bgColor = getColorForCategory(note.title)
            binding.cardView.setCardBackgroundColor(bgColor)

            // Set edit/delete actions
            binding.btnEdit.setOnClickListener { onAction(note, "edit") }
            binding.btnDelete.setOnClickListener { onAction(note, "delete") }
        }

        private fun getColorForCategory(title: String): Int {
            val context = binding.root.context
            return when (title.lowercase()) {
                "coding" -> ContextCompat.getColor(context, R.color.colorCoding)
                "growth" -> ContextCompat.getColor(context, R.color.colorGrowth)
                "item's" -> ContextCompat.getColor(context, R.color.colorItems)
                "last note" -> ContextCompat.getColor(context, R.color.colorLastNote)
                "technology" -> ContextCompat.getColor(context, R.color.colorTechnology)
                "targets" -> ContextCompat.getColor(context, R.color.colorTargets)
                else -> ContextCompat.getColor(context, R.color.colorDefault)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(NoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(old: Note, new: Note) = old.id == new.id
            override fun areContentsTheSame(old: Note, new: Note) = old == new
        }
    }
}
