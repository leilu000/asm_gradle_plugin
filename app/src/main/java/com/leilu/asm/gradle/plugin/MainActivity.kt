package com.leilu.asm.gradle.plugin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.leilu.asm.gradle.libthread_schedule.annotations.BGThread
import com.leilu.asm.gradle.libthread_schedule.annotations.MainThread

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn1).setOnClickListener {
            println("lambda表达式 实现的 onClick")
        }
        findViewById<Button>(R.id.btn2).setOnClickListener(this)
        findViewById<Button>(R.id.btn3).setOnClickListener(listener)

        println(getName(100))

        val test = Test()
        Thread {
            test.test()
        }.start()
        Thread {
            Log.i("==", test.test("sunny"))
        }.start()
        test.test("sunny", 5)
        test.testInnerClass()
        Test.staticTest(5)
        Log.i("==", Test.staticTest("sunny", 5))
    }

    @BGThread
    override fun onClick(v: View?) {
        println("复写OnClickListener 实现的 onClick:${Thread.currentThread().name}")
    }

    val listener = object : View.OnClickListener {

        @MainThread(delay = 5000)
        override fun onClick(v: View?) {
            println("匿名内部类 实现的 onClick:${Thread.currentThread().name}")
        }
    }

    @BGThread(delay = 1000)
    fun getName(age: Int): String = "getName:$age"


}