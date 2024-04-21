package com.example.rassoonlineapp.Fragments
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewSwitcher
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.AccountSettingsActivity
import com.example.rassoonlineapp.Adapter.PortfolioImageAdapter
import com.example.rassoonlineapp.Adapter.PortfolioImageRetrieveAdapter
import com.example.rassoonlineapp.Adapter.ProposalsStatisticAdapter
import com.example.rassoonlineapp.Adapter.RatingItemAdapter
import com.example.rassoonlineapp.Adapter.ServiceStatisticAdapter
import com.example.rassoonlineapp.Adapter.VideoRetrieveAdapter
import com.example.rassoonlineapp.Model.ProposalsStatistic
import com.example.rassoonlineapp.Model.Rating
import com.example.rassoonlineapp.Model.Statistic
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.RatingActivity
import com.example.rassoonlineapp.SigninActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
class ProfileFragment : Fragment() {
    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var recyclerViewRating: RecyclerView
    private lateinit var ratingAdapter: RatingItemAdapter
    private lateinit var portfolioImageAdapter: PortfolioImageAdapter
    private lateinit var portfolioImageRetrieveAdapter: PortfolioImageRetrieveAdapter
    private lateinit var videoRetrieveAdapter: VideoRetrieveAdapter
    private val ratingList: MutableList<Rating> = mutableListOf()
    private lateinit var  recyclerViewPortfolioImage: RecyclerView
    // Declarar uma instância do ServiceStatisticAdapter
   private lateinit var serviceStatisticAdapter: ServiceStatisticAdapter
   private val  statisticList: MutableList<Statistic> = mutableListOf()
    private lateinit var proposalsStatistic: ProposalsStatisticAdapter
    private val  proposalsStatisticList: MutableList<ProposalsStatistic> = mutableListOf()
    private var user: User? = null
    private var userRatingCount: Int = 0 // Variável para armazenar a quantidade de ratings
    private var userRatingTotal: Double = 0.0 // Variável para armazenar a soma das avaliações



    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val viewSwitcher = view.findViewById<ViewSwitcher>(R.id.view_switcher)
        val scrollView = view.findViewById<ScrollView>(R.id.scroll_view)
        val topBar = view.findViewById<LinearLayout>(R.id.top_bar)

        recyclerViewRating = view.findViewById(R.id.recycler_view_rating)
        recyclerViewRating.layoutManager = LinearLayoutManager(requireContext())
        ratingAdapter = RatingItemAdapter(requireContext(), ratingList)
        recyclerViewRating.adapter = ratingAdapter

        view.findViewById<ImageView>(R.id.options_view).setOnClickListener { view ->
            val popupMenu = PopupMenu(context, view)
            popupMenu.inflate(R.menu.profile_menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_edit_profile -> {
                        // Abrir a atividade de configurações de conta
                        val intent = Intent(context, AccountSettingsActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.menu_logout -> {
                        // Deslogar o usuário
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, SigninActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }


        // Lógica para exibir o layout principal no ViewSwitcher
        view.findViewById<Button>(R.id.button_principal).setOnClickListener {
            viewSwitcher.setDisplayedChild(0)
            hidePortfolioRecyclerView()
            hideServicesRecyclerView()
            hideServicesRecyclerProsalsView()
            showRatingRecyclerView()
        }
        retrieveRating()

        // Lógica para exibir o layout de portfólio no ViewSwitcher
        view.findViewById<Button>(R.id.button_portifolio).setOnClickListener {
            viewSwitcher.setDisplayedChild(1)
            hideServicesRecyclerView()
            hideRatingRecyclerView()
            showPortfolioRecyclerView()
            hideServicesRecyclerProsalsView()

            recyclerViewPortfolioImage = view.findViewById(R.id.recycler_view_portfolioImage)
            recyclerViewPortfolioImage.layoutManager = GridLayoutManager(requireContext(), 3)
            portfolioImageRetrieveAdapter = PortfolioImageRetrieveAdapter(requireContext(), listOf())
            recyclerViewPortfolioImage.adapter = portfolioImageRetrieveAdapter

            retrievePortfolioImages()

            val recyclerViewPortfolioVideo = view.findViewById<RecyclerView>(R.id.recycler_view_portfolioVideo)
            recyclerViewPortfolioVideo.layoutManager = GridLayoutManager(requireContext(), 3)
            videoRetrieveAdapter = VideoRetrieveAdapter(requireContext(), listOf())
            recyclerViewPortfolioVideo.adapter = videoRetrieveAdapter

            retrievePortfolioVideos()

        }
        // Lógica para exibir o layout de serviços no ViewSwitcher
        view.findViewById<Button>(R.id.button_servicos).setOnClickListener {
            viewSwitcher.setDisplayedChild(2)
            hidePortfolioRecyclerView()
            hideRatingRecyclerView()
         showServicesRecyclerView()
            showServicesRecyclerProposalsView()
            // RecyclerView de serviços


            val recyclerViewServices = view.findViewById<RecyclerView>(R.id.recycler_view_services)
            // Configurar o layout manager para a RecyclerView de estatísticas
            recyclerViewServices.layoutManager = LinearLayoutManager(requireContext())
            // Inicializar o adaptador de estatísticas
            serviceStatisticAdapter = ServiceStatisticAdapter(requireContext(), statisticList)
            // Definir o adaptador para a RecyclerView de estatísticas
            recyclerViewServices.adapter = serviceStatisticAdapter

            // Método para recuperar os dados de estatísticas
            retrieveStatistic()



            val recyclerViewServicesProposals = view.findViewById<RecyclerView>(R.id.recycler_view_services_proposals)
            // Configurar o layout manager para a RecyclerView de estatísticas
            recyclerViewServicesProposals.layoutManager = LinearLayoutManager(requireContext())
            // Inicializar o adaptador de estatísticas
            proposalsStatistic = ProposalsStatisticAdapter(requireContext(), proposalsStatisticList)
            // Definir o adaptador para a RecyclerView de estatísticas
            recyclerViewServicesProposals.adapter = proposalsStatistic

            // Método para recuperar os dados de estatísticas
            retrieveStatisticProposals()
        }

        view.findViewById<Button>(R.id.button_assessment).setOnClickListener {
            // Verifica se o perfil que está sendo avaliado é diferente do usuário atual
            if (firebaseUser?.uid != profileId) {
                // Se forem diferentes, inicie a atividade de avaliação
                val intent = Intent(context, RatingActivity::class.java)
                intent.putExtra("profileId", profileId) // Passa o profileId para a RatingActivity
                context?.startActivity(intent)
            } else {
                // Se forem iguais, exiba uma mensagem informando que o usuário não pode se avaliar
                Toast.makeText(context, "Você não pode se avaliar", Toast.LENGTH_SHORT).show()
            }
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId",  firebaseUser.uid) ?: firebaseUser.uid // Padrão para o uid do usuário atual
        }


        // Verificar se o perfil visualizado pertence ao usuário atual
        if (profileId == firebaseUser.uid) {
            view.findViewById<ImageView>(R.id.options_view)
        } else {
            view.findViewById<ImageView>(R.id.options_view).visibility = View.GONE
        }

        userInfo()
        return view
    }


    private fun retrieveStatisticProposals() {
        val statisticProposalsRef = FirebaseDatabase.getInstance().getReference("ProposalStats").child(profileId)

        statisticProposalsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    proposalsStatisticList.clear() // Limpa a lista para evitar duplicatas
                    val proposalStatistic = dataSnapshot.getValue(ProposalsStatistic::class.java)
                    if (proposalStatistic != null) {
                            proposalsStatisticList.add(proposalStatistic)
                            proposalsStatistic.notifyDataSetChanged()
                        }
                // Notifica o adaptador sobre a mudança nos dados
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Database error: ${error.message}")
            }
        })
    }
    private fun retrieveStatistic() {
        val statisticRef = FirebaseDatabase.getInstance().getReference("Statistics").child(profileId)

        statisticRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    statisticList.clear() // Limpa a lista para evitar duplicatas
                    val statistic = dataSnapshot.getValue(Statistic::class.java)
                    if (statistic != null) {
                        statisticList.add(statistic)
                        serviceStatisticAdapter.notifyDataSetChanged() // Notifica o adaptador sobre a mudança nos dados
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Database error: ${error.message}")
            }
        })
    }

    private fun retrieveRating() {
        val ratingsRef = FirebaseDatabase.getInstance().getReference().child("Ratings")

        ratingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    ratingList.clear() // Limpa a lista para evitar duplicatas
                    for (ratingSnapshot in dataSnapshot.children) {
                        val rating = ratingSnapshot.getValue(Rating::class.java)
                        if (rating != null && rating.userIdOther == profileId) { // Verifica se o userIdOther corresponde ao profileId
                            ratingList.add(rating)
                        }
                    }
                    ratingAdapter.notifyDataSetChanged() // Notifica o adaptador sobre a mudança nos dados

                    // Após atualizar a lista de ratings, calcule e atualize a quantidade de ratings
                    calculateUserRatingCount()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Database error: ${error.message}")
            }
        })
    }

    // Método para calcular a quantidade de ratings
    // Método para calcular a quantidade e a média de ratings
    private fun calculateUserRatingCount() {
        userRatingCount = ratingList.size
        userRatingTotal = ratingList.sumByDouble { it.rating }

        val averageRating = if (userRatingCount > 0) userRatingTotal / userRatingCount else 0.0

        updateRatingCountUI(userRatingCount, averageRating)
    }

    // Método para atualizar a UI com a quantidade e a média de ratings
    private fun updateRatingCountUI(count: Int, average: Double) {
        view?.findViewById<RatingBar>(R.id.allRating)?.rating = average.toFloat()
        view?.findViewById<TextView>(R.id.textView_show_rating)?.text = String.format("%.1f", average)
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
            .child(profileId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    val userImage = user?.getImage()
                    if (userImage != null && view != null) {
                        Picasso.get().load(userImage).placeholder(R.drawable.profile).into(view?.findViewById(R.id.pro_image_profile_frag))
                    }


                   // Picasso.get().load(user?.getImage()).placeholder(R.drawable.profile).into(view?.findViewById(R.id.pro_image_profile_frag))

                    view?.findViewById<TextView>(R.id.profile_fragment_username)?.text =
                        user?.getUsername()
                    view?.findViewById<TextView>(R.id.full_name_profile_frag)?.text =
                        user?.getFullname()
                    view?.findViewById<TextView>(R.id.bio_profile_frag)?.text = user?.getBio()
                    view?.findViewById<TextView>(R.id.textView_profile_data)?.text =
                        user?.getDescription()
                    view?.findViewById<TextView>(R.id.function)?.text = user?.getEspecialidade()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Database error: ${error.message}")
            }
        })
    }
    private fun hidePortfolioRecyclerView() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_portfolioImage)?.visibility = View.GONE
        view?.findViewById<RecyclerView>(R.id.recycler_view_portfolioVideo)?.visibility = View.GONE
      //  view?.findViewById<TextView>(R.id.port_video)?.visibility = View.GONE
      //  view?.findViewById<TextView>(R.id.port_fotos)?.visibility = View.GONE

    }
    private fun showPortfolioRecyclerView() {
        // Exibe a RecyclerView de portfólio
        view?.findViewById<RecyclerView>(R.id.recycler_view_portfolioImage)?.visibility = View.VISIBLE
        view?.findViewById<RecyclerView>(R.id.recycler_view_portfolioVideo)?.visibility = View.VISIBLE
        //view?.findViewById<TextView>(R.id.port_video)?.visibility = View.VISIBLE
       // view?.findViewById<TextView>(R.id.port_fotos)?.visibility = View.VISIBLE
    }
    private fun hideServicesRecyclerView() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_services)?.visibility = View.GONE
    }
    private fun showServicesRecyclerView() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_services)?.visibility = View.VISIBLE
    }
    private fun hideServicesRecyclerProsalsView() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_services_proposals)?.visibility = View.GONE
    }
    private fun showServicesRecyclerProposalsView() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_services_proposals)?.visibility = View.VISIBLE
    }
    private fun hideRatingRecyclerView() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_rating)?.visibility = View.GONE
    }
    private fun showRatingRecyclerView() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_rating)?.visibility = View.VISIBLE
    }
    private fun retrievePortfolioImages() {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("portfolio_images").child(profileId)

        storageRef.listAll().addOnSuccessListener { listResult ->
            val imageUrlList = mutableListOf<String>()

            listResult.items.forEachIndexed { _, item ->
                item.downloadUrl.addOnSuccessListener { uri ->
                    imageUrlList.add(uri.toString())

                    // Set the list of image URLs to the adapter
                    portfolioImageRetrieveAdapter.setData(imageUrlList)
                }
            }
        }.addOnFailureListener {
            // Handle any errors
            Toast.makeText(context, "Failed to retrieve portfolio images", Toast.LENGTH_SHORT).show()
        }
    }
    private fun retrievePortfolioVideos() {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("portfolio_videos").child(profileId)

        storageRef.listAll().addOnSuccessListener { listResult ->
            val videoUrlList = mutableListOf<String>()

            listResult.items.forEachIndexed { _, item ->
                item.downloadUrl.addOnSuccessListener { uri ->
                    videoUrlList.add(uri.toString())
                }
            }

            // Set the list of video URLs to the adapter
            videoRetrieveAdapter.setData(videoUrlList)

        }.addOnFailureListener {
            // Handle any errors
            Toast.makeText(context, "Failed to retrieve portfolio videos", Toast.LENGTH_SHORT).show()
        }
    }




}