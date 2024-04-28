package com.example.rassoonlineapp.Admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Admin.adapter.ReportsBoxAdapter
import com.example.rassoonlineapp.Model.inappropriateContent
import com.example.rassoonlineapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Collections

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReportsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReportsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var contentList: List<inappropriateContent>? = null
    private var contentAdapter: ReportsBoxAdapter? = null
    private lateinit var recyclerView: RecyclerView


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
        val view = inflater.inflate(R.layout.fragment_reports, container, false)

            recyclerView = view.findViewById(R.id.recycler_view_content)
            recyclerView?.setHasFixedSize(true)
            recyclerView?.layoutManager = LinearLayoutManager(context)

        contentList = ArrayList()

        contentAdapter = ReportsBoxAdapter(requireContext(), contentList as ArrayList<inappropriateContent>)
        recyclerView.adapter = contentAdapter

        retriveContennt()
        return view
    }

    private fun retriveContennt(){
        val contentRef = FirebaseDatabase.getInstance()
            .reference.child("inappropriate_content")

        contentRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    (contentList as ArrayList<inappropriateContent>).clear()

                    for (snapshot in snapshot.children){
                        val adminContent = snapshot.getValue(inappropriateContent::class.java)
                        adminContent?.let {
                            (contentList as ArrayList<inappropriateContent>).add(it)
                        }
                    }

                    Collections.reverse(contentList)
                    contentAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //
            }
        })
    }

}