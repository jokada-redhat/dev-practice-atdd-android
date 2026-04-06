package com.example.libretta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.libretta.R
import com.example.libretta.loan.LoanRepository
import com.example.libretta.model.Book
import com.google.android.material.button.MaterialButton

class BookAdapter(
    private var books: List<Book> = emptyList(),
    private val loanRepository: LoanRepository,
    private val onBorrowClick: (Book) -> Unit = {}
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textBookTitle: TextView = view.findViewById(R.id.textBookTitle)
        val textBookAuthor: TextView = view.findViewById(R.id.textBookAuthor)
        val textBookStatus: TextView = view.findViewById(R.id.textBookStatus)
        val textBookIsbn: TextView = view.findViewById(R.id.textBookIsbn)
        val textBookYear: TextView = view.findViewById(R.id.textBookYear)
        val buttonBorrow: MaterialButton = view.findViewById(R.id.buttonBorrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        val context = holder.itemView.context

        holder.textBookTitle.text = book.title
        holder.textBookAuthor.text = book.author
        holder.textBookIsbn.text = book.isbn
        holder.textBookYear.text = book.publicationYear

        // ステータスバッジの設定
        val isAvailable = loanRepository.findActiveByBookId(book.id) == null
        if (isAvailable) {
            holder.textBookStatus.text = context.getString(R.string.book_status_available)
            holder.textBookStatus.setTextColor(
                ContextCompat.getColor(context, R.color.primary)
            )
            holder.buttonBorrow.isEnabled = true
        } else {
            holder.textBookStatus.text = context.getString(R.string.book_status_borrowed)
            holder.textBookStatus.setTextColor(
                ContextCompat.getColor(context, R.color.on_surface_variant)
            )
            holder.buttonBorrow.isEnabled = false
        }

        holder.buttonBorrow.setOnClickListener {
            onBorrowClick(book)
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
