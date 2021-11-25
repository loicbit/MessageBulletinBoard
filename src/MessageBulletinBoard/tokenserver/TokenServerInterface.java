package MessageBulletinBoard.tokenserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.security.SignatureException;

public interface TokenServerInterface extends Remote {
    int NUMBER_TOKENS_SESSION = 10;
    int REG_PORT = 2001;
    String DEF_PATH= "rmi:token";
    String DIV_TOKEN = "DIVTOKEN";

    byte[] initContact(String name, byte[] publicKey) throws RemoteException;
    byte[] getToken(byte[] id) throws Exception;
    //todo verify also as user
    byte[] getPublicKeySign(byte[] id) throws RemoteException;
}
