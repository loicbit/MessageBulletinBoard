package MessageBulletinBoard.authenticationserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuthenticationServerInterface extends Remote {
    int NUMBER_TOKENS_SESSION = 2;
    int REG_PORT = 2001;
    String DEF_PATH= "rmi:authentication";
    String DIV_AUTH = "DIVAUTH";
    String DIV_TOKEN = "DIVTOKEN";

    byte[] initContact(String name, byte[] publicKey) throws RemoteException;
    byte[] getToken(byte[] id) throws Exception;
    //todo verify also as user
    byte[] getPublicKeySign(byte[] id) throws RemoteException;
}
