/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chat;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;

public class FileReceiveThread extends Thread {

    private Socket client;
    private ServerSocket fileSS;
    private String filename;

    public FileReceiveThread(String filename) {
        this.filename = filename;
    }

    @Override
    public void run() {
        DataInputStream dis = null;
        try {
            fileSS = new ServerSocket(9092);
            client = fileSS.accept();
            System.out.println("Client: " + client.getRemoteSocketAddress().toString() + "connected to file receiving.");
            File file = new File("C://KTH/Network Programming/File/" + filename);
            DataOutputStream dos = new DataOutputStream(client.getOutputStream());
            dis = new DataInputStream(new FileInputStream(file));
            dos.writeUTF(file.getName());
            dos.flush();
            dos.writeLong(file.length());
            dos.flush();
            System.out.println("Start Sending file" + filename + ". Size:" + file.getTotalSpace() + ")");
            int length = -1;
            byte[] buff = new byte[1024];
            while ((length = dis.read(buff)) > 0) {
                dos.write(buff, 0, length);
                dos.flush();
            }
            System.out.println("Sending finished.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (dis != null) {
                    dis.close();
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
}
