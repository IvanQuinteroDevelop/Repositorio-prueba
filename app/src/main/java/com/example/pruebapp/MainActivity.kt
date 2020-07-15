package com.example.pruebapp

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File


const val REQUEST_IMAGE_CAPTURE = 1
const val FILE_NAME = "photo"
private lateinit var photoFile: File
val storage = Firebase.storage

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab_share.hide()
        button_upload.hide()
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            openCamera()
        }
        findViewById<FloatingActionButton>(R.id.fab_share).setOnClickListener {
            shareFile()
        }
        button_upload.setOnClickListener {
            uploadFile()
        }
    }

    private fun uploadFile() {
        val storageRef = storage.reference
        val baos = ByteArrayOutputStream()
        val data = baos.toByteArray()
        val picturesRef = storageRef.child("$photoFile")
        var uploadTask: UploadTask

        photoFile = getPhotoFile(FILE_NAME)
        val fileProvider =
            FileProvider.getUriForFile(applicationContext,
                "com.example.pruebapp.fileprovider",
                photoFile)
        uploadTask = picturesRef.putFile(fileProvider)

        uploadTask.addOnFailureListener {
            Log.e("message", it.message!!)
            Toast.makeText(this, getString(R.string.fail_upload), Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener {
           it.bytesTransferred
            Toast.makeText(this, getString(R.string.success_upload), Toast.LENGTH_SHORT).show()
        }
    }


    private fun shareFile() {
        val shareIntent: Intent = Intent().apply {
            action = ACTION_SEND
            val fileProvider =
                FileProvider.getUriForFile(applicationContext,
                "com.example.pruebapp.fileprovider",
                photoFile)
            putExtra(Intent.EXTRA_STREAM, fileProvider)
            type = "image/jpg"
        }
        startActivity(Intent.createChooser(shareIntent, "imageToShare"))
    }


    private fun openCamera(){
        photoFile = getPhotoFile(FILE_NAME)
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            val fileProvider = FileProvider.getUriForFile(this, "com.example.pruebapp.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }


    private fun getPhotoFile(fileName: String): File{
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg",  storageDirectory)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            Glide.with(this)
                .load(takenImage)
                .into(myImage)
            if (takenImage!=null){
                fab_share.show()
                button_upload.show()
                tx_upload.visibility = View.VISIBLE
            }
        }
    }
}