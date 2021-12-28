package MessageBulletinBoard.mixednetwork;

import MessageBulletinBoard.bulletinboard.BulletinBoardClient;
import MessageBulletinBoard.bulletinboard.BulletinBoardInterface;
import MessageBulletinBoard.crypto.AsymEncrypt;
import org.apache.commons.lang3.SerializationUtils;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;


public class MixedNetworkServer implements MixedNetworkServerInterface {
    private AsymEncrypt asymEncrypt;
    private HashMap<String, Key> publickeys= new HashMap<>();
    static Registry registry = null;

    private KeyPairGenerator keyPairGen = null;
    private KeyPair pair = null;
    private PrivateKey privKey = null;
    private Signature sign = null;

    private BulletinBoardClient bulletinBoardClient = null;

    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    public MixedNetworkServer() throws Exception {
        this.asymEncrypt = new AsymEncrypt();
        initSignature();
        this.bulletinBoardClient = new BulletinBoardClient();
        System.err.println("Bulletinboard server connected");
    }
    public static void main(String[] args) throws Exception {
        try {
            MixedNetworkServer obj = new MixedNetworkServer();
            MixedNetworkServerInterface stub = (MixedNetworkServerInterface) UnicastRemoteObject.exportObject(obj, 0);

            try{
                registry = LocateRegistry.createRegistry(MixedNetworkServerInterface.REG_PORT);
            }catch(Exception e) {
                registry = LocateRegistry.getRegistry(MixedNetworkServerInterface.REG_PORT);
            }

            String nameReg = MixedNetworkServerInterface.DEF_PATH;
            registry.bind(nameReg, stub);

            System.err.println("Server ready");

            obj.connectBulletinBoard();
        }catch (Exception e) {
            throw new Exception(e);
        }
    }

    @Override
    public byte[] initContact(String name, byte[] publicKey) throws RemoteException {
        Key publicKeyOther = SerializationUtils.deserialize(publicKey);
        this.publickeys.put(name, publicKeyOther);

        return this.asymEncrypt.getPublicKeySer();
    }

    @Override
    public byte[] get(byte[] indexEnc, byte[] tagEnc, byte[] tokenEnc, byte[] nameUserEnc) throws Exception {
        String token = this.asymEncrypt.do_RSADecryption(tokenEnc);

        if(verifyToken(token)){
            String indexStr = this.asymEncrypt.do_RSADecryption(indexEnc);
            String tag = this.asymEncrypt.do_RSADecryption(tagEnc);
            String nameUser = this.asymEncrypt.do_RSADecryption(nameUserEnc);

            int index = Integer.parseInt(indexStr);

            String message = this.bulletinBoardClient.get(index, tag);

            if(message!=null){
                return this.asymEncrypt.do_RSAEncryption(message, this.publickeys.get(nameUser));
            }
            return BulletinBoardInterface.emptyMessage;

        }
        return BulletinBoardInterface.emptyMessage;
    }

    @Override
    public void add(byte[] indexEnc, byte[] valueEnc, byte[] tagEnc, byte[] tokenEnc) throws Exception {
        String indexStr = this.asymEncrypt.do_RSADecryption(indexEnc);
        String value = this.asymEncrypt.do_RSADecryption(valueEnc);
        String tag = this.asymEncrypt.do_RSADecryption(tagEnc);
        String token = this.asymEncrypt.do_RSADecryption(tokenEnc);

        int index = Integer.parseInt(indexStr);

        if(verifyToken(token)){
            this.bulletinBoardClient.add(index,value,tag);
        }
    }

    private void connectBulletinBoard() throws Exception {
        this.bulletinBoardClient = new BulletinBoardClient();
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

    private boolean authenticateUser(String name, String password){
        return true;
    }

    private boolean verifyToken(String token){
        return true;

        //todo verify
        //todo save used tokens and check
        /*
        if(this.usedTokens.contains(token)) return false;

        //this.signature
        return true;*/
    }
}
