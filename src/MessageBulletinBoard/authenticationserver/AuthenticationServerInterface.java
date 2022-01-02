package MessageBulletinBoard.authenticationserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public interface AuthenticationServerInterface extends Remote {
    int NUMBER_TOKENS_SESSION = 5;
    int REG_PORT = 2001;
    String DEF_PATH= "rmi:authentication";
    String DIV_AUTH = "DIVAUTH";
    String DIV_TOKEN = "DIVTOKEN";

    String SIGN_INSTANCE = "SHA1withRSA";
    String SIGN_KEY_INSTANCE = "RSA";

    //PublicKey initContact(String name, PublicKey publicKey) throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException;
    PublicKey initSecureChannel(String name, PublicKey publicKey) throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException;

    byte[] getToken(String id) throws Exception;
    byte[] getTokenSign(String serverName, String user) throws RemoteException;

    PublicKey getPublicKeySign(String id) throws Exception;
}
