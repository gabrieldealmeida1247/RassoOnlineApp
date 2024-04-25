package com.example.rassoonlineapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.rassoonlineapp.View.MainActivity
import com.example.rassoonlineapp.View.PaymentActivity
import com.example.rassoonlineapp.View.PortfolioActivity
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.View.ServiceManageActivity

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

        view.findViewById<CardView>(R.id.card_proposals).setOnClickListener {
                val fragment = ProposalsFragment()
                val transition = requireActivity().supportFragmentManager.beginTransaction()

                transition.replace(R.id.fragment_container, fragment)
                transition.addToBackStack(null)
                transition.commit()
        }


        view.findViewById<CardView>(R.id.card_history).setOnClickListener {
            val fragment = HistoryFragment()
            val transition = requireActivity().supportFragmentManager.beginTransaction()

            transition.replace(R.id.fragment_container, fragment)
            transition.addToBackStack(null)
            transition.commit()
        }

        view.findViewById<CardView>(R.id.card_statistic).setOnClickListener {
            val fragment = GraphicFragment()
            val transition = requireActivity().supportFragmentManager.beginTransaction()

            transition.replace(R.id.fragment_container, fragment)
            transition.addToBackStack(null)
            transition.commit()
        }

/*
        view.findViewById<CardView>(R.id.cardService).setOnClickListener {
            val fragment = ManageServiceFragment()
            val transition = requireActivity().supportFragmentManager.beginTransaction()

            transition.replace(R.id.fragment_container, fragment)
            transition.addToBackStack(null)
            transition.commit()
        }


 */
        view.findViewById<CardView>(R.id.cardService).setOnClickListener {
            val intent = Intent(context, ServiceManageActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<CardView>(R.id.cardPortfolio).setOnClickListener {
            val intent = Intent(context, PortfolioActivity::class.java)
            startActivity(intent)
        }


        view.findViewById<CardView>(R.id.card_payment).setOnClickListener {
            val intent = Intent(context, PaymentActivity::class.java)
            startActivity(intent)
        }

        // Other code...

        return view
    }

    // Other methods...
}
