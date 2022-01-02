package MessageBulletinBoard.authenticationserver;

import MessageBulletinBoard.bulletinboard.BulletinBoardInterface;
import MessageBulletinBoard.crypto.AsymEncrypt;
import MessageBulletinBoard.bulletinboard.BulletinBoardClient;
import MessageBulletinBoard.crypto.DiffieH;
import MessageBulletinBoard.data.INFO_MESSAGE;
import MessageBulletinBoard.mixednetwork.MixedNetworkClient;
import MessageBulletinBoard.mixednetwork.MixedNetworkServerInterface;
import org.apache.commons.lang3.SerializationUtils;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;


public class AuthenticationServer implements AuthenticationServerInterface {
    private final HashMap<String, DiffieH> diffieEncrypt;
    private final LinkedList<String> mixedServerNames;
    private final HashMap<String, Queue<byte[]>> tokensClients;
    static Registry registry = null;

    private KeyPairGenerator keyPairGen = null;
    private KeyPair pair = null;
    private PrivateKey privKey = null;
    private Signature sign = null;

    private SecureRandom secureRandom = null; //threadsafe

    public AuthenticationServer() throws Exception {
        this.secureRandom = new SecureRandom();
        this.mixedServerNames = new LinkedList<>();
        this.tokensClients = new HashMap<>();

        this.diffieEncrypt = new HashMap<>();
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
            System.out.println("Authentication Server failed: " + e);
        }
    }

    @Override
    public PublicKey initSecureChannel(String name, PublicKey publicKeyOther) throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        if(name.contains(MixedNetworkServerInterface.DEF_NAME)){
            this.mixedServerNames.add(name);
        }
        this.diffieEncrypt.put(name, new DiffieH(false));
        this.diffieEncrypt.get(name).generateSecretKeyObject(publicKeyOther);
        this.tokensClients.put(name, new LinkedList<>());

        String out = name + "is connected";
        System.err.println(out);
        return this.diffieEncrypt.get(name).getPubkeyObject();
    }

    @Override
    public byte[] getToken(String name) throws Exception {

        //String idString = this.asymEncrypt.decryptionToString(id);

        if(this.diffieEncrypt.containsKey(name)){
            //Key keyOther = this.diffieEncrypt.get(idString);


            byte[] token = generateToken();
            this.sign.update(token);
            byte[] tokenSigned= this.sign.sign();

            //Send the valid tokens to every mixedserver

            this.tokensClients.get(name).add(token);
            return this.diffieEncrypt.get(name).encryptBytes(tokenSigned);
            //return this.asymEncrypt.do_RSAEncryption(bytes, keyOther);

        }else return BulletinBoardInterface.emptyMessage;
    }

    @Override
    public byte[] getTokenSign(String serverName, String user){
        byte[] token = this.tokensClients.get(user).poll();

        if(token== null){
             token = INFO_MESSAGE.NO_TOKENS_AIV.name().getBytes();
        }
        return this.diffieEncrypt.get(serverName).encryptBytes(token);
    }

    @Override
    public PublicKey getPublicKeySign(String autToken){
        return this.pair.getPublic();
    }

    private void initSignature() throws NoSuchAlgorithmException, InvalidKeyException {
        //Creating KeyPair generator object
        this.keyPairGen = KeyPairGenerator.getInstance(AuthenticationServerInterface.SIGN_KEY_INSTANCE);

        //Initializing the key pair generator
        this.keyPairGen.initialize(2048);

        //Generate the pair of keys
        this.pair = keyPairGen.generateKeyPair();

        //Getting the privatekey from the key pair
        this.privKey = pair.getPrivate();

        //Creating a Signature object
        this.sign = Signature.getInstance(AuthenticationServerInterface.SIGN_INSTANCE);

        this.sign.initSign(this.privKey);
    }

    private byte[] generateToken() throws SignatureException, InvalidKeyException {
        this.sign.initSign(this.privKey);
        byte[] randomBytes = new byte[4];
        secureRandom.nextBytes(randomBytes);
        return randomBytes;
    }

    /*
    private List<byte[]> generateTokens(int numberTokens) throws SignatureException {
        LinkedList<byte[]> tokens = new LinkedList<>();

        for(int i=0; i < numberTokens; i++){
            byte[] randomBytes = new byte[4];
            secureRandom.nextBytes(randomBytes);
            this.sign.update(randomBytes);

            tokens.add(this.sign.sign());
        }
        return tokens;
    }*/

    private boolean authenticateUser(String name, String password){
        return true;
    }
}
