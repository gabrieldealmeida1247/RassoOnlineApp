package com.example.rassoonlineapp.Admin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Adapter.PostAdapter
import com.example.rassoonlineapp.Admin.fragments.ReportsFragment
import com.example.rassoonlineapp.Model.Post
import com.example.rassoonlineapp.Model.inappropriateContent
import com.example.rassoonlineapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ReportsContentAdapter(private val mContext: Context, private val content:List<inappropriateContent>): RecyclerView.Adapter<ReportsContentAdapter.ViewHolder>() {

    private val postList = mutableListOf<Post>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReportsContentAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.report_content_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportsContentAdapter.ViewHolder, position: Int) {
        val content: inappropriateContent = content[position]


        holder.textView_usermane.text = "${content.name} denúncio conteúdo inapropriado"
        holder.textView_subject.text = "${content.subject}"
        holder.textView_description.text = "${content.message}"

        holder.btnClose.setOnClickListener {
            // Verificar se o usuário atual é diferente do usuário que publicou o projeto

            // O usuário pode fazer uma proposta
            val fragmentManager = (mContext as AppCompatActivity).supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val reportContent = ReportsFragment()

            // Substitui o fragmento atual pelo InaproprieteContentFragment
            fragmentTransaction.replace(R.id.fragment_container,reportContent)
            fragmentTransaction.addToBackStack(null) // Isso permite voltar ao fragmento anterior ao pressionar o botão Voltar
            fragmentTransaction.commit()
        }

        // Carrega o conteúdo do post
        loadPostData(content.postId, holder.report_post)
    }

    override fun getItemCount(): Int {
        return content.size
    }


    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
     val textView_usermane = itemView.findViewById<TextView>(R.id.textView_username_report)
        val textView_subject = itemView.findViewById<TextView>(R.id.textView_subject)
        val textView_description = itemView.findViewById<TextView>(R.id.textView_description_report)
        val report_post = itemView.findViewById<RecyclerView>(R.id.report_post)
        val btnClose = itemView.findViewById<Button>(R.id.btnClose)

        init {
            report_post.layoutManager = LinearLayoutManager(mContext)
        }
    }


    private fun loadPostData(postId: String, reportPost: RecyclerView) {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)
        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val post = dataSnapshot.getValue(Post::class.java)
                    post?.let {
                        postList.add(it)
                        val postAdapter = PostAdapter(mContext, postList, showProposalButton = false)
                        reportPost.adapter = postAdapter
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }


}