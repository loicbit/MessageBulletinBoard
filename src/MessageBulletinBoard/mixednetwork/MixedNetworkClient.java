package MessageBulletinBoard.mixednetwork;

import MessageBulletinBoard.bulletinboard.BulletinBoardInterface;
import MessageBulletinBoard.crypto.AsymEncrypt;
import MessageBulletinBoard.crypto.DiffieH;
import MessageBulletinBoard.data.CellLocationPair;
import org.apache.commons.lang3.SerializationUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Key;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MixedNetworkClient {
    private List<byte []> tokens;
    private MixedNetworkServerInterface mixedNetworkServerStub;
    private Registry registry;
    private String nameUser;
    private AsymEncrypt asymEncrypt;
    private Key publicKeyTokenServer;

    private MessageDigest md;

    private boolean isEncrypted = false;
    private boolean publicKeysSend = false;

    CellLocationPair nextCellLocationPairAB = null;
    CellLocationPair nextCellLocationPairBA = null;

    private DiffieH diffiehAB;
    private DiffieH diffiehBA;



    public MixedNetworkClient(String nameUser) throws Exception {
        this.nameUser = nameUser;
        this.tokens = new LinkedList<>();
        this.asymEncrypt = new AsymEncrypt();

        this.diffiehAB = new DiffieH();
        this.diffiehBA = new DiffieH();

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

    public void initMixedNetwork() throws RemoteException {
        //todo: return boolean if succeed
        byte[] publicKeyUserSer = this.asymEncrypt.getPublicKeySer();

        byte[] publicKeyOtherSer = this.mixedNetworkServerStub.initContact(this.nameUser, publicKeyUserSer);
        this.publicKeyTokenServer = SerializationUtils.deserialize(publicKeyOtherSer);
    }

    private boolean verifyTokens(List<byte[]> tokens){
        return false;
    }

    public void setNextCellLocationPairAB(CellLocationPair next){
        //todo first check if there is
        //this.generateStateHashAB();
        this.nextCellLocationPairAB = next;
    }

    public void setNextCellLocationPairBA(CellLocationPair next){
        this.nextCellLocationPairBA = next;
    }

    public void sendMessage(String message) throws RemoteException {
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
            String token = "dummy";
            String[] messageTagPair = prepareMessage(message, locationCurrentMessage);
            String encryptedMessage = this.diffiehAB.encrypt(messageTagPair[0]);

            this.mixedNetworkServerStub.add(locationCurrentMessage.getIndex(), encryptedMessage, messageTagPair[1], token);
        }else{
            throw new NullPointerException("First cell not yet initialised");
        }
    }

    public void sendCryptoKeys() throws RemoteException{
        CellLocationPair locationCurrentMessage = this.nextCellLocationPairAB;

        String publicKeyAB = this.diffiehAB.getPubKey();
        String publicKeyBA = this.diffiehBA.getPubKey();

        String randomSeedKDFString = Integer.toString(this.diffiehAB.getSeed());
        //String randomSeedKDFString =  "";

        if(locationCurrentMessage != null){
            //String token = getToken();
            String token = "dummy";
            String message = publicKeyAB + BulletinBoardInterface.keyDIV + publicKeyBA + BulletinBoardInterface.keyDIV + randomSeedKDFString;
            String[] messageTagPair = prepareMessage(message, locationCurrentMessage);


            this.mixedNetworkServerStub.add(locationCurrentMessage.getIndex(), messageTagPair[0], messageTagPair[1], token);
        } else{
            throw new NullPointerException("First cell not yet initialised");
        }

        this.publicKeysSend = true;
    }

    public void setPublicKeysContact(String publicKeyContactAB, String publicKeyContactBA){
        this.diffiehAB.generateSecretKey(publicKeyContactAB);
        this.diffiehBA.generateSecretKey(publicKeyContactBA);
    }

    public String getMessage() throws RemoteException {
        // todo: get token
        if (this.nextCellLocationPairBA != null) {
            CellLocationPair nextLocation = this.nextCellLocationPairBA;
            String token="";

            String uMessage = this.mixedNetworkServerStub.get(nextLocation.getIndex(), nextLocation.getTag(), token);

            if(uMessage != null){
                if(!isSecured()){
                    String message =  splitUMessage(uMessage);
                    String[] split = message.split(BulletinBoardInterface.keyDIV);
                    setPublicKeysContact(split[1], split[0]);
                    int seedBA = Integer.valueOf(split[2]);

                    this.diffiehBA.setSeed(seedBA);

                    // In case publickeys not yet send, send them
                    if(!this.publicKeysSend) sendCryptoKeys();

                }else{
                    String message =  this.diffiehBA.decrypt(uMessage);
                    return splitUMessage(message);
                }
            } else return null;
        }
        return null;
    }

    public boolean isConnected(){
        return this.nextCellLocationPairAB !=null && this.nextCellLocationPairBA != null;
    }

    public boolean isEncrypted() { return this.isEncrypted; }

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
        return this.diffiehAB.isSecurd() && this.diffiehBA.isSecurd();
    }

    /*
    private void generateStateHashAB(){
        if(this.nextCellLocationPairAB == null) this.hashAB = null;
        else{
            this.hashAB = this.nextCellLocationPairAB.getHash().getBytes(StandardCharsets.UTF_8);
        }
    }*/

    private byte[] getHashBA(){
        return this.nextCellLocationPairBA.getHash().getBytes(StandardCharsets.UTF_8);
    }
}
