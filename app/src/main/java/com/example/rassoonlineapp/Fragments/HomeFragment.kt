package com.example.rassoonlineapp.Fragments

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Adapter.PostAdapter
import com.example.rassoonlineapp.Model.Post
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.UsersActivity
import com.example.rassoonlineapp.ViewModel.SharedViewModel
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
    private lateinit var sharedViewModel: SharedViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_home)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = PostAdapter(requireContext(), postList as ArrayList<Post>)
        recyclerView.adapter = postAdapter

        retrievePosts()
        /*
        sharedViewModel.acceptedProposal.observe(viewLifecycleOwner, { acceptedProposal ->
            // Remover o post da Firebase
            postsRef?.child(acceptedProposal.postId!!)?.removeValue()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Remover o post da lista de posts local
                    val removed = postList?.removeAll { it.postId == acceptedProposal.postId }
                    Log.d("HomeFragment", "PostId to remove: ${acceptedProposal.postId}, Removed: $removed")

                    if (removed == true) {
                        postAdapter?.notifyItemRemoved(postList?.size ?: 0)
                        postAdapter?.notifyItemRangeChanged(0, postList?.size ?: 0)
                    }
                } else {
                    Log.e("HomeFragment", "Error removing post: ${task.exception?.message}")
                }
            }
        })
         */

        sharedViewModel.acceptedProposal.observe(viewLifecycleOwner, { acceptedProposal ->
            // Remover o post da lista de posts local
            val removed = postList?.removeAll { it.postId == acceptedProposal.postId }
            Log.d("HomeFragment", "PostId to remove: ${acceptedProposal.postId}, Removed: $removed")

            if (removed == true) {
                postAdapter?.notifyItemRemoved(postList?.size ?: 0)
                postAdapter?.notifyItemRangeChanged(0, postList?.size ?: 0)
            }
        })





        view.findViewById<ImageView>(R.id.message_icon).setOnClickListener {
            val intent = Intent(context, UsersActivity::class.java)
            startActivity(intent)
        }



        return view
    }

    /*
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

     */

    private fun retrievePosts() {
        if (!isNetworkConnected()) {
            Toast.makeText(requireContext(), "Conecte-se à internet para ver os posts", Toast.LENGTH_SHORT).show()
            return
        }
        // Mostrar a ProgressBar
        view?.findViewById<ProgressBar>(R.id.progress_bar)?.visibility = View.VISIBLE

        postsRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList?.clear()

                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    post?.let {
                        if (it.isVisible) {  // Verifica se o post é visível
                            postList?.add(it)
                        }
                    }
                }

                postAdapter?.notifyDataSetChanged()
                // Esconder a ProgressBar após os dados serem carregados
                view?.findViewById<ProgressBar>(R.id.progress_bar)?.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Error retrieving posts: ${error.message}")
                // Em caso de erro, também esconda a ProgressBar
                view?.findViewById<ProgressBar>(R.id.progress_bar)?.visibility = View.GONE
            }
        })
    }





    private fun isNetworkConnected(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }




}
