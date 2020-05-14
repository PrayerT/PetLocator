package com.example.petnav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class select extends Activity {
    Button button1, button2,button3;
    String usrname;
    TextView welcometext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select);
        Intent intent = getIntent();
        usrname = intent.getStringExtra("name");
        button1 = (Button) findViewById(R.id.goLoc);
        button1.setOnClickListener(new select.button1_click());
        button2 = (Button) findViewById(R.id.goFence);
        button2.setOnClickListener(new select.button2_click());
        button3 = (Button) findViewById(R.id.showinfo);
        button3.setOnClickListener(new select.button3_click());
        welcometext = findViewById(R.id.welcome);
        welcometext.setText("Welcome"+" "+usrname);

    }

    class button1_click implements View.OnClickListener {
        Intent intent = new Intent();
        @Override
        public void onClick(View v) {
            Log.i("none","正在跳转页面...");
            intent.setClass(select.this, Nav.class);
            select.this.startActivity(intent);
        }
    }

    class button2_click implements View.OnClickListener {
        Intent intent = new Intent();
        @Override
        public void onClick(View v) {
            Log.i("none","正在跳转页面...");
            intent.setClass(select.this, Fence.class);
            select.this.startActivity(intent);
        }
    }
    class button3_click implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }
    }
}

