package MessageBulletinBoard.client;

import MessageBulletinBoard.bulletinboard.BulletinBoardInterface;
import MessageBulletinBoard.crypto.AsymEncrypt;
import MessageBulletinBoard.data.CellLocationPair;
import MessageBulletinBoard.data.INFO_MESSAGE;
import MessageBulletinBoard.mixednetwork.MixedNetworkClient;
import org.apache.commons.lang3.SerializationUtils;

import java.nio.charset.Charset;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Key;
import java.security.SecureRandom;
import java.util.LinkedList;
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
    AsymEncrypt asymEncrypt;

    private MixedNetworkClient mixedNetworkClient;


    private UserServerInterface contactServerStub;


    public UserClient(String contact, String user) throws Exception {
        this.publicKey = null;
        this.nameContact = contact;
        this.nameUser = user;
        this.connected = false;
        this.secured = false;

        this.asymEncrypt = new AsymEncrypt();

        this.mixedNetworkClient = new MixedNetworkClient(contact, user);


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

            byte[] publicKeyOtherSerialized =  this.contactServerStub.initContact(this.nameUser, this.asymEncrypt.getPublicKeySer());
            this.publicKeyOther = SerializationUtils.deserialize(publicKeyOtherSerialized);

            this.connected = firstCellExchange(this.nameUser);

            return this.connected;
        }

        else return null;
    }

    public INFO_MESSAGE sendPublicKeys() throws Exception {
        return this.mixedNetworkClient.sendCryptoKeys();
    }

    public INFO_MESSAGE sendMessageBoard(String message) throws Exception {
        INFO_MESSAGE status;

        if(isConnected()){
            if(this.mixedNetworkClient.isSecured()){
                status = this.mixedNetworkClient.sendMessage(message);
            }else{
                return sendPublicKeys();
                //todo; error not secured to send
            }
        }else {
            //todo print error message

        }
        return INFO_MESSAGE.NO_MESSAGE_SENT;
    }

    public String getMessageBoard() throws Exception {
        if(isConnected()){
            return this.mixedNetworkClient.getMessage();
        }else {
            //todo print error message
            return null;
        }
    }

    public void addTokens(LinkedList tokens){
        this.mixedNetworkClient.addTokensUser(tokens);
    }

    public boolean firstCellExchange(String nameContact) throws Exception {
        CellLocationPair firstCellSend = generateNewCell();
        this.mixedNetworkClient.setNextCellLocationPairAB(firstCellSend);

        //byte[] cellSerialized = SerializationUtils.serialize(firstCellSend);
        String nameAndCell = nameContact + UserServerInterface.DIV_CELL+ firstCellSend.toString();
        byte[] nameAndCellEncrypted = this.asymEncrypt.do_RSAEncryption(nameAndCell, this.publicKeyOther);

        byte[] firstCellGetEncrypted = this.contactServerStub.getFirstCell(nameAndCellEncrypted);
        String firstCellGet = this.asymEncrypt.decryptionToString(firstCellGetEncrypted);

        CellLocationPair firstCellReceive = new CellLocationPair(firstCellGet);
        this.mixedNetworkClient.setNextCellLocationPairBA(firstCellReceive);

        if(firstCellReceive != null) return true;
        else return false;
    }

    public void setFirstCellPair(CellLocationPair cellAB, CellLocationPair cellBA){
        this.mixedNetworkClient.setNextCellLocationPairAB(cellAB);
        this.mixedNetworkClient.setNextCellLocationPairBA(cellBA);

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
        return this.mixedNetworkClient.isConnected();
        //return this.connected;
    }


    //todo get states from bulletin

    private void updateStates(){

    }

}
