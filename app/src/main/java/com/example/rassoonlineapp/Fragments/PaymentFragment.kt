package com.example.rassoonlineapp.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.Model.transferAmount
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PaymentFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var editText_send_money: EditText
    private lateinit var editText_username: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        editText_send_money = view.findViewById(R.id.edit_send_amount)
        editText_username = view.findViewById<EditText?>(R.id.edit_text_username)
/*
        val sendButton = view.findViewById<Button>(R.id.send)
        sendButton.setOnClickListener {

        val userName = editText_username.text.toString()


            val amountToSubtract = editText_send_money.text.toString().toDoubleOrNull()
            if (amountToSubtract != null) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    subtractAmount(userId, amountToSubtract)
                } else {
                    Log.e("PaymentFragment", "Current user ID is null")
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a valid amount to subtract", Toast.LENGTH_SHORT).show()
            }
        }

 */
/*
        val sendButton = view.findViewById<Button>(R.id.send)
        sendButton.setOnClickListener {
            val userName = editText_username.text.toString()
            val amountToSubtract = editText_send_money.text.toString().toDoubleOrNull()
            if (amountToSubtract != null) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    subtractAmount(userId, amountToSubtract)
                    addAmountToUser(userName, amountToSubtract)
                } else {
                    Log.e("PaymentFragment", "Current user ID is null")
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a valid amount to subtract", Toast.LENGTH_SHORT).show()
            }
        }

 */

        val sendButton = view.findViewById<Button>(R.id.send)
        sendButton.setOnClickListener {
            val userName = editText_username.text.toString()
            val amountToSubtract = editText_send_money.text.toString().toDoubleOrNull()

            // Verifica se o nome de usuário não está vazio
            if (userName.isNotBlank()) {
                if (amountToSubtract != null) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        subtractAmount(userId, amountToSubtract)
                        addAmountToUser(userName, amountToSubtract)
                    } else {
                        Log.e("PaymentFragment", "Current user ID is null")
                    }
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid amount to subtract", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a username", Toast.LENGTH_SHORT).show()
            }
        }



        return view
    }

    /*
    private fun subtractAmount(userId: String, amountToSubtract: Double) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("transferAmounts")

        databaseReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val transferData = childSnapshot.getValue(transferAmount::class.java)
                        if (transferData != null) {
                            val currentAmount = transferData.amount
                            val newAmount = currentAmount - amountToSubtract
                            childSnapshot.ref.child("amount").setValue(newAmount)
                                .addOnSuccessListener {
                                    // Atualize o textViewAmount com o novo saldo, se necessário
                                }
                                .addOnFailureListener { e ->
                                    Log.e("PaymentFragment", "Failed to update amount in the database: ${e.message}")
                                }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "No transfer amount found for the current user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("PaymentFragment", "Database query cancelled: ${databaseError.message}")
            }
        })
    }

     */



    private fun subtractAmount(userId: String, amountToSubtract: Double) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("transferAmounts")

        databaseReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val transferData = childSnapshot.getValue(transferAmount::class.java)
                        if (transferData != null) {
                            val currentAmount = transferData.amount
                            val newAmount = currentAmount - amountToSubtract
                            childSnapshot.ref.child("amount").setValue(newAmount)
                                .addOnSuccessListener {
                                    // Atualize o textViewAmount com o novo saldo, se necessário
                                }
                                .addOnFailureListener { e ->
                                    Log.e("PaymentFragment", "Failed to update amount in the database: ${e.message}")
                                }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "No transfer amount found for the current user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("PaymentFragment", "Database query cancelled: ${databaseError.message}")
            }
        })
    }

    private fun addAmountToUser(userName: String, amountToAdd: Double) {
        findUserIdByName(userName) { userId ->
            if (userId != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("transferAmounts")
                databaseReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (childSnapshot in dataSnapshot.children) {
                                val transferData = childSnapshot.getValue(transferAmount::class.java)
                                if (transferData != null) {
                                    val currentAmount = transferData.amount
                                    val newAmount = currentAmount + amountToAdd
                                    childSnapshot.ref.child("amount").setValue(newAmount)
                                        .addOnSuccessListener {
                                            // Atualize o textViewAmount com o novo saldo, se necessário
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("PaymentFragment", "Failed to update amount in the database: ${e.message}")
                                        }
                                }
                            }
                        } else {
                            // Se o usuário não tiver um valor registrado, crie um novo
                            val transferId = databaseReference.push().key ?: ""
                            if (transferId.isNotEmpty()) {
                                val transferData = transferAmount(transferId, userId, amountToAdd)
                                databaseReference.child(transferId).setValue(transferData)
                                    .addOnSuccessListener {
                                        Log.d("PaymentFragment", "Transfer saved successfully to the database")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("PaymentFragment", "Failed to save transfer to the database: ${e.message}")
                                    }
                            } else {
                                Log.e("PaymentFragment", "Failed to generate unique transfer ID")
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("PaymentFragment", "Database query cancelled: ${databaseError.message}")
                    }
                })
            } else {
                Toast.makeText(requireContext(), "User with username $userName not found", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun findUserIdByName(userName: String, callback: (String?) -> Unit) {
        val usersRef = database.child("Users")
        val query = usersRef.orderByChild("username").equalTo(userName)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val user = snapshot.getValue(User::class.java)
                        // Assuming each username is unique, so we only get the first user found
                        callback(user?.getUID())
                        return
                    }
                }
                callback(null) // User not found
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                callback(null)
            }
        })
    }
}
