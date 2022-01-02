package MessageBulletinBoard.bulletinboard;

import java.rmi.*;

public interface BulletinBoardInterface extends Remote{
    byte[] get(byte[] i, byte[] b, byte[] authToken) throws Exception;
    void add(byte[] index, byte[] value, byte[] tag) throws Exception;
    byte[] initMixedServer(String authToken, byte[] pubKey) throws RemoteException;
    int NUMBER_CELLS = 100;

    int REG_PORT = 2001;

    String STUB_NAME = "rmi:bulletinboard";
    String DEF_NAME = "BulletinBoardNr:";

    //todo check use secure param
    int TAG_LENGTH = 8;

    String messageDiv = "DIVMES";
    String keyDIV = "DIVK";

    String algoMD = "SHA-256";
    byte [] emptyMessage = new byte[1];
}

