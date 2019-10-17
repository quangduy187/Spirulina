package com.example.spirulina

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btnToSignIn.setOnClickListener {
            val intent = Intent(this@SignUpActivity,SignInActivity::class.java)
            startActivity(intent)
        }
    }
}
