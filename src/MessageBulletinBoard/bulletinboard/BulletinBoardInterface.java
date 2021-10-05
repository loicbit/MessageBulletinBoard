package MessageBulletinBoard.bulletinboard;

import java.rmi.*;

public interface BulletinBoardInterface extends Remote{
    String get(int i, String b) throws RemoteException;
    void add(int index, String value, String tag) throws RemoteException;

    int NUMBER_CELLS = 100;

    //todo replace the param:
    int SEC_PARAM = 8;
    int REG_PORT = 2001;

    String STUB_NAME = "rmi:bulletinboard";
    int securityParam = 8;

    String messageDiv = "DIVMES";
    // Get public values
    // String getPublicKey() throws RemoteException;
    //

    //public void initReceiver(BulletinBoardInterface c)throws RemoteException;
}
