package com.example.rassoonlineapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.rassoonlineapp.Model.Transacao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream

class PaymentActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private var firebaseUser: FirebaseUser? = null
    private var suaChave: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        database = FirebaseDatabase.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser  // Adicione esta linha

        val etSuaChave = findViewById<EditText>(R.id.etSuaChave)
        val etChaveDestinatario = findViewById<EditText>(R.id.etChaveDestinatario)
        val etValor = findViewById<EditText>(R.id.etValor)
        val btnGerarChave = findViewById<Button>(R.id.btnGerarChave)
        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        val btnHistorico = findViewById<Button>(R.id.btnHistorico)


        // Verificar se a chave já foi gerada e preencher o campo
        suaChave = gerarChaveSeNecessario()
        etSuaChave.setText(suaChave)
        btnGerarChave.setOnClickListener {
            // Aqui não é mais necessário gerar uma nova chave, pois já foi feito
            Toast.makeText(this, "Chave já gerada", Toast.LENGTH_SHORT).show()
        }

        btnEnviar.setOnClickListener {
            val suaChave = etSuaChave.text.toString()
            val chaveDestinatario = etChaveDestinatario.text.toString()
            val valorText = etValor.text.toString()

            val valor = valorText.toDoubleOrNull()

            if (suaChave.isNotEmpty() && chaveDestinatario.isNotEmpty() && valor != null) {
                val userId = firebaseUser?.uid ?: "" // Substitua pelo ID do usuário atual
                transferirDinheiro(userId, suaChave, chaveDestinatario, valor)
            } else {
                Toast.makeText(this, "Preencha os campos corretamente", Toast.LENGTH_SHORT).show()

                if (valor == null) {
                    Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show()
                }
            }
        }



        btnHistorico.setOnClickListener {
            val suaChave = etSuaChave.text.toString()
            val chaveDestinatario = etChaveDestinatario.text.toString()

            if (suaChave.isNotEmpty() && chaveDestinatario.isNotEmpty()) {
                val intent = Intent(this, HistoryPaymentActivity::class.java)
                intent.putExtra("suaChave", suaChave)
                intent.putExtra("destinatario", chaveDestinatario) // Passa o ID do destinatário
                startActivity(intent)
            } else {
                Toast.makeText(this, "Preencha os campos corretamente", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun gerarChave(): String {
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val chave = StringBuilder()

        for (i in 0 until 10) {
            val index = (caracteres.indices).random()
            chave.append(caracteres[index])
        }

        return chave.toString()
    }


    private fun transferirDinheiro(
        userId: String,
        suaChave: String,
        chaveDestinatario: String,
        valor: Double
    ) {
        val transacao = Transacao(userId, suaChave, chaveDestinatario, valor.toString())

        val ref = database.reference.child("transacoes").push()

        ref.setValue(transacao).addOnSuccessListener {
            atualizarSaldos(userId, suaChave, chaveDestinatario, valor)
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao realizar a transferência", Toast.LENGTH_SHORT).show()
        }
    }

    private fun atualizarSaldos(
        userId: String,
        suaChave: String,
        chaveDestinatario: String,
        valor: Double
    ) {
        val suaRef = database.reference.child("saldos").child(userId).child(suaChave)
        val destinatarioRef = database.reference.child("saldos").child(chaveDestinatario)

        destinatarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val saldoDestinatario = snapshot.getValue(Double::class.java) ?: 0.0
                val novoSaldoDestinatario = saldoDestinatario + valor
                destinatarioRef.setValue(novoSaldoDestinatario) // Remova a conversão para String

                suaRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val saldoSuaChave = snapshot.getValue(Double::class.java) ?: 0.0
                        val novoSaldoSuaChave = saldoSuaChave - valor
                        suaRef.setValue(novoSaldoSuaChave) // Remova a conversão para String

                        Toast.makeText(
                            applicationContext,
                            "Transferência realizada com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }


    private fun gerarHistorico(suaChave: String) {
        val ref = database.reference.child("transacoes")

        ref.orderByChild("remetente").equalTo(suaChave)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bitmap = gerarBitmapHistorico(snapshot)
                    if (bitmap != null) {
                        salvarImagem(bitmap)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun gerarBitmapHistorico(snapshot: DataSnapshot): Bitmap? {
        val width = 800
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Definindo o Paint para o texto
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isAntiAlias = true
        }

        var yPos = 50f
        snapshot.children.forEach { child ->
            val transacao = child.getValue(Transacao::class.java)
            val text =
                "De: ${transacao?.remetente}\nPara: ${transacao?.destinatario}\nValor: ${transacao?.valor}\n\n"

            canvas.drawText(text, 50f, yPos, paint)
            yPos += 100f
        }

        return bitmap
    }


    private fun salvarImagem(bitmap: Bitmap) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
            return
        }

        val dir = Environment.getExternalStorageDirectory()
        val file = File(dir, "historico_transacao.png")

        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            Toast.makeText(this, "Imagem salva com sucesso", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


    private fun gerarChaveSeNecessario(): String {
        val userId = firebaseUser?.uid ?: ""

        // Verificar se o nó "chave" existe para o usuário atual
        val chaveRef = database.reference.child("Users").child(userId).child("chave")

        chaveRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Se a chave existir no Firebase, recuperamos ela
                    suaChave = snapshot.value.toString()
                    val etSuaChave = findViewById<EditText>(R.id.etSuaChave)
                    etSuaChave.setText(suaChave)
                } else {
                    // Se a chave não existir, geramos uma nova e a salvamos no Firebase
                    suaChave = gerarChave()

                    // Salvar a chave no Firebase
                    chaveRef.setValue(suaChave).addOnSuccessListener {
                        // Atualizar o campo de texto após salvar no Firebase
                        val etSuaChave = findViewById<EditText>(R.id.etSuaChave)
                        etSuaChave.setText(suaChave)
                    }.addOnFailureListener {
                        Toast.makeText(
                            applicationContext,
                            "Erro ao salvar chave no Firebase",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Toast.makeText(applicationContext, "Erro ao recuperar chave", Toast.LENGTH_SHORT)
                    .show()
            }
        })
        return suaChave?: ""
    }




}
