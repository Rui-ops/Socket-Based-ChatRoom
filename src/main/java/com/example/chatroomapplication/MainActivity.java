package com.example.chatroomapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText hostEditText = findViewById(R.id.host);
        final EditText portEditText = findViewById(R.id.port);
        final EditText nameEditText = findViewById(R.id.name);
        final Button loginButton = findViewById(R.id.Register);
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("host", hostEditText.getText().toString());
                intent.putExtra("port", portEditText.getText().toString());
                intent.putExtra("username", nameEditText.getText().toString());
                startActivity(intent);
            }
        });
    }
}
