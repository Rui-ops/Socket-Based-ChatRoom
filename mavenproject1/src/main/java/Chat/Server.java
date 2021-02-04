package Chat;

/**
 *
 * @author ritch
 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws Exception{
        Server ser = new Server();
        ser.SetupServer(9090);
    }
    
    public void SetupServer(int port) throws Exception{
        ServerSocket server = new ServerSocket(port);
        System.out.println("Server established! Port:"+port);
        while(true){
            Socket s = server.accept();
            System.out.println("One client connected:"+s.getRemoteSocketAddress().toString());
            ServerThread st = new ServerThread(s);
            Manager.getServerMangager().add(st);
            st.start();
        }
    }
}
