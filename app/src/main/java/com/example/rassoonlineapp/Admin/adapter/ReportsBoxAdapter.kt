package com.example.rassoonlineapp.Admin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Admin.fragments.ReportsContentFragment
import com.example.rassoonlineapp.Model.inappropriateContent
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseUser

class ReportsBoxAdapter(private val mContext: Context, private val content: List<inappropriateContent>)
    :RecyclerView.Adapter<ReportsBoxAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null
        override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReportsBoxAdapter.ViewHolder {
            val view = LayoutInflater.from(mContext).inflate(R.layout.reports_box_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportsBoxAdapter.ViewHolder, position: Int) {
      val content:  inappropriateContent = content[position]

        holder.textView_username_report.text = "${content.name} " +
                "Denúnciou conteúdo inadequado"

        holder.btnShow.setOnClickListener {
            // Verificar se o usuário atual é diferente do usuário que publicou o projeto

                // O usuário pode fazer uma proposta
                val fragmentManager = (mContext as AppCompatActivity).supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                val reportContent = ReportsContentFragment()

                // Substitui o fragmento atual pelo InaproprieteContentFragment
                fragmentTransaction.replace(R.id.fragment_container,reportContent)
                fragmentTransaction.addToBackStack(null) // Isso permite voltar ao fragmento anterior ao pressionar o botão Voltar
                fragmentTransaction.commit()
        }

    }

    override fun getItemCount(): Int {
        return content.size
    }


    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
       val textView_username_report = itemView.findViewById<TextView>(R.id.textView_username_report)
        val btnShow = itemView.findViewById<Button>(R.id.btn_show)
    }
}