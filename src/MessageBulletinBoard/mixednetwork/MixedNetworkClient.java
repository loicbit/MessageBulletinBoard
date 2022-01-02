package MessageBulletinBoard.mixednetwork;

import MessageBulletinBoard.bulletinboard.BulletinBoardInterface;
import MessageBulletinBoard.crypto.DiffieH;
import MessageBulletinBoard.data.COM_DIR;
import MessageBulletinBoard.data.CellLocationPair;
import MessageBulletinBoard.data.INFO_MESSAGE;

import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class MixedNetworkClient {
    private List<byte[]> tokens;
    private MixedNetworkServerInterface mixedNetworkServerStub;
    private Registry registry;
    private String nameUser;
    //private AsymEncrypt asymEncrypt;
    private Key publicKeyMixedServer;
    private DiffieH symEncryptServer;

    private MessageDigest md;

    private boolean isEncrypted = false;
    private boolean publicKeysSend = false;

    CellLocationPair nextCellLocationPairAB = null;
    CellLocationPair nextCellLocationPairBA = null;

    private DiffieH symEncryptAB;
    private DiffieH symEncryptBA;




    public MixedNetworkClient(String nameContact, String nameUser) throws Exception {
        this.nameUser = nameUser;
        this.tokens = new LinkedList<>();
        //this.asymEncrypt = new AsymEncrypt();

        this.symEncryptAB = new DiffieH(true);
        this.symEncryptBA = new DiffieH(true);

        this.symEncryptServer = new DiffieH(false);

        try{
            this.registry = LocateRegistry.createRegistry(BulletinBoardInterface.REG_PORT);
        }catch(Exception e) {
            this.registry = LocateRegistry.getRegistry(BulletinBoardInterface.REG_PORT);
        }

        try{
            this.mixedNetworkServerStub = (MixedNetworkServerInterface) this.registry.lookup(MixedNetworkServerInterface.DEF_PATH);
        }catch(Exception e){
            System.out.println(e);
        }

        this.md = MessageDigest.getInstance(BulletinBoardInterface.algoMD);
        this.initMixedNetwork();
    }

    public void initMixedNetwork() throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        //todo: return boolean if succeed
        PublicKey publicKeyUserSer = this.symEncryptServer.getPubkeyObject();

        //byte[] publicKeyUserSer = this.asymEncrypt.getPublicKeySer();

        PublicKey publicKeyOtherSer = this.mixedNetworkServerStub.initContact(this.nameUser, publicKeyUserSer);
        this.symEncryptServer.generateSecretKeyObject(publicKeyOtherSer);
    }

    //private boolean verifyTokens(List<byte[]> tokens){
    //    return false;
    //}

    public void setNextCellLocationPairAB(CellLocationPair next){
        //todo first check if there is
        //this.generateStateHashAB();
        this.nextCellLocationPairAB = next;
    }

    public void setNextCellLocationPairBA(CellLocationPair next){
        this.nextCellLocationPairBA = next;
    }

    public INFO_MESSAGE sendMessage(String message) throws Exception {
        // In case publickeys not yet send, send them
        if(!this.publicKeysSend){
            //this.generateStateHashAB();
            sendCryptoKeys();
        }

        CellLocationPair locationCurrentMessage = this.nextCellLocationPairAB;

        // Generate hash of the cell to send
        //this.generateStateHashAB();

        if(locationCurrentMessage != null){
            //String token = getToken();
            //todo get token

            if(this.tokens.isEmpty()) return INFO_MESSAGE.NO_TOKENS_AIV;

            byte[] token = this.tokens.get(0);
            byte[] hashAB = this.getHash(COM_DIR.AB);

            String[] messageTagPair = prepareMessage(message, locationCurrentMessage);
            String encryptedMessage = this.symEncryptAB.encrypt(messageTagPair[0]);

            byte[] indexEnc = this.symEncryptServer.encryptBytes(Integer.toString(locationCurrentMessage.getIndex()).getBytes());
            byte[] encryptedMessageEnc = this.symEncryptServer.encryptBytes(encryptedMessage.getBytes());
            byte[] tagEnc = this.symEncryptServer.encryptBytes(messageTagPair[1].getBytes());
            byte[] tokenEnc = this.symEncryptServer.encryptBytes(token);
            byte[] hashABEnc = this.symEncryptServer.encryptBytes(hashAB);

            this.mixedNetworkServerStub.add(indexEnc, encryptedMessageEnc, tagEnc, tokenEnc, hashABEnc, this.nameUser);

            return INFO_MESSAGE.MESSAGE_SENT;
        }else{
            return INFO_MESSAGE.NO_INIIT_CELL;
        }
    }

    public INFO_MESSAGE sendCryptoKeys() throws Exception{
        CellLocationPair locationCurrentMessage = this.nextCellLocationPairAB;

        String publicKeyAB = this.symEncryptAB.getPubKey();
        String publicKeyBA = this.symEncryptBA.getPubKey();

        String randomSeedKDFString = Integer.toString(this.symEncryptAB.getSeed());

        if(locationCurrentMessage != null){
            //String token = getToken();

            if(this.tokens.isEmpty()) return INFO_MESSAGE.NO_TOKENS_AIV;

            byte[] token = this.tokens.get(0);
            byte[] hashAB = this.getHash(COM_DIR.AB);

            String message = publicKeyAB + BulletinBoardInterface.keyDIV + publicKeyBA + BulletinBoardInterface.keyDIV + randomSeedKDFString;
            String[] messageTagPair = prepareMessage(message, locationCurrentMessage);

            byte[] indexEnc = this.symEncryptServer.encryptBytes(Integer.toString(locationCurrentMessage.getIndex()).getBytes());
            byte[] valueEnc = this.symEncryptServer.encryptBytes(messageTagPair[0].getBytes());
            byte[] tagEnc = this.symEncryptServer.encryptBytes(messageTagPair[1].getBytes());
            byte[] tokenEnc = this.symEncryptServer.encryptBytes(token);
            byte[] hashABEnc = this.symEncryptServer.encryptBytes(hashAB);

            this.mixedNetworkServerStub.add(indexEnc, valueEnc, tagEnc, tokenEnc, hashABEnc, this.nameUser);
            this.publicKeysSend = true;
            return INFO_MESSAGE.CRYPTO_SENT;

        }else{
            return INFO_MESSAGE.NO_INIIT_CELL;
        }

    }

    public void setPublicKeysContact(String publicKeyContactAB, String publicKeyContactBA){
        this.symEncryptAB.generateSecretKey(publicKeyContactAB);
        this.symEncryptBA.generateSecretKey(publicKeyContactBA);
    }

    public String getMessage() throws Exception {
        if(this.tokens.isEmpty()){
            //todo; return error message
            return INFO_MESSAGE.NO_TOKENS_AIV.name();
        }

        if (this.nextCellLocationPairBA != null) {
            //if(this.tokens.isEmpty()) return INFO_MESSAGE.NO_TOKENS_AIV.name();
            byte[] token = this.tokens.get(0);

            byte[] hashBA = this.getHash(COM_DIR.BA);

            CellLocationPair nextLocation = this.nextCellLocationPairBA;


            byte[] indexEnc = this.symEncryptServer.encryptBytes(Integer.toString(nextLocation.getIndex()).getBytes());
            byte[] tagEnc = this.symEncryptServer.encryptBytes(nextLocation.getTag().getBytes());

            byte[] tokenEnc = this.symEncryptServer.encryptBytes(token);
            byte[] hashEnc = this.symEncryptServer.encryptBytes(hashBA);

            byte[] uMessageEnc = this.mixedNetworkServerStub.get(indexEnc, tagEnc, tokenEnc, hashEnc,this.nameUser);

            if(uMessageEnc.length>1){
                String uMessage = new String(this.symEncryptServer.decryptBytes(uMessageEnc));
                if(!isSecured()){
                    String message =  splitUMessage(uMessage);
                    String[] split = message.split(BulletinBoardInterface.keyDIV);
                    setPublicKeysContact(split[1], split[0]);
                    int seedBA = Integer.valueOf(split[2]);

                    this.symEncryptBA.setSeed(seedBA);

                    // In case publickeys not yet send, send them
                    if(!this.publicKeysSend) sendCryptoKeys();

                }else{
                    String message =  this.symEncryptBA.decrypt(uMessage);
                    return splitUMessage(message);
                }
            } else return null;
        }
        return null;
    }

    public boolean isConnected(){
        return this.nextCellLocationPairAB !=null && this.nextCellLocationPairBA != null;
    }

    public void addTokensUser(LinkedList tokensRec){
        tokensRec.forEach((token)->{ this.tokens.add((byte[]) token);});
    }

    /*
    public void addTokensServer(LinkedList tokensRec) {
        //todo handle exception with info message
        tokensRec.forEach((token) -> {
            byte[] tokenEnc =  this.symEncryptServer.encryptBytes((byte[]) token);
            try {
                this.mixedNetworkServerStub.addToken(tokenEnc, BulletinBoardInterface.emptyMessage,this.nameUser);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }*/

    private String[] prepareMessage(String message, CellLocationPair locationCurrentMessage){
        this.nextCellLocationPairAB = null;
        String [] result = new String[2];

        //todo replace generator
        Random rand = new Random();
        int index = rand.nextInt(BulletinBoardInterface.NUMBER_CELLS *100)%BulletinBoardInterface.NUMBER_CELLS;


        byte[] array = new byte[BulletinBoardInterface.securityParam];
        new Random().nextBytes(array);
        String tag = new String(array, Charset.forName("ASCII"));

        CellLocationPair nextLocationCell = new CellLocationPair(index, tag);

        //todo clear and only save the hash
        this.nextCellLocationPairAB = nextLocationCell;

        // uMessage
        result[0] = message + BulletinBoardInterface.messageDiv + index + BulletinBoardInterface.messageDiv + tag;

        // tagHash
        result[1] = new String(this.md.digest(locationCurrentMessage.getTag().getBytes()));

        return result;
    }

    private String splitUMessage(String message){
        this.nextCellLocationPairBA = null;
        String splitted[] = message.split(BulletinBoardInterface.messageDiv);
        String messageCell = splitted[0];
        int nextIdx = Integer.valueOf(splitted[1]);
        String nextTag = splitted[2];

        CellLocationPair nextPair = new CellLocationPair(nextIdx, nextTag);

        this.nextCellLocationPairBA = nextPair;

        return messageCell;
    }

    public boolean isSecured(){
        return this.symEncryptAB.isSecurd() && this.symEncryptBA.isSecurd();
    }

    /*
    private void generateStateHashAB(){
        if(this.nextCellLocationPairAB == null) this.hashAB = null;
        else{
            this.hashAB = this.nextCellLocationPairAB.getHash().getBytes(StandardCharsets.UTF_8);
        }
    }*/

    private byte[] getHash(COM_DIR direction) throws NoSuchAlgorithmException{
        CellLocationPair locPair = null;
        SecretKey sharedKey = null;

        if(direction == COM_DIR.AB){
            locPair = this.nextCellLocationPairAB;
            sharedKey = this.symEncryptAB.getSharedKey();
        }
        else if(direction == COM_DIR.BA){
            locPair = this.nextCellLocationPairBA;
            sharedKey = this.symEncryptBA.getSharedKey();
        }
        else return BulletinBoardInterface.emptyMessage;

        MessageDigest keyHash = MessageDigest.getInstance("SHA-256");

        String index = Integer.toString(locPair.getIndex());
        String tag = locPair.getTag();

        byte[] key = null;


        if(sharedKey == null || !this.publicKeysSend){
            key = INFO_MESSAGE.NO_KEY.name().getBytes();
        }else{
            key = sharedKey.getEncoded();
        }
        // Sort to have the same order for A and B
        List<ByteBuffer> keys = Arrays.asList(ByteBuffer.wrap(index.getBytes()), ByteBuffer.wrap(tag.getBytes()), ByteBuffer.wrap(key));
        Collections.sort(keys);

        keyHash.update(keys.get(0));
        keyHash.update(keys.get(1));
        keyHash.update(keys.get(2));

        return keyHash.digest();
    }
}
