package MessageBulletinBoard.mixednetwork;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MixedNetworkServerInterface extends Remote {
    int NUMBER_TOKENS_SESSION = 10;
    int REG_PORT = 2001;
    String DEF_PATH= "rmi:mixed_network";
    String DIV_AUTH = "DIVMIXED";
    String DIV_TOKEN = "DIVTOKEN";
    String algoMD = "SHA-256";

    byte[] initContact(String name, byte[] publicKey) throws RemoteException;

    String get(int i, String b,String token) throws RemoteException;
    boolean add(int index, String value, String tag, String token) throws RemoteException;

    //todo verify also as user
    //byte[] getPublicKeySign(byte[] id) throws RemoteException;
}
