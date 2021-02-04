package com.example.chatroomapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.content.Intent;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import android.os.Looper;
import android.util.Log;

import java.io.*;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.KeyGenerator;

public class ChatActivity extends AppCompatActivity {

    private Handler mHandler;

    private ClientThread clientThread;

    private Button sendButton;
    private EditText sendText;
    private TextView tvMain;

    private byte[] DESKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        Intent intent = getIntent();
        final String host = intent.getStringExtra("host");
        final String port = intent.getStringExtra("port");
        final String username = intent.getStringExtra("username");

        sendButton = findViewById(R.id.btn_main);
        sendText = findViewById(R.id.et_main);
        tvMain =  findViewById(R.id.tv_main);



        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    tvMain.append("\n" + msg.obj.toString());
                }
            }
        };

        DESKey = getKey();

        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String send_text = username + ": " + sendText.getText().toString();
                try {
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = encrypt(send_text, DESKey);
                    clientThread.revHandler.sendMessage(msg);
                    sendText.setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        clientThread = new ClientThread(host, port, username, mHandler);
        clientThread.start();

    }

    public class ClientThread extends Thread {
        private Socket s = null;
        private PrintStream out = null;
        private BufferedReader indata = null;

        private String ipaddress;
        private String port;
        private String username;

        private Handler mHandler;
        public Handler revHandler;

        public ClientThread(String ip, String prt, String usr, Handler handler){
            this.ipaddress = ip;
            this.port = prt;
            this.username = usr;
            this.mHandler = handler;
        }

        @Override
        public void run(){
            try {
                s = new Socket(ipaddress, Integer.parseInt(port));
                out = new PrintStream(s.getOutputStream());
//                out.println("#username:" + username);
                indata = new BufferedReader(new InputStreamReader(s.getInputStream()));

                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            String back_text;
                            while ((back_text = indata.readLine()) != null) {
                                Message msg = new Message();
                                msg.what = 0;
                                msg.obj = decrypt(back_text, DESKey);
                                mHandler.sendMessage(msg);
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }.start();

                Looper.prepare();
                revHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {

                            try {
                                out.write((msg.obj.toString() + "\r\n").getBytes("utf-8"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                };
                Looper.loop();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String encrypt(String src, byte[] key)
    {
        try {
            java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DES");
            KeySpec keySpec = new DESKeySpec(key);
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new SecureRandom());
            byte[] enMsgBytes = cipher.doFinal(src.getBytes());
            String encryptedMessage = encoder.encodeToString(enMsgBytes);
            return encryptedMessage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unused")
    public String decrypt(String encryptString, byte[] key){
        try {
            java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
            byte[] encryptBytes = decoder.decode(encryptString);
            Cipher deCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            SecretKeyFactory deDecretKeyFactory = SecretKeyFactory.getInstance("DES");
            KeySpec deKeySpec = new DESKeySpec(key);
            SecretKey deSecretKey = deDecretKeyFactory.generateSecret(deKeySpec);
            deCipher.init(Cipher.DECRYPT_MODE, deSecretKey,new SecureRandom());
            byte[] deMsgBytes = deCipher.doFinal(encryptBytes);
            String decryptedMessage = new String(deMsgBytes);
            return decryptedMessage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] getKey(){
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
            keyGenerator.init(56);
            SecretKey generateKey = keyGenerator.generateKey();
            byte[] encoded = generateKey.getEncoded();
            return encoded;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
