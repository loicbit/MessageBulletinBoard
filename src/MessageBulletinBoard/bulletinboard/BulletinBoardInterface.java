package MessageBulletinBoard.bulletinboard;

import java.rmi.*;

public interface BulletinBoardInterface extends Remote{
    String get(int i, String b) throws RemoteException;
    boolean add(int index, String value, String tag, String token) throws RemoteException;

    int NUMBER_CELLS = 100;

    //todo replace the param:
    //todo remove sec parameters from interface
    int SEC_PARAM = 8;
    int REG_PORT = 2001;

    String STUB_NAME = "rmi:bulletinboard";
    int securityParam = 8;

    String messageDiv = "DIVMES";
    String keyDIV = "DIVK";

    String algoMD = "SHA-256";

    //public void initReceiver(BulletinBoardInterface c)throws RemoteException;
}
