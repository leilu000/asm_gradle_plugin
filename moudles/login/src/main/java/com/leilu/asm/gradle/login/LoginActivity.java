package com.leilu.asm.gradle.login;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.leilu.asm.gradle.libthread_schedule.annotations.BGThread;
import com.leilu.asm.gradle.libthread_schedule.annotations.MainThread;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @BGThread
            @Override
            public void onClick(View v) {
                Log.i("==", "匿名内部类onClick,id:" + v.getId() + "  thread:" + Thread.currentThread().getName());
            }
        });
        findViewById(R.id.btn2).setOnClickListener(this);
    }


    @MainThread(delay = 5000)
    @Override
    public void onClick(View v) {
        Log.i("==", "成员方法onClick,id:" + v.getId() + "  thread:" + Thread.currentThread().getName());
    }
}
