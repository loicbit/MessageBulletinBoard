package MessageBulletinBoard.bulletinboard;

import MessageBulletinBoard.crypto.DiffieH;
import MessageBulletinBoard.data.CellLocationPair;
import MessageBulletinBoard.tokenserver.TokenServerInterface;

import java.nio.charset.Charset;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class BulletinBoardClient {
    //todo: encrypt communication

    private Registry registry;
    String nameUser = null;

    CellLocationPair nextCellLocationPairAB = null;
    CellLocationPair nextCellLocationPairBA = null;

    private BulletinBoardInterface bulletinServerStub;


    private MessageDigest md;

    private DiffieH diffiehAB;
    private DiffieH diffiehBA;

    private boolean isEncrypted = false;
    private boolean publicKeysSend = false;

    private LinkedList<String> tokens = new LinkedList<>();

    // todo generate hash of the state

    public BulletinBoardClient(String contact) throws RemoteException, NotBoundException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        this.diffiehAB = new DiffieH();
        this.diffiehBA = new DiffieH();

        try{
            this.registry = LocateRegistry.createRegistry(BulletinBoardInterface.REG_PORT);
        }catch(Exception e) {
            this.registry = LocateRegistry.getRegistry(BulletinBoardInterface.REG_PORT);
        }

        this.bulletinServerStub = (BulletinBoardInterface) this.registry.lookup(BulletinBoardInterface.STUB_NAME);



        this.md = MessageDigest.getInstance(BulletinBoardInterface.algoMD);
        //System.out.println(this.bulletinServerStub);

    }

    public void setNextCellLocationPairAB(CellLocationPair next){
        this.nextCellLocationPairAB = next;

    }

    public void setNextCellLocationPairBA(CellLocationPair next){
        this.nextCellLocationPairBA = next;
    }

    public void sendMessage(String message) throws RemoteException {
        // In case publickeys not yet send, send them
        if(!this.publicKeysSend) sendPublicKeys();

        CellLocationPair locationCurrentMessage = this.nextCellLocationPairAB;

        if(locationCurrentMessage != null){
            //String token = getToken();
            //todo get token
            String token = "dummy";
            String[] messageTagPair = prepareMessage(message, locationCurrentMessage);
            String encryptedMessage = this.diffiehAB.encrypt(messageTagPair[0]);

            this.bulletinServerStub.add(locationCurrentMessage.getIndex(), encryptedMessage, messageTagPair[1], token);
        }else{
            throw new NullPointerException("First cell not yet initialised");
        }
    }

    //todo change name
    public void sendPublicKeys() throws RemoteException{
        CellLocationPair locationCurrentMessage = this.nextCellLocationPairAB;
        String publicKeyAB = this.diffiehAB.getPubKey();
        String publicKeyBA = this.diffiehBA.getPubKey();

        if(locationCurrentMessage != null){
            //String token = getToken();
            String token = "dummy";
            String message = publicKeyAB + BulletinBoardInterface.keyDIV + publicKeyBA;
            String[] messageTagPair = prepareMessage(message, locationCurrentMessage);


            this.bulletinServerStub.add(locationCurrentMessage.getIndex(), messageTagPair[0], messageTagPair[1], token);
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
        if (this.nextCellLocationPairBA != null) {
            CellLocationPair nextLocation = this.nextCellLocationPairBA;

            String uMessage = this.bulletinServerStub.get(nextLocation.getIndex(), nextLocation.getTag());

            if(uMessage != null){
                if(!isSecured()){
                    String message =  splitUMessage(uMessage);
                    String[] split = message.split(BulletinBoardInterface.keyDIV);
                    setPublicKeysContact(split[1], split[0]);

                    // In case publickeys not yet send, send them
                    if(!this.publicKeysSend) sendPublicKeys();

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


}
