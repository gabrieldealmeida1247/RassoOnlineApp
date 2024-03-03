package com.example.rassoonlineapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.rassoonlineapp.MainActivity
import com.example.rassoonlineapp.R

class MenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        // Existing code...

        // Add click listener to cardSearch_user
        view.findViewById<CardView>(R.id.cardSearch_user).setOnClickListener {
            // Call a function in MainActivity to navigate to the search screen
            (activity as MainActivity).navigateToSearchFragment()
        }

        // Other code...

        return view
    }

    // Other methods...
}
