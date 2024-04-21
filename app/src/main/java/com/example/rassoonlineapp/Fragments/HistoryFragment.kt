package com.example.rassoonlineapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ViewSwitcher
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Adapter.HistoryPostAdapter
import com.example.rassoonlineapp.Adapter.HistoryServiceClientAdapter
import com.example.rassoonlineapp.Adapter.HistoryServiceWorkerAdapter
import com.example.rassoonlineapp.Model.History
import com.example.rassoonlineapp.Model.ManageServiceHistory
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryFragment : Fragment() {

    private var manageServiceHistoryList: MutableList<ManageServiceHistory> = mutableListOf()
    private var manageServiceHistoryAdapter: HistoryServiceClientAdapter? = null
    private var manageServiceWorkerHistoryAdapter: HistoryServiceWorkerAdapter? = null
    private var manageServiceHistoryRef: DatabaseReference? = null

    private var historyAdapter: HistoryPostAdapter? = null
    private var historyList: MutableList<History> = mutableListOf()
    private var firebaseUser: FirebaseUser? = null
    private var historyRef: DatabaseReference? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        val viewSwitcher = view.findViewById<ViewSwitcher>(R.id.view_switcher_history)

        historyRef = FirebaseDatabase.getInstance().reference.child("History") // Inicialize a referência do Firebase
        viewSwitcher.post { viewSwitcher.setDisplayedChild(0) }

        view.findViewById<Button>(R.id.button_posts_history).setOnClickListener {
            // Lógica para exibir o layout principal no ViewSwitcher
            viewSwitcher.setDisplayedChild(0)
            showHistoryPosts()
            hideAcceptHistory()
            hideRejectHistory()
            hideSevicesClientHistory()
            hideSevicesWorkerHistory()
        }


        view.findViewById<Button>(R.id.button_serviceclient_history).setOnClickListener {
            // Lógica para exibir o layout principal no ViewSwitcher
            viewSwitcher.setDisplayedChild(0)
            hideHistoryPosts()
            hideAcceptHistory()
            hideRejectHistory()
            showServicesClientHistory()
            hideSevicesWorkerHistory()


            manageServiceHistoryRef = FirebaseDatabase.getInstance().reference.child("ManageServiceHistory") // Inicialize a referência do Firebase para ManageServiceHistory

            manageServiceHistoryAdapter = HistoryServiceClientAdapter(requireContext(), manageServiceHistoryList)
            val manageServiceRecyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_concluide_serviceclient_history)
            manageServiceRecyclerView.adapter = manageServiceHistoryAdapter
            manageServiceRecyclerView.layoutManager = LinearLayoutManager(context)

            retrieveManageServiceHistory()

        }


        view.findViewById<Button>(R.id.button_serviceworker_history).setOnClickListener {
            viewSwitcher.setDisplayedChild(1)
            hideHistoryPosts()
            hideAcceptHistory()
            hideRejectHistory()
            hideSevicesClientHistory()
            showServicesWorkerHistory()


            manageServiceHistoryRef = FirebaseDatabase.getInstance().reference.child("ManageServiceHistory") // Inicialize a referência do Firebase para ManageServiceHistory

            manageServiceWorkerHistoryAdapter = HistoryServiceWorkerAdapter(requireContext(), manageServiceHistoryList)
            val manageServiceRecyclerView = view?.findViewById<RecyclerView>(R.id.recycler_view_concluide_serviceworker_history)
            manageServiceRecyclerView?.adapter = manageServiceWorkerHistoryAdapter
            manageServiceRecyclerView?.layoutManager = LinearLayoutManager(context)

            retrieveManageServiceWorkerHistory()
        }


        historyAdapter = HistoryPostAdapter(historyList!!)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_posts_hiistory)
        recyclerView.adapter = historyAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        retrievePostsHistory()


        return view
    }

    private fun retrievePostsHistory() {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = firebaseUser?.uid

        historyRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                historyList?.clear()

                for (postSnapshot in snapshot.children) {
                    val history = postSnapshot.getValue(History::class.java)
                    history?.let {
                        if (it.userId == currentUserId){
                            historyList?.add(it)
                        }

                    }
                }

                historyAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }


    private fun retrieveManageServiceHistory() {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = firebaseUser?.uid

        manageServiceHistoryRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                manageServiceHistoryList.clear()

                for (postSnapshot in snapshot.children) {
                    val manageServiceHistory = postSnapshot.getValue(ManageServiceHistory::class.java)
                    manageServiceHistory?.let {
                        if (it.userIdOther ==  currentUserId)
                        manageServiceHistoryList.add(it)
                    }

                }

                manageServiceHistoryAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }


    private fun retrieveManageServiceWorkerHistory() {

        firebaseUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = firebaseUser?.uid

        manageServiceHistoryRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                manageServiceHistoryList.clear()

                for (postSnapshot in snapshot.children) {
                    val manageServiceHistory = postSnapshot.getValue(ManageServiceHistory::class.java)
                    manageServiceHistory?.let {
                        if (it.userId == currentUserId) {
                            manageServiceHistoryList.add(it)
                        }
                    }
                }

                manageServiceWorkerHistoryAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }



    private fun hideHistoryPosts() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_posts_hiistory)?.visibility = View.GONE
    }
    private fun showHistoryPosts() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_posts_hiistory)?.visibility = View.VISIBLE
    }
    private fun hideSevicesClientHistory() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_concluide_serviceclient_history)?.visibility = View.GONE
    }
    private fun showServicesClientHistory() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_concluide_serviceclient_history)?.visibility = View.VISIBLE
    }

    private fun hideSevicesWorkerHistory() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_concluide_serviceworker_history)?.visibility = View.GONE
    }
    private fun showServicesWorkerHistory() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_concluide_serviceworker_history)?.visibility = View.VISIBLE
    }



    private fun hideAcceptHistory() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_accept_history)?.visibility = View.GONE
    }
    private fun showAcceptHistory() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_accept_history)?.visibility = View.VISIBLE
    }


    private fun hideRejectHistory() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_rejected_history)?.visibility = View.GONE
    }
    private fun showRejectHistory() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_rejected_history)?.visibility = View.VISIBLE
    }

}