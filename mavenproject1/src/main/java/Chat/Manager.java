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
import java.util.Vector;

public class Manager {

    private Manager() {
    }
    ;
    
    private static final Manager m = new Manager();

    public static Manager getServerMangager() {
        return m;
    }

    Vector<ServerThread> vector = new Vector<ServerThread>();

    //add new client
    public void add(ServerThread st) {
        vector.add(st);
    }

    //remove client
    public void remove(ServerThread st) {
        vector.remove(st);
    }

    //broadcast message
    public void broadcast(String out) {
        for (int i = 0; i < vector.size(); i++) {

            ServerThread stSocket = vector.get(i);
            stSocket.out(out);

        }
    }

}
