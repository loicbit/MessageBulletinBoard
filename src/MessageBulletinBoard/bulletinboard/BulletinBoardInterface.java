package MessageBulletinBoard.bulletinboard;

import java.rmi.*;

public interface BulletinBoardInterface extends Remote{
    public String get(int i, String b) throws RemoteException;
    public void add(int index, String value, String tag) throws RemoteException;
    //public void initReceiver(BulletinBoardInterface c)throws RemoteException;
}
