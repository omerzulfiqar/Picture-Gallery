package edu.vt.cs.cs5254.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity

class PhotoPageActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_page)

        val manager = supportFragmentManager
        val currentFragment = manager.findFragmentById(R.id.fragment_container)

        if(currentFragment == null){
            val fragment = PhotoPageFragment.newInstance(intent.data!!)
            manager.beginTransaction().add(R.id.fragment_container, fragment).commit()
        }
    }

    companion object{
        fun newIntent(context: Context, photoPageUri: Uri) : Intent{
            return Intent(context, PhotoPageActivity::class.java).apply {
                data = photoPageUri
            }
        }
    }
}