/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chat;

/**
 *
 * @author ritch
 */
import java.io.*;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class ServerThread extends Thread {

    private Socket client;
    private OutputStream ous;
    private String username;
    //private UserInfo user;

    public void out(String out) {
        try {
            client.getOutputStream().write((out + "\n").getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
//        out("You are connected to the server.");
        try {
            byte[] key = "this is a key".getBytes();
            BufferedReader indata
                    = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String text = null;
            while ((text = indata.readLine()) != null) {
                if (text.contains("#username")) {
                    String name = text.split(":")[1];
                    this.username = name;

                } else {
                    if (this.username == null) {
                        out(encrypt("Please enter a username using format like #username:yourusername", key));

                    } else {
                        text = decrypt(text, key);
                        if (text.equals("#filesend")) {
                            FileThread ft = new FileThread(this.username);
                            ft.start();
                        } else if (text.equals("#goodbye")) {
                            System.out.println("Client: " + client.getRemoteSocketAddress().toString() + " left the chatroom.");
                            indata.close();
                            Manager.getServerMangager().remove(this);
                        } else if (text.contains("#filereceive")) {
                            String fname = text.split(":")[1];
                            FileReceiveThread frt = new FileReceiveThread(fname);
                            frt.start();
                        } else if (text.contains("#username")) {
                            String name = text.split(":")[1];
                            this.username = name;
//                        System.out.println("The name is " + name);
                        } else {
                            System.out.println("Message from " + this.username + ":" + text);
                            Manager.getServerMangager().broadcast(encrypt(username + ":  " + text, key));
                        }
                    }
                }
            }
            indata.close();
            Manager.getServerMangager().remove(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ServerThread(Socket client) {
        this.client = client;
    }

    public String getUsername() {
        return this.username;
    }

    public String encrypt(String src, byte[] key) {
        try {
            java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DES");
            KeySpec keySpec = new DESKeySpec(key);
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new SecureRandom());
            byte[] enMsgBytes = cipher.doFinal(src.getBytes());
            return encoder.encodeToString(enMsgBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decrypt(String encryptString, byte[] key) {
        try {
            java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
            byte[] encryptBytes;
            encryptBytes = decoder.decode(encryptString);
            Cipher deCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            SecretKeyFactory deDecretKeyFactory = SecretKeyFactory.getInstance("DES");
            KeySpec deKeySpec = new DESKeySpec(key);
            SecretKey deSecretKey = deDecretKeyFactory.generateSecret(deKeySpec);
            deCipher.init(Cipher.DECRYPT_MODE, deSecretKey, new SecureRandom());
            byte[] deMsgBytes = deCipher.doFinal(encryptBytes);
            return new String(deMsgBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return null;
        return "Failed";
    }

}
