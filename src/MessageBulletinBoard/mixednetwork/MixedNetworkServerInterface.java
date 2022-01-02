package MessageBulletinBoard.mixednetwork;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public interface MixedNetworkServerInterface extends Remote {
    int NUMBER_TOKENS_SESSION = 10;
    int REG_PORT = 2001;
    String DEF_PATH= "rmi:mixed_network";
    String DIV_AUTH = "DIVMIXED";
    String DIV_TOKEN = "DIVTOKEN";
    String algoMD = "SHA-256";

    String DEF_NAME = "MixedServerNr:";

    PublicKey initContact(String name, PublicKey publicKey) throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException;

    byte[] get(byte[] index, byte[] tag,byte[] token, byte[] hashBA,String nameUse) throws Exception;
    void add(byte[] index, byte[] value, byte[] tag, byte[] token, byte[] hashAB, String nameUse) throws Exception;
}
