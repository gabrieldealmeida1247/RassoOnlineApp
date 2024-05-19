package com.example.rassoonlineapp.Admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.rassoonlineapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FinancialAdminFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var amountAdminTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_financial_admin, container, false)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference.child("adminAmounts").child("admin")

        // Find TextView
        amountAdminTextView = view.findViewById(R.id.amount_admin)

        // Retrieve and display admin amount
        retrieveAdminAmount()

        return view
    }

    private fun retrieveAdminAmount() {
        // Listen for changes to the admin amount in the database
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Retrieve the adminAmount value from the database
                val adminAmount = snapshot.child("adminAmount").getValue(Double::class.java)

                // Update the TextView with the retrieved amount
                adminAmount?.let {
                    amountAdminTextView.text = it.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }
}
