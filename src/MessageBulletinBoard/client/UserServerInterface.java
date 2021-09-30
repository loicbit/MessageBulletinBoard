package MessageBulletinBoard.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserServerInterface extends Remote {
    String initContact(String name, String publicKey) throws RemoteException;
    int getFirstCell(String name) throws RemoteException;
}
