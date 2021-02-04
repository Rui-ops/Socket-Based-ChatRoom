/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chat;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class FileThread extends Thread {
//    private int port;

    private Socket client;
    private ServerSocket fileSS;
    private String filename;
    private String username;
//    private DataOutputStream dos = null;

    public FileThread(String username) {
        this.username = username;
    }

    @Override
    public void run() {
        DataOutputStream dos = null;
        try {
            fileSS = new ServerSocket(9091);
            client = fileSS.accept();
            System.out.println("Connected to files from client: " + client.getRemoteSocketAddress().toString());
            DataInputStream dis = new DataInputStream(client.getInputStream());
            filename = dis.readUTF();
            File file = new File("C://KTH/Network Programming/File/" + filename);
            double totleLength = dis.readLong();
            dos = new DataOutputStream(new FileOutputStream(file));
            System.out.println("Receiving Starts. File Size:" + totleLength);
            int length = -1;
            byte[] buff = new byte[1024];
            double curLength = 0;
            while ((length = dis.read(buff)) > 0) {
                dos.write(buff, 0, length);
                curLength += length;
                System.out.println("Process: " + (curLength / totleLength * 100) + "%");
                if (curLength == totleLength) {
                    break;
                }
            }
            dos.flush();
            System.out.println("Receiving finished");
            System.out.println("Received file: " + filename);
            Manager.getServerMangager().broadcast(encrypt("Server: A file is sent from " + username + " to the Server: " + filename, "this is a key".getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                if (client != null) {
                    client.close();
                }
                if (fileSS != null) {
                    fileSS.close();
                }
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
            return encoder.encodeToString(enMsgBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String decrypt(String encryptString, byte[] key){
        try {
            java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
            byte[] encryptBytes;
            encryptBytes = decoder.decode(encryptString);
            Cipher deCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            SecretKeyFactory deDecretKeyFactory = SecretKeyFactory.getInstance("DES");
            KeySpec deKeySpec = new DESKeySpec(key);
            SecretKey deSecretKey = deDecretKeyFactory.generateSecret(deKeySpec);
            deCipher.init(Cipher.DECRYPT_MODE, deSecretKey,new SecureRandom());
            byte[] deMsgBytes = deCipher.doFinal(encryptBytes);
            return new String(deMsgBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return null;
        return "Failed";
    }

}
