package com.example.rassoonlineapp

import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddPostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        val editTextSkills: TextInputEditText = findViewById(R.id.editText_skills)
        val skillsContainer: LinearLayout = findViewById(R.id.skillsContainer)
        val addButton: Button = findViewById(R.id.button_add_skill)

        addButton.setOnClickListener {
            val inputText = editTextSkills.text.toString().trim()

            if (inputText.isNotEmpty()) {
                val textContainer = layoutInflater.inflate(R.layout.shape_container, null) as LinearLayout

                val textElement: TextView = textContainer.findViewById(R.id.text)
                textElement.text = inputText

                val deleteButton: TextView = textContainer.findViewById(R.id.deleteButton)
                deleteButton.setOnClickListener {
                    skillsContainer.removeView(textContainer)
                }

                skillsContainer.addView(textContainer)
                editTextSkills.text?.clear()
            }
        }



        val prazoAutoComplete: AutoCompleteTextView = findViewById(R.id.autoCompletePrazo)
        val prazoOptions = arrayOf("1 dia", "3 dias", "1 semana", "2 semanas", "1 mês")
        val prazoAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, prazoOptions)
        prazoAutoComplete.setAdapter(prazoAdapter)

        prazoAutoComplete.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                prazoAutoComplete.showDropDown()
            }
            return@setOnTouchListener false
        }

        findViewById<ImageView>(R.id.close_add_post_btn).setOnClickListener {
            // Use o FragmentManager para voltar ao ProfileFragment
            val fragmentManager = supportFragmentManager

            // Verifique se há algum fragmento na pilha de fragmentos
            if (fragmentManager.backStackEntryCount > 0) {
                // Se houver fragmentos na pilha, popBackStack() remove o fragmento atual e volta ao anterior
                fragmentManager.popBackStack()
            } else {
                // Se não houver fragmentos na pilha, simplesmente feche a Activity atual
                finish()
            }
        }


        findViewById<Button>(R.id.button_publicar).setOnClickListener {
            createPost()
        }
    }



    private val skillsContainer: LinearLayout by lazy {
        findViewById<LinearLayout>(R.id.skillsContainer)
    }

    private fun createPost() {
        val titulo = findViewById<EditText>(R.id.edit_text_titulo).text.toString()
        val descricao = findViewById<EditText>(R.id.edit_text_description).text.toString()
        val orcamento = findViewById<EditText>(R.id.editTextBudget).text.toString()
        val prazo = findViewById<AutoCompleteTextView>(R.id.autoCompletePrazo).text.toString()
        val habilidadesList = mutableListOf<String>()

        // Iterar sobre as habilidades e adicioná-las à lista
        for (i in 0 until skillsContainer.childCount) {
            val skillContainer = skillsContainer.getChildAt(i) as LinearLayout
            val skillTextView = skillContainer.findViewById<TextView>(R.id.text)
            habilidadesList.add(skillTextView.text.toString())
        }


        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descricao) || TextUtils.isEmpty(
                orcamento
            ) || TextUtils.isEmpty(prazo)
        ) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val tipoTrabalhoRadioGroup: RadioGroup = findViewById(R.id.radioGroupType)
        val selectedTipoTrabalhoId = tipoTrabalhoRadioGroup.checkedRadioButtonId
        val tipoTrabalhoRadioButton: RadioButton = findViewById(selectedTipoTrabalhoId)
        val tipoTrabalho = tipoTrabalhoRadioButton.text.toString()


        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("Posts")
        val postId = databaseReference.push().key

        val postMap = HashMap<String, Any>()

        postMap["postId"] = postId!!
        postMap["habilidades"] = habilidadesList
        postMap["titulo"] = titulo
        postMap["descricao"] = descricao
        postMap["orcamento"] = orcamento
        postMap["prazo"] = prazo
        postMap["tipoTrabalho"] = tipoTrabalho  // Adicionando o tipo de trabalho
        postMap["data_hora"] = getCurrentDateTime()

        databaseReference.push().setValue(postMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Post criado com sucesso", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Erro ao criar o post", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }


}
