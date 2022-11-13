package com.example.firebasesns

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.firebasesns.databinding.ActivityUserBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage



class UserActivity : AppCompatActivity() {
    lateinit var storage: FirebaseStorage
    lateinit var binding: ActivityUserBinding
    private val db: FirebaseFirestore = Firebase.firestore
    val docUserRef = db.collection("user").document("${Firebase.auth.currentUser?.uid}")
    val docPostRef = db.collection("post").document("${Firebase.auth.currentUser?.uid}")
    val REQUEST_IMAGE_CAPTURE = 1

    companion object {
        const val REQUEST_CODE = 1
        const val REQ_GALLERY = 1
    }

    // 갤러리에서 이미지 선택결과를 받고 파일 업로드
    private val imageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if(result.resultCode == RESULT_OK){
            val imageURI = result.data?.data
            imageURI?.let{

                val imageFile = getRealPathFromURI(it)
                val imageName = getRealPathFromNAME(it)
                uploadFile(imageFile, imageName)
            }
        }
    }

    // 갤러리에서 이미지 선택결과를 받고 프로필화면으로 전환
    private val profileResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if(result.resultCode == RESULT_OK){
            val imageURI = result.data?.data
            imageURI?.let{
                binding.profile.setImageURI(imageURI)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Firebase.auth.currentUser ?: finish() // if not authenticated, finish this activity

        storage = Firebase.storage
        val storageRef = storage.reference // reference to root
        val imageRef1 = storageRef.child("${Firebase.auth.currentUser?.uid}/hansung2.png")
        val imageRef2 = storage.getReferenceFromUrl(
            "gs://android-sns-5a992.appspot.com/FfMFYbRfePgRLBQQBxhd3q1DGVM2/hansung2.png"
        )

        // 초기 프로필
        displayImageRef(imageRef1, binding.profile)

        // 나중에 자기 포스트에서 이미지 받아오기
        val postRef1 = storage.getReferenceFromUrl(
            "gs://android-sns-5a992.appspot.com/${Firebase.auth.currentUser?.uid}/post1.png"
        )
        val postRef2 = storage.getReferenceFromUrl(
            "gs://android-sns-5a992.appspot.com/${Firebase.auth.currentUser?.uid}/post2.png"
        )
        val postRef3 = storage.getReferenceFromUrl(
            "gs://android-sns-5a992.appspot.com/${Firebase.auth.currentUser?.uid}/post3.png"
        )
        val postRef4 = storage.getReferenceFromUrl(
            "gs://android-sns-5a992.appspot.com/${Firebase.auth.currentUser?.uid}/post4.png"
        )
        val postRef5 = storage.getReferenceFromUrl(
            "gs://android-sns-5a992.appspot.com/${Firebase.auth.currentUser?.uid}/post5.png"
        )
        val postRef6 = storage.getReferenceFromUrl(
            "gs://android-sns-5a992.appspot.com/${Firebase.auth.currentUser?.uid}/post6.png"
        )

        displayImageRef(postRef1, binding.imageView)
        displayImageRef(postRef2, binding.imageView2)
        displayImageRef(postRef3, binding.imageView3)
        displayImageRef(postRef4, binding.imageView4)
        displayImageRef(postRef5, binding.imageView5)
        displayImageRef(postRef6, binding.imageView6)

        // 개시물수, 친구수 출력
        queryItem()

        // 업로드 버튼
        binding.buttonUpload.setOnClickListener {
            selectGallery()
        }
        // 프로필 변경 버튼
        binding.buttonProfile.setOnClickListener {
            selectGalleryProfile()
        }


    }

    // 개시물 id 가져오기
    fun getRealPathFromURI(uri: Uri): Long {
        var columnIndex = 0
        val proj = arrayOf(MediaStore.Images.ImageColumns._ID)
        val cursor = contentResolver.query(uri, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
        }
        val result = cursor.getLong(columnIndex)
        cursor.close()
        return result
    }

    // 개시물 name 가져오기
    fun getRealPathFromNAME(uri: Uri): String {
        var columnIndex = 0
        val proj = arrayOf(MediaStore.Images.ImageColumns.DISPLAY_NAME)
        val cursor = contentResolver.query(uri, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME)
        }
        val result = cursor.getString(columnIndex)
        cursor.close()
        return result
    }

    //갤러리 호출
    private fun selectGallery(){
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        if(writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), REQ_GALLERY)
        }else{
            val intent = Intent(Intent.ACTION_PICK)

            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )

            imageResult.launch(intent)
        }
    }

    //갤러리 호출 후 프로필사진 변경
    private fun selectGalleryProfile(){
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        if(writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), REQ_GALLERY)
        }else{
            val intent = Intent(Intent.ACTION_PICK)

            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )

            profileResult.launch(intent)
        }
    }


    // 게시물수, 친구수 출력
    private fun queryItem() {
        docUserRef.get()
            .addOnSuccessListener { // it: DocumentSnapshot
                binding.friendNumber.setText(it["index"].toString())
            }.addOnFailureListener {
            }

        docPostRef.get()
            .addOnSuccessListener { // it: DocumentSnapshot
                binding.postNumber.setText(it["index"].toString())
            }.addOnFailureListener {
            }
    }


    // 스토리지에 이미지 업로드
    private fun uploadFile(file_id: Long?, fileName: String?) {
        file_id ?: return
        val imageRef = storage.reference.child("${Firebase.auth.currentUser?.uid}/${fileName}")
        val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, file_id)
        imageRef.putFile(contentUri).addOnCompleteListener {
            if (it.isSuccessful) {
                // upload success
                Snackbar.make(binding.root, "Upload completed.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    // 참고용 나중에 지우기
    private fun listPhotosDialog() {
        storage.reference.child("${Firebase.auth.currentUser?.uid}/").listAll()
            .addOnSuccessListener {
                val itemsString = mutableListOf<String>()
                for (i in it.items) {
                    itemsString.add(i.name)
                }
                AlertDialog.Builder(this)
                    .setTitle("Uploaded Photos")
                    .setItems(itemsString.toTypedArray(), {_, i -> }).show()
            }.addOnFailureListener {

            }
    }

    // 참고용 나중에 지우기
    private fun uploadDialog() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null)

            AlertDialog.Builder(this)
                .setTitle("Choose Photo")
                .setCursor(cursor, { _, i ->
                    cursor?.run {
                        moveToPosition(i)
                        val idIdx = getColumnIndex(MediaStore.Images.ImageColumns._ID)
                        val nameIdx = getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                        uploadFile(getLong(idIdx), getString(nameIdx))
                    }
                }, MediaStore.Images.ImageColumns.DISPLAY_NAME).create().show()
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
        }
    }

    // 스토리지에서 이미지 가져와서 표시
    private fun displayImageRef(imageRef: StorageReference?, view: ImageView) {
        imageRef?.getBytes(Long.MAX_VALUE)?.addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            view.setImageBitmap(bmp)
        }?.addOnFailureListener {
        // Failed to download the image
        }
    }

    // 나중에 지우기
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if ((grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                uploadDialog()
            }
        }
    }

}