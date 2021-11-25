package MessageBulletinBoard.client;

import MessageBulletinBoard.bulletinboard.BulletinBoardClient;
import MessageBulletinBoard.bulletinboard.BulletinBoardInterface;
import MessageBulletinBoard.crypto.AssymEncrypt;
import MessageBulletinBoard.data.CellLocationPair;
import MessageBulletinBoard.tokenserver.TokenServerInterface;
import org.apache.commons.lang3.SerializationUtils;

import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Key;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;

public class UserClient {
    //todo: reach out to new contact
    //      add message to cell
    //      get message of cell

    private Registry registry;
    private String nameContact;
    private String nameUser;

    private boolean connected;
    private boolean secured;

    private CellLocationPair nextCellLocationPairAB = null;
    private CellLocationPair nextCellLocationPairBA = null;

    private String secretKey;
    private String publicKey;

    private Key publicKeyOther;
    AssymEncrypt assymEncrypt;

    private BulletinBoardClient boardClient;

    private UserServerInterface contactServerStub;
    private TokenServerInterface tokenServerStub;

    public UserClient(String contact, String user) throws Exception {
        //todo: add state of communication (assynchrone securded connection,firstcell exchange, sec parameter, messages)
        this.publicKey = null;
        this.nameContact = contact;
        this.nameUser = user;
        this.connected = false;
        this.secured = false;

        this.assymEncrypt = new AssymEncrypt();

        this.boardClient = new BulletinBoardClient(contact);

        try{
            this.registry = LocateRegistry.createRegistry(UserServerInterface.REG_PORT);
        }catch(Exception ex){
            System.out.println(ex);
        }

        try{
            this.registry = LocateRegistry.getRegistry(UserServerInterface.REG_PORT);

        }catch(Exception e){
            System.out.println(e);
            //throw new Exception();
        }

        try{
            this.contactServerStub = (UserServerInterface) this.registry.lookup(UserServerInterface.DEF_PATH_USER+contact);
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public void setSecretKey(String secretKey){
        this.secretKey = secretKey;
    }

    public void setPublicKeyContact(Key publicKeyOther){
        this.publicKeyOther = publicKeyOther;
    }


    public Boolean symmetricKeyExchange(String nameContact) throws Exception {
        //todo prepare first cell for other
        //      setup secure communication before start
        //      prevent own name
        if(!this.connected){
            //byte[] publicKeySerialized = SerializationUtils.serialize(this.publicKey);

            // Setup assymetric communication between the two contacts before exchanging public keys for symmetric

            byte[] publicKeyOtherSerialized =  this.contactServerStub.initContact(this.nameUser, this.assymEncrypt.getPublicKeySer());
            this.publicKeyOther = SerializationUtils.deserialize(publicKeyOtherSerialized);

            this.connected = firstCellExchange(this.nameUser);

            return this.connected;
        }

        else return null;
    }

    public void sendPublicKeys() throws RemoteException {
        this.boardClient.sendPublicKeys();
    }

    public boolean sendMessageBoard(String message) throws RemoteException {
        if(isConnected()){
            if(this.boardClient.isSecured()){
                this.boardClient.sendMessage(message);
            }else{
                sendPublicKeys();
                //todo; error not secured to send
                return false;
            }
        }else {
            //todo print error message

        }
        return false;
    }

    public String getMessageBoard() throws RemoteException {
        //todo check if it is not publickeys
        if(isConnected()){
            return this.boardClient.getMessage();
        }else {
            //todo print error message
            return null;
        }
    }

    public boolean firstCellExchange(String nameContact) throws Exception {
        CellLocationPair firstCellSend = generateNewCell();
        this.boardClient.setNextCellLocationPairAB(firstCellSend);

        //byte[] cellSerialized = SerializationUtils.serialize(firstCellSend);
        String nameAndCell = nameContact + UserServerInterface.DIV_CELL+ firstCellSend.toString();
        byte[] nameAndCellEncrypted = this.assymEncrypt.do_RSAEncryption(nameAndCell, this.publicKeyOther);

        byte[] firstCellGetEncrypted = this.contactServerStub.getFirstCell(nameAndCellEncrypted);
        String firstCellGet = this.assymEncrypt.do_RSADecryption(firstCellGetEncrypted);

        CellLocationPair firstCellReceive = new CellLocationPair(firstCellGet);
        this.boardClient.setNextCellLocationPairBA(firstCellReceive);

        if(firstCellReceive != null) return true;
        else return false;
    }

    public void setFirstCellPair(CellLocationPair cellAB, CellLocationPair cellBA){
        this.boardClient.setNextCellLocationPairAB(cellAB);
        this.boardClient.setNextCellLocationPairBA(cellBA);

        this.connected = true;
    }

    private CellLocationPair generateNewCell(){
        Random rand = new SecureRandom();
        int index = rand.nextInt(BulletinBoardInterface.NUMBER_CELLS *100)%BulletinBoardInterface.NUMBER_CELLS;

        byte[] array = new byte[BulletinBoardInterface.securityParam];
        new Random().nextBytes(array);
        String tag = new String(array, Charset.forName("ASCII"));

        return new CellLocationPair(index, tag);
    }

    private void getSecurityParameters(String nameContact){

    }

    public boolean isConnected(){
        return this.boardClient.isConnected();
        //return this.connected;
    }


     /*
    public CellLocationPair getNextCellLocationPairAB(){
        return this.nextCellLocationPairAB;
    }

    public CellLocationPair getNextCellLocationPairBA(){
        return this.nextCellLocationPairBA;
    }*/
}
