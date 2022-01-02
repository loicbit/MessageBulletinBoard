package MessageBulletinBoard.authenticationserver;

import MessageBulletinBoard.crypto.AsymEncrypt;
import MessageBulletinBoard.crypto.DiffieH;
import org.apache.commons.lang3.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
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
    private List<byte []> tokens;
    private AuthenticationServerInterface authenticateServerStub;
    private Registry registry;
    private String authToken;
    private AsymEncrypt asymEncrypt;
    private DiffieH diffieEncrypt;
    private Key publicKeyAuthServer;

    public AuthenticationClient(String authToken, boolean encrypted) throws Exception {
        this.authToken = authToken;

        this.diffieEncrypt = new DiffieH(false);

        /*
        this.asymEncrypt = new AsymEncrypt();

        if(encrypted) this.diffieEncrypt = new DiffieH(false);*/

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

        this.initAuthServer(encrypted);

    }

    public void initAuthServer(boolean encrypted) throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        //todo: return boolean if succeed
        //SerializationUtils.serialize(this.diffieEncrypt.getPubkeyObject());
        PublicKey publicKeyUserSer = this.diffieEncrypt.getPubkeyObject();
        //byte[] publicKeyUserSerByte =  Base64.getDecoder().decode(publicKeyUserSer);

        PublicKey publicKeyOtherSer = this.authenticateServerStub.initMixedServer(this.authToken, publicKeyUserSer);
        //byte[] publicKeyAuthServerSym = SerializationUtils.deserialize(publicKeyOtherSer);

        this.diffieEncrypt.generateSecretKeyObject(publicKeyOtherSer);
        /*

        if(!encrypted){
            /*
            SerializationUtils.serialize(this.diffieEncrypt.getPubKey());
            byte[] publicKeyUserSer = this.diffieEncrypt.getPubKeyByte();
            //byte[] publicKeyUserSerByte =  Base64.getDecoder().decode(publicKeyUserSer);

            byte[] publicKeyOtherSer = this.authenticateServerStub.initMixedServer(this.authToken, publicKeyUserSer);
            //byte[] publicKeyAuthServerSym = SerializationUtils.deserialize(publicKeyOtherSer);

            this.diffieEncrypt.generateSecretKeyByte(publicKeyOtherSer);*/
            //todo generate secret
        /*}else{
            byte[] publicKeyUserSer = this.asymEncrypt.getPublicKeySer();

            byte[] publicKeyOtherSer = this.authenticateServerStub.initContact(this.authToken, publicKeyUserSer);
            this.publicKeyAuthServer = SerializationUtils.deserialize(publicKeyOtherSer);
        }*/

    }

    public PublicKey getPublicSignKeyNoEnc() throws Exception {
        PublicKey publicKey =  this.authenticateServerStub.getPublicKeySignNoEnc(this.authToken);

        return publicKey;
    }
    public PublicKey getPublicSignKey() throws Exception {
        byte[] authTokenEnc = this.diffieEncrypt.encryptBytes(this.authToken.getBytes(StandardCharsets.UTF_8));

        PublicKey publicKey =  this.authenticateServerStub.getPublicKeySign(authTokenEnc);

        /*
        byte[] publicKeyEnc =  this.authenticateServerStub.getPublicKeySign(authTokenEnc);

        byte[]  publicKeydec = this.diffieEncrypt.decryptBytes(publicKeyEnc);
        PublicKey publicKey = SerializationUtils.deserialize(publicKeydec);*/

        //byte[] authTokenEnc = this.asymEncrypt.do_RSAEncryption(this.authToken, this.publicKeyAuthServer);
        //byte[] publicKeyEnc =  this.authenticateServerStub.getPublicKeySign(authTokenEnc);

        //byte[] publicKeydec = this.asymEncrypt.decryptionToByte(publicKeyEnc);
        //PublicKey publicKey = SerializationUtils.deserialize(publicKeydec);
        return publicKey;
    }

    public LinkedList getTokens() throws Exception {
        LinkedList<byte[]> tokensReceived = new LinkedList<>();

        for(int i =0; i<AuthenticationServerInterface.NUMBER_TOKENS_SESSION; i++){
            byte[] response = this.authenticateServerStub.getToken(this.authToken);
            byte[] tokens_array = this.diffieEncrypt.decryptBytes(response);

            tokensReceived.add(tokens_array);
        }



        //byte[] name_encrypted =  this.diffieEncrypt.encryptBytes(this.authToken.getBytes());
        //byte[] response = this.authenticateServerStub.getToken(this.authToken);
        //byte[] tokens_array = this.diffieEncrypt.decryptBytes(response);

        /*
        byte[] name_encrypted =  this.asymEncrypt.do_RSAEncryption(this.authToken, this.publicKeyAuthServer);
        byte[] response = this.authenticateServerStub.getToken(name_encrypted);
        byte[] tokens_array = this.asymEncrypt.decryptionToByte(response);
         */

        //ByteArrayInputStream arrayStream = new ByteArrayInputStream(tokens_array);
        //ObjectInputStream objStream = new ObjectInputStream(arrayStream);

        //String[] tokensReceived = ((String) objStream.readObject()).split(AuthenticationServerInterface.DIV_TOKEN);

        //String[] tokensReceived = new String(tokens_array).split(AuthenticationServerInterface.DIV_TOKEN);

        // 1: Token for send
        // 2: Token for receiver

        return tokensReceived;
    }

    //todo lambda
    public byte[] getTokensSign(String user) throws RemoteException {
        byte[] tokenReceivedEnc =  this.authenticateServerStub.getTokenSign(this.authToken, user);
        return this.diffieEncrypt.decryptBytes(tokenReceivedEnc);
    }
    private void getNewTokens(){

    }

}
