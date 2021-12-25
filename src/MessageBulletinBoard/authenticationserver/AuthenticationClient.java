package MessageBulletinBoard.authenticationserver;

import MessageBulletinBoard.crypto.AsymEncrypt;
import org.apache.commons.lang3.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Key;
import java.util.List;

public class AuthenticationClient {
    private List<byte []> tokens;
    private AuthenticationServerInterface authenticateServerStub;
    private Registry registry;
    private String nameUser;
    private AsymEncrypt asymEncrypt;
    private Key publicKeyTokenServer;

    public AuthenticationClient(String nameUser) throws Exception {
        this.nameUser = nameUser;

        this.asymEncrypt = new AsymEncrypt();

        try{
            this.registry = LocateRegistry.createRegistry(AuthenticationServerInterface.REG_PORT);
        }catch(Exception ex){
            System.out.println(ex);
        }

        try{
            this.registry = LocateRegistry.getRegistry(AuthenticationServerInterface.REG_PORT);

        }catch(Exception e){
            System.out.println(e);
            //throw new Exception();
        }

        try{
            this.authenticateServerStub = (AuthenticationServerInterface) this.registry.lookup(AuthenticationServerInterface.DEF_PATH);
        }catch(Exception e){
            System.out.println(e);
        }

    }

    public void initAuthServer() throws RemoteException {
        //todo: return boolean if succeed
        byte[] publicKeyUserSer = this.asymEncrypt.getPublicKeySer();

        byte[] publicKeyOtherSer = this.authenticateServerStub.initContact(this.nameUser, publicKeyUserSer);
        this.publicKeyTokenServer = SerializationUtils.deserialize(publicKeyOtherSer);
    }

    public String[] getTokens() throws Exception {
        byte[] name_encrypted =  this.asymEncrypt.do_RSAEncryption(this.nameUser, this.publicKeyTokenServer);
        byte[] response = this.authenticateServerStub.getToken(name_encrypted);
        byte[] tokens_array = this.asymEncrypt.do_RSADecryption_byte(response);

        ByteArrayInputStream arrayStream = new ByteArrayInputStream(tokens_array);
        ObjectInputStream objStream = new ObjectInputStream(arrayStream);

        String[] tokensReceived = ((String) objStream.readObject()).split(AuthenticationServerInterface.DIV_TOKEN);

        // 1: Token for send
        // 2: Token for receiver

        return tokensReceived;
    }

    private void getNewTokens(){

    }

    private boolean verifyTokens(List<byte[]> tokens){
        return false;
    }
}
