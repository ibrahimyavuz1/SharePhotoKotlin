package com.example.sharephotokotlin.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharephotokotlin.FeedRecyclerAdapter
import com.example.sharephotokotlin.Post
import com.example.sharephotokotlin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_feed.*

class FeedActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database:FirebaseFirestore
    var postList= ArrayList<Post>()
    private lateinit var recyclerViewAdapter:FeedRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        auth= FirebaseAuth.getInstance()
        database= FirebaseFirestore.getInstance()
        getDatas()
        var layoutManager= LinearLayoutManager(this)
        recyclerView.layoutManager=layoutManager
        recyclerViewAdapter= FeedRecyclerAdapter(postList)
        recyclerView.adapter=recyclerViewAdapter

    }
    fun getDatas(){
       database.collection("Post").orderBy("time",Query.Direction.DESCENDING).addSnapshotListener { snapshot, exception ->
           if(exception!=null){
               Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
           }
           else{
               if(snapshot!=null){
                   if(!snapshot.isEmpty){
                       val documents= snapshot.documents
                       postList.clear()
                       for(document in documents){
                           val userEmail=document.get("useremail") as String
                           val userComment= document.get("usercomment") as String
                           val imageUrl= document.get("imageurl")as String
                           postList.add(Post(userEmail,userComment,imageUrl))
                       }
                       recyclerViewAdapter.notifyDataSetChanged()
                   }
               }
           }
       }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater=menuInflater
        menuInflater.inflate(R.menu.options_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId== R.id.sharePhoto){
            val intent= Intent(this, SharingPhotoActivity::class.java)
            startActivity(intent)
        }
        else if(item.itemId== R.id.signOut){
            auth.signOut()
            val intent= Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}