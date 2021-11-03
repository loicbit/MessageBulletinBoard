package MessageBulletinBoard.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserServerInterface extends Remote {
    byte[] initContact(String name, byte[] publicKey) throws RemoteException;
    byte[] getFirstCell(byte[] firstCellBA) throws Exception;

    String DIV_CELL = "DIVFCELL";
    int REG_PORT = 2001;
    String DEF_PATH_USER = "rmi:user/";
}
