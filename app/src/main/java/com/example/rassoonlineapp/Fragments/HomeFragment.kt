package com.example.rassoonlineapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Adapter.PostAdapter
import com.example.rassoonlineapp.Model.Post
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.UsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null
    private var firebaseUser: FirebaseUser? = null
    private var postsRef: DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_home)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = PostAdapter(requireContext(), postList as ArrayList<Post>)
        recyclerView.adapter = postAdapter

        retrievePosts()

        view.findViewById<ImageView>(R.id.message_icon).setOnClickListener {
            val intent = Intent(context, UsersActivity::class.java)
            startActivity(intent)
        }



        return view
    }

    private fun retrievePosts() {
        postsRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList?.clear()

                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    post?.let {
                        postList?.add(it)
                    }
                }

                postAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
}
