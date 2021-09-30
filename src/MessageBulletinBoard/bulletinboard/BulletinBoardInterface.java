package MessageBulletinBoard.bulletinboard;

import java.rmi.*;

public interface BulletinBoardInterface extends Remote{
    String get(int i, String b) throws RemoteException;
    void add(int index, String value, String tag) throws RemoteException;

    // Get public values
    // String getPublicKey() throws RemoteException;
    //

    //public void initReceiver(BulletinBoardInterface c)throws RemoteException;
}
