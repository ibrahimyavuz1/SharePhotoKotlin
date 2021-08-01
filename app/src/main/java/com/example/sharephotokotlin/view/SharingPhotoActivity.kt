package com.example.sharephotokotlin.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sharephotokotlin.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_sharing_photo.*
import java.util.*

class SharingPhotoActivity : AppCompatActivity() {
    var selectedImage: Uri?=null
    var selectedBitmap: Bitmap?=null
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database:FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sharing_photo)
        storage= FirebaseStorage.getInstance()
        auth= FirebaseAuth.getInstance()
        database= FirebaseFirestore.getInstance()
    }
    fun selectPhoto(view: View){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
        }
        else{
            val galleryIntent= Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent,2)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode==1){
            if(grantResults.size>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                val galleryIntent= Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent,2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==2&&resultCode==Activity.RESULT_OK&&data!=null){
            selectedImage=data.data
            if(selectedImage!=null){
                if(Build.VERSION.SDK_INT>=28){   //This if and else are just for deprecated "getBitmap" function.  (SDK diff)
                    val source=ImageDecoder.createSource(this.contentResolver,selectedImage!!)
                    selectedBitmap=ImageDecoder.decodeBitmap(source)
                    imageView.setImageBitmap(selectedBitmap)
                }
            }else{  //This if and else are just for deprecated "getBitmap" function. (SDK diff)
                selectedBitmap=MediaStore.Images.Media.getBitmap(this.contentResolver,selectedImage)
                imageView.setImageBitmap(selectedBitmap)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    fun share(view:View){
        val uuid= UUID.randomUUID()
        val imageName="${uuid}.jpg"
        val reference=  storage.reference
        val imageReference=reference.child("images").child(imageName)
        if(selectedImage!=null){
            imageReference.putFile(selectedImage!!).addOnSuccessListener {taskSnapshot->
                val uploadedImageReference= FirebaseStorage.getInstance().reference.child("images").child(imageName)
                uploadedImageReference.downloadUrl.addOnSuccessListener { uri->
                    val downloadUrl= uri.toString()
                    val currentUserEmail=auth.currentUser!!.email.toString()
                    val userComment= commentText.text.toString()
                    val time= Timestamp.now()

                    val postHashMap=hashMapOf<String,Any>()
                    postHashMap.put("imageurl",downloadUrl)
                    postHashMap.put("useremail",currentUserEmail)
                    postHashMap.put("usercomment",userComment)
                    postHashMap.put("time",time)

                    database.collection("Post").add(postHashMap).addOnCompleteListener {task->
                        if(task.isSuccessful){
                            finish()
                        }

                    }.addOnFailureListener { exception->
                        Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()

                    }

                }.addOnFailureListener { exception->
                    Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
                }

            }
        }
    }
}