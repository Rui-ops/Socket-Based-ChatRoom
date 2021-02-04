package Chat;

import java.io.*;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class ChatClient1 extends Thread {

    Socket s = null;

    public ChatClient1(String host, int port) {
        try {
            s = new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        new listenMessage().start();
        try {
            PrintStream out = new PrintStream(s.getOutputStream());
            BufferedReader indata = new BufferedReader(new InputStreamReader(System.in));
            String send_text;
            System.out.println("Choose options: 1.Send message; 2.Send file; 3.Receive file; 4.exit. ");
            while ((send_text = indata.readLine()) != null) {
                if (send_text.contains("1")) {
                    System.out.println("Enter text to send: ");
                    while ((send_text = indata.readLine()) != null) {
                        if (send_text.contains("#username")){
                            out.println(send_text);
                        } else {
                            out.println(encrypt(send_text, "this is a key".getBytes()));
                            break;
                        }
                    }
                } else if (send_text.contains("2")) {
                    System.out.println("Enter the path of the file to send: ");
                    out.println(encrypt("#filesend", "this is a key".getBytes()));
                    while ((send_text = indata.readLine()) != null) {
                        FileSend fs = new FileSend(send_text);
                        fs.start();
                        break;
                    }
                } else if (send_text.contains("3")) {
                    String fname;
                    System.out.println("Enter the name of the file you want to receive: ");
                    fname = indata.readLine();
                    out.println(encrypt("#filereceive:" + fname, "this is a key".getBytes()));
                    System.out.println("Enter the path where you want to save the file: ");
                    while ((send_text = indata.readLine()) != null) {
                        FileReceive fr = new FileReceive(send_text);
                        fr.start();
                        break;
                    }
//                    out.println("#filerequest");
                } else if (send_text.contains("4")) {
                    System.out.println("Goodbye!");
                    out.println(encrypt("#goodbye", "this is a key".getBytes()));
                    System.exit(0);
                } else {
                    System.out.println("Please choose one option");
                }
                System.out.println("Choose options: 1.Send message; 2.Send file; 3.Receive file; 4.exit. ");
//                out.println(send_text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        ChatClient1 clientTest = new ChatClient1("localhost", 9090);
        clientTest.start();
    }

    class listenMessage extends Thread {

        @Override
        public void run() {
            try {
                BufferedReader indata
                        = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String back_text;
                while ((back_text = indata.readLine()) != null) {
                    back_text = decrypt(back_text, "this is a key".getBytes());
                    System.out.println("Received: " + back_text);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class FileSend extends Thread {

        private String file;

        public FileSend(String file) {
            this.file = file;
        }

        @Override
        public void run() {
            DataInputStream dis = null;
            try {
                File f = new File(file);
                Socket fs = new Socket("localhost", 9091);
                System.out.println("Connected to File Port.");
                DataOutputStream dos = new DataOutputStream(fs.getOutputStream());
                dis = new DataInputStream(new FileInputStream(f));
                dos.writeUTF(f.getName());
                dos.flush();
                dos.writeLong(f.length());
                dos.flush();
                System.out.println("Start Sending file. Size:" + f.getTotalSpace() + ")");
                int length = -1;
                byte[] buff = new byte[1024];

                while ((length = dis.read(buff)) > 0) {
                    dos.write(buff, 0, length);
                    dos.flush();
                }
                System.out.println("Sending finished.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class FileReceive extends Thread {

        private String path;

        public FileReceive(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            Socket s = null;
            DataOutputStream dos = null;
            try {
                s = new Socket("localhost", 9092);
                System.out.println("You are connected to the file receiving service.");
                DataInputStream dis = new DataInputStream(s.getInputStream());
                File file = new File(path + dis.readUTF());
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
//                System.out.println("Received file: " + filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
