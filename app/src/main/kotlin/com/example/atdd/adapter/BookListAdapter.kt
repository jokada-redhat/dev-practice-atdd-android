package com.example.atdd.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.atdd.R
import com.example.atdd.model.Book
import com.example.atdd.model.BookStatus

class BookListAdapter(
    private var books: List<Book> = emptyList()
) : RecyclerView.Adapter<BookListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTitle: TextView = view.findViewById(R.id.textBookTitle)
        val textAuthor: TextView = view.findViewById(R.id.textBookAuthor)
        val textIsbn: TextView = view.findViewById(R.id.textBookIsbn)
        val textYear: TextView = view.findViewById(R.id.textBookYear)
        val textStatus: TextView = view.findViewById(R.id.textBookStatus)
        val buttonBorrow: View = view.findViewById(R.id.buttonBorrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        val context = holder.itemView.context

        holder.textTitle.text = book.title
        holder.textAuthor.text = book.author
        holder.textIsbn.text = book.isbn
        holder.textYear.text = book.publicationYear

        holder.buttonBorrow.visibility = View.GONE

        if (book.status == BookStatus.AVAILABLE) {
            holder.textStatus.text = context.getString(R.string.book_status_available)
            holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.primary))
        } else {
            holder.textStatus.text = context.getString(R.string.book_status_borrowed)
            holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.on_surface_variant))
        }
    }

    override fun getItemCount() = books.size

    fun updateBooks(newBooks: List<Book>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = books.size
            override fun getNewListSize() = newBooks.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) = books[oldPos].id == newBooks[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) = books[oldPos] == newBooks[newPos]
        })
        books = newBooks
        diffResult.dispatchUpdatesTo(this)
    }
}
