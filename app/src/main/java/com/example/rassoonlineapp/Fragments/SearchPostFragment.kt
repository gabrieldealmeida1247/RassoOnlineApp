package com.example.rassoonlineapp.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Adapter.PostSearchAdapter
import com.example.rassoonlineapp.Model.Post
import com.example.rassoonlineapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchPostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchPostFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var recyclerView: RecyclerView? = null
    private var searchPostAdapter: PostSearchAdapter? = null
    private var mPost: MutableList<Post>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search_post, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        mPost = ArrayList()
        searchPostAdapter = context?.let { PostSearchAdapter(it, mPost as ArrayList<Post>, true) }
        recyclerView?.adapter = searchPostAdapter

        // Busca todos os usuários assim que o layout for inflado
        retrievePost()

        view.findViewById<EditText>(R.id.search_edit_text).addTextChangedListener(object:
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (view.findViewById<EditText>(R.id.search_edit_text).text.toString() == "" ){

                }else{
                    recyclerView?.visibility = View.VISIBLE
                    retrievePost()
                    searchPost(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        } )
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Busca todos os usuários assim que o fragmento é criado
        retrievePost()
    }

    private fun searchPost(input: String) {
        val query = FirebaseDatabase.getInstance().reference
            .child("Posts")
            .orderByChild("titulo")
            .startAt(input)
            .endAt(input + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mPost?.clear()

                for (snapshot in dataSnapshot.children){
                    val post = snapshot.getValue(Post::class.java)
                    if (post != null){
                        mPost?.add(post)
                    }
                }
                // Notifica o adapter sobre as mudanças nos dados
                searchPostAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun retrievePost() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("SearchFragment", "DataSnapshot exists")
                    mPost?.clear()
                    for (snapshot in dataSnapshot.children) {
                        val post = snapshot.getValue(Post::class.java)
                        if (post != null) {
                            mPost?.add(post)
                            Log.d("SearchFragment", "User added: ${post.titulo}")
                        }
                    }
                    searchPostAdapter?.notifyDataSetChanged()
                } else {
                    Log.d("SearchFragment", "No users found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SearchFragment", "retrieveUsers onCancelled: ${error.message}")
            }
        })
    }
}