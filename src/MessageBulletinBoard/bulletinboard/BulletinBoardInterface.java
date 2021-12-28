package MessageBulletinBoard.bulletinboard;

import java.rmi.*;

public interface BulletinBoardInterface extends Remote{
    byte[] get(byte[] i, byte[] b, byte[] authToken) throws Exception;
    void add(byte[] index, byte[] value, byte[] tag) throws RemoteException, Exception;
    byte[] initMixedServer(String authToken, byte[] pubKey) throws RemoteException;
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
    byte [] emptyMessage = new byte[1];
}

