package MessageBulletinBoard.authenticationserver;

import MessageBulletinBoard.crypto.AssymEncrypt;
import org.apache.commons.lang3.SerializationUtils;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class AuthenticationServer implements AuthenticationServerInterface {
    private AssymEncrypt assymEncrypt;
    private HashMap<String, Key> publickeys= new HashMap<>();
    static Registry registry = null;

    private KeyPairGenerator keyPairGen = null;
    private KeyPair pair = null;
    private PrivateKey privKey = null;
    private Signature sign = null;

    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    public AuthenticationServer() throws Exception {
        this.assymEncrypt = new AssymEncrypt();
        initSignature();
    }
    public static void main(String[] args) throws Exception {
        try {
            AuthenticationServer obj = new AuthenticationServer();
            AuthenticationServerInterface stub = (AuthenticationServerInterface) UnicastRemoteObject.exportObject(obj, 0);

            try{
                registry = LocateRegistry.createRegistry(AuthenticationServerInterface.REG_PORT);
            }catch(Exception e) {
                registry = LocateRegistry.getRegistry(AuthenticationServerInterface.REG_PORT);
            }

            String nameReg = AuthenticationServerInterface.DEF_PATH;
            registry.bind(nameReg, stub);


            System.err.println("Server ready");
        }catch (Exception e) {
            throw new Exception(e);
        }
    }

    @Override
    public byte[] initContact(String name, byte[] publicKey) throws RemoteException {
        Key publicKeyOther = SerializationUtils.deserialize(publicKey);
        this.publickeys.put(name, publicKeyOther);

        String out = name + "is connected";
        System.err.println(out);
        return this.assymEncrypt.getPublicKeySer();
    }

    @Override
    public byte[] getToken(byte[] id) throws Exception {

        String idString = this.assymEncrypt.do_RSADecryption(id);

        if(this.publickeys.containsKey(idString)){
            Key keyOther = this.publickeys.get(idString);

            List<byte []> tokens = generateTokens(AuthenticationServerInterface.NUMBER_TOKENS_SESSION);
            String tokensString= "";

            for (byte[] token: tokens) {
                tokensString += Base64.getEncoder().encodeToString(token);
                tokensString += AuthenticationServerInterface.DIV_TOKEN;
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(tokensString);
            byte[] bytes = bos.toByteArray();

            return this.assymEncrypt.do_RSAEncryption(bytes, keyOther);

        }else return null;
    }

    @Override
    public byte[] getPublicKeySign(byte[] id) throws RemoteException {
        //todo encrypt
        //this.assymEncrypt.do_RSAEncryption()

        byte[] publicKeySer = SerializationUtils.serialize(this.pair.getPublic());
        return publicKeySer;
    }

    private void initSignature() throws NoSuchAlgorithmException, InvalidKeyException {
        //Creating KeyPair generator object
        this.keyPairGen = KeyPairGenerator.getInstance("DSA");

        //Initializing the key pair generator
        this.keyPairGen.initialize(2048);

        //Generate the pair of keys
        this.pair = keyPairGen.generateKeyPair();

        //Getting the privatekey from the key pair
        this.privKey = pair.getPrivate();

        //Creating a Signature object
        this.sign = Signature.getInstance("SHA256withDSA");

        this.sign.initSign(this.privKey);
    }

    private List<byte[]> generateTokens(int numberTokens) throws SignatureException {
        LinkedList<byte[]> tokens = new LinkedList<>();

        for(int i=0; i < numberTokens; i++){
            byte[] randomBytes = new byte[24];
            secureRandom.nextBytes(randomBytes);
            this.sign.update(randomBytes);

            tokens.add(this.sign.sign());
        }

        return tokens;
    }

    private boolean authenticateUser(String name, String password){
        return true;
    }
}
