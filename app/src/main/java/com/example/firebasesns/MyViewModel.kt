package com.example.firebasesns

import android.net.Uri
import androidx.lifecycle.ViewModel

class MyViewModel : ViewModel() {
    var postImgUrl: Uri? = null

    fun setPos(ImgUrl: Uri?){
        postImgUrl = ImgUrl
    }

    fun getPos() : Uri?{
        return postImgUrl
    }
}