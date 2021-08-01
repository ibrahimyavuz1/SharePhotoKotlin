package com.example.sharephotokotlin.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.sharephotokotlin.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth= FirebaseAuth.getInstance()
        val currentUser= auth.currentUser
        if(currentUser!=null){
            val intent=Intent(this, FeedActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    fun signIn(view: View){
        auth.signInWithEmailAndPassword(emailText.text.toString(),passwordText.text.toString()).addOnCompleteListener { task->
            if(task.isSuccessful){
                val currentUser=auth.currentUser?.email.toString()
                Toast.makeText(this,"Hoşgeldin: ${currentUser}",Toast.LENGTH_LONG).show()
                val intent=Intent(this, FeedActivity::class.java )
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener { exception->
            Toast.makeText(this,exception.localizedMessage,Toast.LENGTH_LONG).show()

        }


    }
    fun signUp(view: View){
        val email= emailText.text.toString()
        val password= passwordText.text.toString()
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task->
            if(task.isSuccessful){
                val intent= Intent(this, FeedActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener { exception->
            Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
        }
    }
}