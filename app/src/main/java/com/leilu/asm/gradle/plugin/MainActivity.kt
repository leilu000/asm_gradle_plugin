package com.leilu.asm.gradle.plugin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn1).setOnClickListener {
            println("lambda表达式 实现的 onClick")
        }
        findViewById<Button>(R.id.btn2).setOnClickListener(this)
        findViewById<Button>(R.id.btn3).setOnClickListener(listener)
    }

    override fun onClick(v: View?) {
        println("复写OnClickListener 实现的 onClick")
    }

    val listener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            println("匿名内部类 实现的 onClick")
        }
    }

}