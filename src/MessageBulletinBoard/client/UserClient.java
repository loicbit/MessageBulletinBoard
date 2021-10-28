package MessageBulletinBoard.client;

import MessageBulletinBoard.bulletinboard.BulletinBoardClient;
import MessageBulletinBoard.bulletinboard.BulletinBoardInterface;
import MessageBulletinBoard.data.CellLocationPair;

import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
    private CellLocationPair nextCellLocationPairAB = null;
    private CellLocationPair nextCellLocationPairBA = null;

    private String secretKey;
    private String publicKey;

    private BulletinBoardClient boardClient;

    private UserServerInterface contactServerStub;

    public UserClient(String contact, String user) throws Exception {
        this.publicKey = null;
        this.nameContact = contact;
        this.nameUser = user;
        this.connected = false;

        this.boardClient = new BulletinBoardClient(contact);

        try{
            this.registry = LocateRegistry.createRegistry(UserServerInterface.REG_PORT);
        }catch(Exception ex){
            System.out.println(ex);
        }

        try{
            //todo: generate publickey
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


    public Boolean contactAsKeyExchange(String nameContact) throws RemoteException{
        //todo prepare first cell for other
        //      setup secure communication before start
        //      prevent own name
        if(!this.connected){
            String publicKey =  this.contactServerStub.initContact(this.nameUser, this.publicKey);
            Boolean firstCellReceived = firstCellExchange(this.nameUser);

            this.connected = firstCellReceived;

            return firstCellReceived;
        }

        else return null;
    }

    public void sendMessage(String message) throws RemoteException {
        if(isConnected()){
            this.boardClient.sendMessage(message);
        }else {
            //todo print error message
        }
    }

    public String getMessage() throws RemoteException {
        if(isConnected()){
            return this.boardClient.getMessage();
        }else {
            //todo print error message
            return null;
        }
    }

    public boolean firstCellExchange(String nameContact) throws RemoteException{
        CellLocationPair firstCellSend = generateNewCell();
        this.boardClient.setNextCellLocationPairAB(firstCellSend);

        String firstCellGet = this.contactServerStub.getFirstCell(nameContact, firstCellSend.toString());
        String splitted[] = firstCellGet.split(CellLocationPair.divider);

        int index = Integer.valueOf(splitted[0]);
        String tag = splitted[1];

        CellLocationPair firstCellReceive = new CellLocationPair(index, tag);
        this.boardClient.setNextCellLocationPairBA(firstCellReceive);

        if(firstCellReceive != null) return true;
        else return false;
        //getSecurityParameters(firstCell);

    }

    public void setFirstCellPair(CellLocationPair cellAB, CellLocationPair cellBA){
        this.boardClient.setNextCellLocationPairAB(cellAB);
        this.boardClient.setNextCellLocationPairBA(cellBA);

        this.connected = true;
    }

    private CellLocationPair generateNewCell(){
        Random rand = new Random();
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
