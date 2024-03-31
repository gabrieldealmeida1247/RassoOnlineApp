package com.example.rassoonlineapp.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewSwitcher
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.AccountSettingsActivity
import com.example.rassoonlineapp.Adapter.PortfolioAdapter
import com.example.rassoonlineapp.Adapter.RatingItemAdapter
import com.example.rassoonlineapp.Model.Rating
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.RatingActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var ratingAdapter: RatingItemAdapter
    private lateinit var ratingList: MutableList<Rating>
    private lateinit var recyclerViewRating: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val viewSwitcher = view.findViewById<ViewSwitcher>(R.id.view_switcher)
        val scrollView = view.findViewById<ScrollView>(R.id.scroll_view)
        val topBar = view.findViewById<LinearLayout>(R.id.top_bar)

        // RecyclerView para exibir as avaliações
        recyclerViewRating = view.findViewById<RecyclerView>(R.id.recycler_view_rating)
        recyclerViewRating.layoutManager = LinearLayoutManager(context)

        // Lista de avaliações
        ratingList = mutableListOf()
        ratingAdapter = RatingItemAdapter(requireContext(), ratingList)
        recyclerViewRating.adapter = ratingAdapter

        // Lógica para exibir o layout principal no ViewSwitcher
        view.findViewById<Button>(R.id.button_principal).setOnClickListener {
            viewSwitcher.setDisplayedChild(0)
            showRatingElements()
            showRatingRecyclerView()
            hidePortfolioRecyclerView()
            hideServicesRecyclerView()

            // Carregar as avaliações
            loadRatings()
        }

        // Lógica para exibir o layout de portfólio no ViewSwitcher
        view.findViewById<Button>(R.id.button_portifolio).setOnClickListener {
            viewSwitcher.setDisplayedChild(1)
            hideRatingElements()
            hideServicesRecyclerView()
            showPortfolioRecyclerView()

            // Inflar o layout do item de portfólio diretamente na RecyclerView
            val recyclerViewPortfolio = view.findViewById<RecyclerView>(R.id.recycler_view_portfolio)
            recyclerViewPortfolio.layoutManager = LinearLayoutManager(context)
            recyclerViewPortfolio.adapter = PortfolioAdapter(requireContext())
        }

        // Lógica para exibir o layout de serviços no ViewSwitcher
        view.findViewById<Button>(R.id.button_servicos).setOnClickListener {
            viewSwitcher.setDisplayedChild(2)
            hideRatingElements()
            hidePortfolioRecyclerView()
            showServicesRecyclerView()
        }

        // Lógica para avaliar
        view.findViewById<AppCompatButton>(R.id.button_assessment).setOnClickListener {
            if (profileId == firebaseUser.uid) {
                it.isEnabled = false
                Toast.makeText(context, "Você não pode se avaliar.", Toast.LENGTH_SHORT).show()
            } else {
                // Passa o profileId como um extra para a RatingActivity
                val intent = Intent(context, RatingActivity::class.java)
                intent.putExtra("profileId", profileId)
                startActivity(intent)
            }
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId", "none").toString()
        }

        // Verificar se o perfil visualizado pertence ao usuário atual
        if (profileId == firebaseUser.uid) {
            view.findViewById<Button>(R.id.edit_account_settings_btn).text = "Edit Profile"
        } else {
            view.findViewById<Button>(R.id.edit_account_settings_btn).visibility = View.GONE
        }

        // Lógica para editar o perfil
        view.findViewById<Button>(R.id.edit_account_settings_btn).setOnClickListener {
            val getButtonText = view.findViewById<Button>(R.id.edit_account_settings_btn).text.toString()
            when {
                getButtonText == "Edit Profile" ->  startActivity(Intent(context, AccountSettingsActivity::class.java))
            }
        }

        userInfo()
        return view
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
            .child(profileId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)

                    Picasso.get().load(user?.getImage()).placeholder(R.drawable.profile)
                        .into(view?.findViewById(R.id.pro_image_profile_frag))
                    view?.findViewById<TextView>(R.id.profile_fragment_username)?.text =
                        user?.getUsername()
                    view?.findViewById<TextView>(R.id.full_name_profile_frag)?.text =
                        user?.getFullname()
                    view?.findViewById<TextView>(R.id.bio_profile_frag)?.text = user?.getBio()
                    view?.findViewById<TextView>(R.id.textView_profile_data)?.text =
                        user?.getDescription()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Database error: ${error.message}")
            }
        })

        // Referência para as avaliações na base de dados
        val ratingsRef = FirebaseDatabase.getInstance().getReference().child("Ratings")

        // Listener para recuperar as avaliações da base de dados
        ratingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                ratingList.clear()

                for (ratingSnapshot in dataSnapshot.children) {
                    val rating = ratingSnapshot.getValue(Rating::class.java)
                    if (rating != null && rating.userId == profileId) {
                        ratingList.add(rating)
                    }
                }

                ratingAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Database error: ${error.message}")
            }
        })
    }

    private fun hideRatingElements() {
        view?.findViewById<Button>(R.id.button_assessment)?.visibility = View.GONE
    }

    private fun showRatingElements() {
        view?.findViewById<Button>(R.id.button_assessment)?.visibility = View.VISIBLE
    }

    private fun hidePortfolioRecyclerView() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_portfolio)?.visibility = View.GONE
    }

    private fun showPortfolioRecyclerView() {
        // Oculta os elementos de avaliação
        hideRatingElements()

        // Oculta o RecyclerView de avaliações
        hideRatingRecyclerView()

        // Exibe a RecyclerView de portfólio
        view?.findViewById<RecyclerView>(R.id.recycler_view_portfolio)?.visibility = View.VISIBLE
    }

    private fun hideRatingRecyclerView() {
        // Oculta o RecyclerView de avaliações
        view?.findViewById<RecyclerView>(R.id.recycler_view_rating)?.visibility = View.GONE
    }

    private fun showRatingRecyclerView() {
        // Oculta o RecyclerView de avaliações
        view?.findViewById<RecyclerView>(R.id.recycler_view_rating)?.visibility = View.VISIBLE
    }
    private fun hideServicesRecyclerView() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_services)?.visibility = View.GONE
    }

    private fun showServicesRecyclerView() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_services)?.visibility = View.VISIBLE
    }

    private fun loadRatings() {
        val ratingsRef = FirebaseDatabase.getInstance().getReference().child("Ratings")

        ratingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                ratingList.clear()

                for (ratingSnapshot in dataSnapshot.children) {
                    val rating = ratingSnapshot.getValue(Rating::class.java)
                    if (rating != null && rating.userId == profileId) {
                        ratingList.add(rating)
                    }
                }

                ratingAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Database error: ${error.message}")
            }
        })
    }
}
