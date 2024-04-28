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
import com.example.rassoonlineapp.Adapter.NotificationAdapter
import com.example.rassoonlineapp.Admin.adapter.AdminNotificationAdapter
import com.example.rassoonlineapp.Admin.model.AdminNotification
import com.example.rassoonlineapp.Model.Notification
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
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
 * Use the [NotificationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotificationsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var notificationList: List<Notification>? = null
    private var notificationAdapter: NotificationAdapter? = null
    private var adminNotificationList: List<AdminNotification>? = null
    private var adminNotificationAdapter: AdminNotificationAdapter? = null

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
        val  view = inflater.inflate(R.layout.fragment_notifications, container, false)

        val viewSwitcher = view.findViewById<ViewSwitcher>(R.id.view_switcher_notifications)
        var recyclerView: RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_notifications)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        viewSwitcher.post { viewSwitcher.setDisplayedChild(0) }

        view.findViewById<Button>(R.id.users_notification).setOnClickListener {
            // Lógica para exibir o layout principal no ViewSwitcher
            viewSwitcher.setDisplayedChild(0)
           showUsersNotifications()
            hideAdminNotifications()
        }

        view.findViewById<Button>(R.id.admin_notifications).setOnClickListener {
            // Lógica para exibir o layout principal no ViewSwitcher
            viewSwitcher.setDisplayedChild(1)
            showAdminNotifications()
            hideUsersNotifications()

            var recyclerViewAdmin: RecyclerView
            recyclerViewAdmin = view.findViewById(R.id.recycler_view_admin_notifications)
            recyclerViewAdmin?.setHasFixedSize(true)
            recyclerViewAdmin?.layoutManager = LinearLayoutManager(context)

            adminNotificationList = ArrayList()

            adminNotificationAdapter = AdminNotificationAdapter(requireContext(), adminNotificationList as ArrayList<AdminNotification>)
            recyclerViewAdmin.adapter = adminNotificationAdapter

            retriveAdminNotifications()

        }
        notificationList = ArrayList()

        notificationAdapter = NotificationAdapter(requireContext(), notificationList as ArrayList<Notification>)
        recyclerView.adapter = notificationAdapter

        readNotifications()
        return view
    }

    private fun readNotifications() {
        val notiRef = FirebaseDatabase.getInstance()
            .reference.child("Notifications")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

        notiRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        (notificationList as ArrayList<Notification>).clear()

                        for (snapshot in snapshot.children){
                            val notification = snapshot.getValue(Notification::class.java)

                            (notificationList as ArrayList<Notification>).add(notification!!)
                        }

                        Collections.reverse(notificationList)
                        notificationAdapter!!.notifyDataSetChanged()
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
    }


    private fun retriveAdminNotifications() {
        // Fetch admin notifications from the database
        val adminNotificationRef = FirebaseDatabase.getInstance()
            .reference.child("notifications")

        adminNotificationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (adminNotificationList as ArrayList<AdminNotification>).clear()

                    for (snapshot in snapshot.children) {
                        val adminNotification = snapshot.getValue(AdminNotification::class.java)
                        adminNotification?.let {
                            (adminNotificationList as ArrayList<AdminNotification>).add(it)
                        }
                    }

                    Collections.reverse(adminNotificationList)
                    adminNotificationAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors that occur during the data retrieval
            }
        })
    }
    private fun hideUsersNotifications() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_notifications)?.visibility = View.GONE
    }

    private fun showUsersNotifications() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_notifications)?.visibility = View.VISIBLE
    }

    private fun hideAdminNotifications() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_admin_notifications)?.visibility = View.GONE
    }

    private fun showAdminNotifications() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_admin_notifications)?.visibility = View.VISIBLE
    }
}