package com.example.petnav;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    Button button1, button2;
    EditText usrnameET, passwordET;
    static String usrname, password, pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = (Button) findViewById(R.id.reset);
        button2 = (Button) findViewById(R.id.goselect);
        button1.setOnClickListener(new button1_click());
        button2.setOnClickListener(new button2_click());
        usrnameET = (EditText) findViewById(R.id.usrname);
        passwordET = (EditText) findViewById(R.id.password);
    }

    class button1_click implements OnClickListener {
        @Override
        public void onClick(View v) {
            usrnameET.setText("");
            passwordET.setText("");

        }
    }

    class button2_click implements OnClickListener {
        Intent intent = new Intent();

        @Override
        public void onClick(View v) {
            usrname = usrnameET.getText().toString();
            password = passwordET.getText().toString();
            Log.i("none", "usrname:" + usrname);
            Log.i("none", "password:" + password);
            while (pwd == null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket = new Socket("121.41.104.6", 6172);

                            OutputStream sendData = socket.getOutputStream();
                            sendData.write((usrname + "\r\n").getBytes("utf-8"));
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                            pwd = bufferedReader.readLine();

                            sendData.close();
                            bufferedReader.close();
                            socket.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }

            if (password.equals(pwd)) {
                Log.i("none", "正在跳转页面...");
                Toast toast = Toast.makeText(getApplicationContext(),"正在跳转页面",Toast.LENGTH_SHORT);
                toast.show();
                intent.putExtra("name", usrname + "");
                intent.setClass(MainActivity.this, select.class);
                MainActivity.this.startActivity(intent);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),"密码错误！",Toast.LENGTH_SHORT);
                toast.show();
                Log.i("none", "两次密码不一致，请重新设置！");
            }
        }
    }
}

