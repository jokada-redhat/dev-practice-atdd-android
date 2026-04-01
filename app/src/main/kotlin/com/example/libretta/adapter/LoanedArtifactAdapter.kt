package com.example.libretta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.libretta.R
import com.example.libretta.model.Book
import com.example.libretta.model.Loan
import com.example.libretta.model.Member
import com.google.android.material.button.MaterialButton
import java.time.format.DateTimeFormatter

data class LoanedArtifact(val loan: Loan, val book: Book, val member: Member)

class LoanedArtifactAdapter(
    private var artifacts: List<LoanedArtifact> = emptyList(),
    private var onReturnClick: ((LoanedArtifact) -> Unit)? = null
) : RecyclerView.Adapter<LoanedArtifactAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textBookTitle: TextView = view.findViewById(R.id.textLoanBookTitle)
        val textBorrowedDate: TextView = view.findViewById(R.id.textLoanBorrowedDate)
        val textBorrowerInitials: TextView = view.findViewById(R.id.textBorrowerInitials)
        val textBorrowerName: TextView = view.findViewById(R.id.textBorrowerName)
        val textBorrowerId: TextView = view.findViewById(R.id.textBorrowerId)
        val buttonReturn: MaterialButton = view.findViewById(R.id.buttonReturnBook)
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
        holder.textBorrowedDate.text = context.getString(
            R.string.borrowed_date_format,
            artifact.loan.borrowedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        )

        val initials = artifact.member.name.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
        holder.textBorrowerInitials.text = initials
        holder.textBorrowerName.text = artifact.member.name
        holder.textBorrowerId.text = context.getString(R.string.member_id_prefix, artifact.member.id)

        holder.buttonReturn.setOnClickListener {
            onReturnClick?.invoke(artifact)
        }
    }

    fun setOnReturnClickListener(listener: (LoanedArtifact) -> Unit) {
        onReturnClick = listener
    }

    override fun getItemCount() = artifacts.size

    fun updateArtifacts(newArtifacts: List<LoanedArtifact>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = artifacts.size
            override fun getNewListSize() = newArtifacts.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                artifacts[oldPos].loan.id == newArtifacts[newPos].loan.id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) = artifacts[oldPos] == newArtifacts[newPos]
        })
        artifacts = newArtifacts
        diffResult.dispatchUpdatesTo(this)
    }
}
