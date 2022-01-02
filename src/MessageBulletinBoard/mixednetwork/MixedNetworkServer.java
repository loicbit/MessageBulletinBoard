package MessageBulletinBoard.mixednetwork;

import MessageBulletinBoard.authenticationserver.AuthenticationClient;
import MessageBulletinBoard.authenticationserver.AuthenticationServerInterface;
import MessageBulletinBoard.bulletinboard.BulletinBoardClient;
import MessageBulletinBoard.bulletinboard.BulletinBoardInterface;
import MessageBulletinBoard.crypto.AsymEncrypt;
import MessageBulletinBoard.crypto.DiffieH;
import MessageBulletinBoard.data.CellLocationPair;
import MessageBulletinBoard.data.INFO_MESSAGE;
import org.apache.commons.lang3.SerializationUtils;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;


public class MixedNetworkServer implements MixedNetworkServerInterface {
    private AsymEncrypt asymEncrypt;
    private HashMap<String, Key> publickeys= new HashMap<>();
    static Registry registry = null;

    private KeyPairGenerator keyPairGen = null;
    private KeyPair pair = null;
    private PrivateKey privKey = null;
    private Signature sign = null;
    private Key publicKeyAuthServer = null;
    private PublicKey publicKeySignature = null;
    private AuthenticationClient authenticationClient = null;
    //private MessageDigest md = null;

    private BulletinBoardClient bulletinBoardClient = null;

    private HashMap<String, byte[]> cellStates = new HashMap<>();
    private HashMap<String,LinkedList<byte[]>> validTokens = new HashMap<>();

    private HashMap<String, DiffieH> diffieEncrypt = new HashMap<>();

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public MixedNetworkServer() throws Exception {
        String authToken = "";

        this.asymEncrypt = new AsymEncrypt();
        this.bulletinBoardClient = new BulletinBoardClient();
        System.err.println("Bulletinboard server connected");
        this.authenticationClient = new AuthenticationClient(authToken, false);
        System.err.println("Authentication server connected");

        this.publicKeySignature = this.authenticationClient.getPublicSignKeyNoEnc();
        if(this.publicKeySignature != null){
            this.initVerifyToken();
            System.err.println("Got publicKey for verification tokens");
        }


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
    public PublicKey initContact(String name, PublicKey publicKeyOther) throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        /*Key publicKeyOther = SerializationUtils.deserialize(publicKey);
        this.publickeys.put(name, publicKeyOther);

        return this.asymEncrypt.getPublicKeySer();*/

        this.validTokens.put(name, new LinkedList<>());
        this.diffieEncrypt.put(name, new DiffieH(false));
        this.diffieEncrypt.get(name).generateSecretKeyObject(publicKeyOther);
        return this.diffieEncrypt.get(name).getPubkeyObject();
    }

    @Override
    public byte[] get(byte[] indexEnc, byte[] tagEnc, byte[] tokenEnc, byte[] hashEnc, String nameUser) throws Exception {
        byte[] token = this.diffieEncrypt.get(nameUser).decryptBytes(tokenEnc);

        if(verifyToken(token, nameUser)){
            String indexStr = new String(this.diffieEncrypt.get(nameUser).decryptBytes(indexEnc));
            String tag = new String(this.diffieEncrypt.get(nameUser).decryptBytes(tagEnc));

            byte[] stateHash = this.diffieEncrypt.get(nameUser).decryptBytes(hashEnc);
            int index = Integer.parseInt(indexStr);

            if(verifyState(stateHash, index, tag)){
                String message = this.bulletinBoardClient.get(index, tag);

                if(message!=null){
                    return this.diffieEncrypt.get(nameUser).encryptBytes(message.getBytes());
                }
                //todo return enum
            }else{
                // todo send message bad state
                return BulletinBoardInterface.emptyMessage;
            }
        }
        return BulletinBoardInterface.emptyMessage;
    }

    @Override
    public void add(byte[] indexEnc, byte[] valueEnc, byte[] tagEnc, byte[] tokenEnc, byte[] hashEnc, String nameUser) throws Exception {
        String indexStr = new String(this.diffieEncrypt.get(nameUser).decryptBytes(indexEnc));
        String value = new String(this.diffieEncrypt.get(nameUser).decryptBytes(valueEnc));
        String tag = new String(this.diffieEncrypt.get(nameUser).decryptBytes(tagEnc));
        byte[] token = this.diffieEncrypt.get(nameUser).decryptBytes(tokenEnc);

        byte[] stateHash = this.diffieEncrypt.get(nameUser).decryptBytes(hashEnc);
        int index = Integer.parseInt(indexStr);

        if(verifyToken(token, nameUser)){
            this.addState(stateHash, index, tag);
            this.bulletinBoardClient.add(index,value,tag);
        }
    }

    // todo find usage
    private void connectBulletinBoard() throws Exception {
        this.bulletinBoardClient = new BulletinBoardClient();
    }


    private void initVerifyToken() throws NoSuchAlgorithmException, InvalidKeyException {
        this.sign = Signature.getInstance(AuthenticationServerInterface.SIGN_INSTANCE);
        this.sign.initVerify(this.publicKeySignature);
    }

    private boolean verifyToken(byte[] tokenSigned, String user) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException, RemoteException {
        //return true;

        Boolean valid = false;

        // todo return info message
        if(this.publicKeySignature==null){
            //todo get public key
            return false;
        }

        boolean allRecev= false;

        while(!allRecev){
            byte[] token = this.authenticationClient.getTokensSign(user);

            if(new String(token).equals(INFO_MESSAGE.NO_TOKENS_AIV.name())) allRecev = true;
            else {
                this.validTokens.get(user).add(token);
            }
        }

        //check if all tokens received
        if(this.validTokens.get(user).isEmpty()){
            //todo add info message
            return false;
        }

        for(byte[] token: this.validTokens.get(user)){
            Signature signVerify = Signature.getInstance(AuthenticationServerInterface.SIGN_INSTANCE);
            signVerify.initVerify(this.publicKeySignature);
            signVerify.update(token);

            if(signVerify.verify(tokenSigned)){
                valid = true;
                break;
            }
        }
        return valid;

        /*
        if(this.validTokens.contains(token)){
            this.sign.initVerify(this.publicKeySignature);
            if(this.sign.verify(token)) {
                this.usedTokens.add(token);
                return true;
            }
        }*/

    }


    private void  addState(byte[] hash, int index, String tag){
        CellLocationPair cellPair = new CellLocationPair(index, tag);
        String key = cellPair.toString();
        //byte[] key =  this.md.digest(cellPair.toString().getBytes());

        this.cellStates.put(key, hash);
    }

    private boolean verifyState(byte[] hash, int index, String tag){
        //Todo hash the cellocation

        CellLocationPair cellPair = new CellLocationPair(index, tag);
        // The hash of the tag is used in the key.
        String keyString = cellPair.getIndex() +  CellLocationPair.divider + cellPair.getTagHash();

        if(this.cellStates.get(keyString)!=null){
            return Arrays.equals(hash, this.cellStates.get(keyString));
        }
        return false;
    }

}
