package com.example.spirulina

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        btnSignIn.setOnClickListener {
            val intent = Intent(this@SignInActivity,SelectionActivity::class.java)
            startActivity(intent)
        }

        btnToSignup.setOnClickListener {
            val intent = Intent(this@SignInActivity,SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
