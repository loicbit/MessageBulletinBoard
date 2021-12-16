package MessageBulletinBoard.mixednetwork;

import MessageBulletinBoard.crypto.AssymEncrypt;
import org.apache.commons.lang3.SerializationUtils;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class MixedNetworkServer implements MixedNetworkServerInterface {
    private AssymEncrypt assymEncrypt;
    private HashMap<String, Key> publickeys= new HashMap<>();
    static Registry registry = null;

    private KeyPairGenerator keyPairGen = null;
    private KeyPair pair = null;
    private PrivateKey privKey = null;
    private Signature sign = null;

    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    public MixedNetworkServer() throws Exception {
        this.assymEncrypt = new AssymEncrypt();
        initSignature();
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
        }catch (Exception e) {
            throw new Exception(e);
        }
    }

    @Override
    public byte[] initContact(String name, byte[] publicKey) throws RemoteException {
        Key publicKeyOther = SerializationUtils.deserialize(publicKey);
        this.publickeys.put(name, publicKeyOther);

        return this.assymEncrypt.getPublicKeySer();
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
}
