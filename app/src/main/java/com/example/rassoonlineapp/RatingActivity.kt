package com.example.rassoonlineapp
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class RatingActivity : AppCompatActivity() {

    private var firebaseUser: FirebaseUser? = null
    private lateinit var usersReference: DatabaseReference
    private lateinit var ratingBar: RatingBar
    private var profileId: String? = null // Inicialize como nulo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersReference = FirebaseDatabase.getInstance().reference.child("Users")
        val button_submit_rating = findViewById<Button>(R.id.button_submit_rating)
        ratingBar = findViewById(R.id.ratingBar)

        // Obtenha o profileId do intent
        profileId = intent.getStringExtra("profileId")

        button_submit_rating.setOnClickListener {
            sendRatingToDatabase()
        }

        loadUserData()
        setupRatingBarListener()
    }

    private fun loadUserData() {
        usersReference.child(firebaseUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userName = snapshot.child("username").value.toString()
                        val userProfileImageUrl = snapshot.child("image").value.toString()

                        findViewById<TextView>(R.id.text_view_name).text = userName
                        if (userProfileImageUrl.isNotEmpty()) {
                            Picasso.get().load(userProfileImageUrl)
                                .into(findViewById<CircleImageView>(R.id.image_profile_rating))
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle errors here
                }
            })
    }

    private fun setupRatingBarListener() {
        ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            // Aqui você pode adicionar qualquer ação desejada quando a classificação for alterada
            // Por exemplo, você pode exibir a classificação selecionada em um TextView
            // Ou, como neste caso, você pode enviar a classificação para o banco de dados quando o usuário mudar a classificação
            // Neste exemplo, estamos apenas exibindo a classificação selecionada em um Toast
            Toast.makeText(this, "Classificação: $rating", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendRatingToDatabase() {
        val ratingDescription = findViewById<EditText>(R.id.edit_text_rating).text.toString().trim()
        val ratingValue = ratingBar.rating

        if (ratingDescription.isEmpty()) {
            findViewById<EditText>(R.id.edit_text_rating).error = "Por favor, insira uma descrição"
            return
        }

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Ratings")
        val ratingId = databaseReference.push().key

        val ratingMap = HashMap<String, Any>()
        ratingMap["ratingId"] = ratingId!!
        ratingMap["userId"] = firebaseUser!!.uid // Usar o ID do usuário atual
        ratingMap["userIdOther"] = profileId!! // Usar o ID do perfil que está sendo avaliado
        ratingMap["userName"] = findViewById<TextView>(R.id.text_view_name).text.toString()
        ratingMap["rating"] = ratingValue
        ratingMap["description"] = ratingDescription

        // Envie os dados da avaliação para a base de dados de avaliações
        databaseReference.child(ratingId).setValue(ratingMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@RatingActivity,
                        "Avaliação enviada com sucesso",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@RatingActivity,
                        "Erro ao enviar a avaliação",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


}