package com.example.atdd.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.atdd.R
import com.example.atdd.model.Book
import com.example.atdd.model.Loan
import com.example.atdd.model.Member
import java.time.format.DateTimeFormatter

data class LoanedArtifact(
    val loan: Loan,
    val book: Book,
    val member: Member
)

class LoanedArtifactAdapter(
    private var artifacts: List<LoanedArtifact> = emptyList()
) : RecyclerView.Adapter<LoanedArtifactAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textBookTitle: TextView = view.findViewById(R.id.textLoanBookTitle)
        val textIsbn: TextView = view.findViewById(R.id.textLoanIsbn)
        val textDueDate: TextView = view.findViewById(R.id.textLoanDueDate)
        val textBorrowerInitials: TextView = view.findViewById(R.id.textBorrowerInitials)
        val textBorrowerName: TextView = view.findViewById(R.id.textBorrowerName)
        val textBorrowerId: TextView = view.findViewById(R.id.textBorrowerId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_loaned_artifact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artifact = artifacts[position]
        val context = holder.itemView.context

        holder.textBookTitle.text = artifact.book.title
        holder.textIsbn.text = artifact.book.isbn

        val dueDate = artifact.loan.borrowedDate.plusWeeks(2)
        holder.textDueDate.text = dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val initials = artifact.member.name.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
        holder.textBorrowerInitials.text = initials
        holder.textBorrowerName.text = artifact.member.name
        holder.textBorrowerId.text = context.getString(R.string.member_id_prefix, artifact.member.id)
    }

    override fun getItemCount() = artifacts.size

    fun updateArtifacts(newArtifacts: List<LoanedArtifact>) {
        artifacts = newArtifacts
        notifyDataSetChanged()
    }
}
