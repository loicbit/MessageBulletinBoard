package MessageBulletinBoard.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserServerInterface extends Remote {
    String initContact(String name, String publicKey) throws RemoteException;
    String getFirstCell(String name, String cellToReceive) throws RemoteException;

    int REG_PORT = 2001;
    String DEF_PATH_USER = "rmi:user/";
}
