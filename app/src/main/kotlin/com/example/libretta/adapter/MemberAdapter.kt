package com.example.libretta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.libretta.R
import com.example.libretta.model.Member

class MemberAdapter(private var members: List<Member> = emptyList(), private val onMemberClick: (Member) -> Unit = {}) :
    RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textMemberId: TextView = view.findViewById(R.id.textMemberId)
        val textMemberName: TextView = view.findViewById(R.id.textMemberName)
        val textLoanCount: TextView = view.findViewById(R.id.textLoanCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        val context = holder.itemView.context

        holder.textMemberId.text = context.getString(R.string.member_id_prefix, member.id)
        holder.textMemberName.text = member.name
        holder.textLoanCount.text = when (member.loanCount) {
            0 -> context.getString(R.string.loan_count_zero)
            1 -> context.getString(R.string.loan_count_single)
            else -> context.getString(R.string.loan_count_multiple, member.loanCount)
        }

        holder.itemView.setOnClickListener {
            onMemberClick(member)
        }
    }

    override fun getItemCount() = members.size

    fun updateMembers(newMembers: List<Member>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = members.size
            override fun getNewListSize() = newMembers.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) = members[oldPos].id == newMembers[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) = members[oldPos] == newMembers[newPos]
        })
        members = newMembers
        diffResult.dispatchUpdatesTo(this)
    }
}
