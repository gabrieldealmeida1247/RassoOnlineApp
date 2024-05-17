package com.example.rassoonlineapp.View

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rassoonlineapp.Model.Proposals
import com.example.rassoonlineapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditProposalsActivity : AppCompatActivity() {

    private lateinit var descricaoEditText: EditText
    private lateinit var lanceEditText: EditText
    private lateinit var numberDaysEditText: EditText
    private lateinit var updateButton: Button

    private lateinit var databaseReference: DatabaseReference
    private lateinit var currentProposalId: String
    private var currentProposal: Proposals? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_proposals)

        // Initialize EditTexts and Button
        descricaoEditText = findViewById(R.id.descricaoEditText)
        lanceEditText = findViewById(R.id.lanceEditText)
        numberDaysEditText = findViewById(R.id.numberDaysEditText)
        updateButton = findViewById(R.id.updateButton)

        // Get current proposal ID
        currentProposalId = intent.getStringExtra("proposalId") ?: ""

        // Initialize DatabaseReference
        databaseReference = FirebaseDatabase.getInstance().reference.child("Proposals").child(currentProposalId)

        // Retrieve Proposal data from Firebase Realtime Database
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentProposal = dataSnapshot.getValue(Proposals::class.java)

                    // Fill the edit fields with retrieved Proposal values
                    currentProposal?.let {
                        descricaoEditText.setText(it.descricao)
                        lanceEditText.setText(it.lance)
                        numberDaysEditText.setText(it.numberDays)
                    }
                } else {
                    // Proposal data does not exist
                    Toast.makeText(this@EditProposalsActivity, "Proposal data does not exist", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors if necessary
            }
        })

        // Set click listener for update button
        updateButton.setOnClickListener {
            updateProposal()
        }
    }

    // Method to update Proposal data in Firebase Realtime Database
    private fun updateProposal() {
        val descricao = descricaoEditText.text.toString()
        val lance = lanceEditText.text.toString()
        val numberDays = numberDaysEditText.text.toString()

        // Update data in Firebase Realtime Database for the current proposal
        currentProposal?.apply {
            this.descricao = descricao
            this.lance = lance
            this.numberDays = numberDays
        }

        databaseReference.setValue(currentProposal)
            .addOnSuccessListener {
                // Data updated successfully
                Toast.makeText(this@EditProposalsActivity, "Proposal updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                // Handle errors if necessary
                Toast.makeText(this@EditProposalsActivity, "Error updating proposal: " + e.message, Toast.LENGTH_SHORT).show()
            }
    }
}
