package MessageBulletinBoard.client;

import MessageBulletinBoard.bulletinboard.BulletinBoardInterface;
import MessageBulletinBoard.crypto.AsymEncrypt;
import MessageBulletinBoard.data.CellLocationPair;
import MessageBulletinBoard.data.INFO_MESSAGE;
import MessageBulletinBoard.mixednetwork.MixedNetworkClient;
import org.apache.commons.lang3.SerializationUtils;

import java.nio.charset.StandardCharsets;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Key;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.Random;

public class UserClient {
    private Registry registry;
    private final String nameContact;
    private final String nameUser;

    private boolean connected;

    private Key publicKeyOther;
    AsymEncrypt asymEncrypt;

    private final MixedNetworkClient mixedNetworkClient;


    private UserServerInterface contactServerStub;


    public UserClient(String contact, String user) throws Exception {
        this.nameContact = contact;
        this.nameUser = user;
        this.connected = false;

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
        }

        try{
            this.contactServerStub = (UserServerInterface) this.registry.lookup(UserServerInterface.DEF_PATH_USER+contact);
        }catch(Exception e){
            System.out.println(e);
        }
    }


    public void setPublicKeyContact(Key publicKeyOther){
        this.publicKeyOther = publicKeyOther;
    }


    public Boolean asymmetricKeyExchange(String nameContact) throws Exception {
        if(!this.connected){
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
            }
        }else {

        }
        return INFO_MESSAGE.NO_MESSAGE_SENT;
    }

    public String getMessageBoard() throws Exception {
        if(isConnected()){
            return this.mixedNetworkClient.getMessage();
        }else {
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
        String nameAndCell = nameContact + UserServerInterface.DIV_CELL+ firstCellSend;
        byte[] nameAndCellEncrypted = this.asymEncrypt.encryptionTBytes(nameAndCell, this.publicKeyOther);

        byte[] firstCellGetEncrypted = this.contactServerStub.getFirstCell(nameAndCellEncrypted);
        String firstCellGet = this.asymEncrypt.decryptionToString(firstCellGetEncrypted);

        CellLocationPair firstCellReceive = new CellLocationPair(firstCellGet);
        this.mixedNetworkClient.setNextCellLocationPairBA(firstCellReceive);

        return firstCellReceive != null;
    }

    public void setFirstCellPair(CellLocationPair cellAB, CellLocationPair cellBA){
        this.mixedNetworkClient.setNextCellLocationPairAB(cellAB);
        this.mixedNetworkClient.setNextCellLocationPairBA(cellBA);

        this.connected = true;
    }

    private CellLocationPair generateNewCell(){
        Random rand = new SecureRandom();
        int index = rand.nextInt(BulletinBoardInterface.NUMBER_CELLS *100)%BulletinBoardInterface.NUMBER_CELLS;

        byte[] array = new byte[BulletinBoardInterface.TAG_LENGTH];
        new Random().nextBytes(array);
        String tag = new String(array, StandardCharsets.US_ASCII);

        return new CellLocationPair(index, tag);
    }


    public boolean isConnected(){
        return this.mixedNetworkClient.isConnected();
    }

}
