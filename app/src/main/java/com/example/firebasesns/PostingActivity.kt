package com.example.firebasesns

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.firebasesns.databinding.ActivityPostingBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.Timestamp
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap

class PostingActivity : AppCompatActivity()  {
    lateinit var binding: ActivityPostingBinding
    private val db: FirebaseFirestore = Firebase.firestore
    val docPostRef = db.collection("post").document("${Firebase.auth.currentUser?.uid}")

    private lateinit var viewModel: MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Firebase.auth.currentUser ?: finish() // if not authenticated, finish this activity

        viewModel = ViewModelProvider(this)[MyViewModel::class.java]

        binding.posting.setOnClickListener {
            val imgUrl = viewModel.getPos() //1번문제
            val comment = binding.comments.text.toString()
            val like = 0
            val post_id = "${Firebase.auth.currentUser?.uid}"

            val timestamp = LocalDateTime.now() // 2번문제

            binding.postImage.setImageURI(imgUrl)

            val comments = hashMapOf(
                "${Firebase.auth.currentUser?.uid}" to comment,
                "${Firebase.auth.currentUser?.uid}" to comment
            )

            val array: Array<Map<String, String>> = arrayOf(comments)// 3번문제

            val itemMap = hashMapOf(
                "imgUrl" to imgUrl,
                "like" to like,
                "post_id" to post_id,
                "Timestamp" to LocalDateTime.now(),
                "comments" to comments
            )

            docPostRef.set(itemMap)
                .addOnSuccessListener {
                }.addOnFailureListener {
                }


        }
    }
}