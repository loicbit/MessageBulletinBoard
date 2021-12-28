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

    byte[] get(byte[] index, byte[] tag,byte[] token, byte[] nameUser) throws Exception;
    void add(byte[] index, byte[] value, byte[] tag, byte[] token) throws Exception;

    //todo verify also as user
    //byte[] getPublicKeySign(byte[] id) throws RemoteException;
}
