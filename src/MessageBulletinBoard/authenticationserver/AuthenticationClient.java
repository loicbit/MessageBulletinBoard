package MessageBulletinBoard.authenticationserver;

import MessageBulletinBoard.crypto.AsymEncrypt;
import MessageBulletinBoard.crypto.DiffieH;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.LinkedList;
import java.util.List;

public class AuthenticationClient {
    private AuthenticationServerInterface authenticateServerStub;
    private Registry registry;
    private final String authToken;
    private final DiffieH diffieEncrypt;

    public AuthenticationClient(String authToken, boolean encrypted) throws Exception {
        this.authToken = authToken;
        this.diffieEncrypt = new DiffieH(false);

        try{
            this.registry = LocateRegistry.createRegistry(AuthenticationServerInterface.REG_PORT);
        }catch(Exception ex){
            System.out.println(ex);
        }

        try{
            this.registry = LocateRegistry.getRegistry(AuthenticationServerInterface.REG_PORT);

        }catch(Exception e){
            System.out.println(e);
        }

        try{
            this.authenticateServerStub = (AuthenticationServerInterface) this.registry.lookup(AuthenticationServerInterface.DEF_PATH);
        }catch(Exception e){
            System.out.println(e);
        }

        this.initAuthServer();

    }

    public void initAuthServer() throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        PublicKey publicKeyUserSer = this.diffieEncrypt.getPubkeyObject();
        PublicKey publicKeyOtherSer = this.authenticateServerStub.initSecureChannel(this.authToken, publicKeyUserSer);

        this.diffieEncrypt.generateSecretKeyObject(publicKeyOtherSer);
    }

    public PublicKey getPublicSignKey() throws Exception {
        PublicKey publicKey =  this.authenticateServerStub.getPublicKeySign(this.authToken);

        return publicKey;
    }

    public LinkedList getTokens() throws Exception {
        LinkedList<byte[]> tokensReceived = new LinkedList<>();

        for(int i =0; i<AuthenticationServerInterface.NUMBER_TOKENS_SESSION; i++){
            byte[] response = this.authenticateServerStub.getToken(this.authToken);
            byte[] tokens_array = this.diffieEncrypt.decryptBytes(response);

            tokensReceived.add(tokens_array);
        }

        return tokensReceived;
    }

    public byte[] getTokensSign(String user) throws RemoteException {
        byte[] tokenReceivedEnc =  this.authenticateServerStub.getTokenSign(this.authToken, user);
        return this.diffieEncrypt.decryptBytes(tokenReceivedEnc);
    }
}
