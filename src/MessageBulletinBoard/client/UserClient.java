package MessageBulletinBoard.client;

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
    private String name;
    private HashMap<String, CellLocationPair> nextCellLocationPairAB = new HashMap<>();
    private HashMap<String, CellLocationPair> nextCellLocationPairBA = new HashMap<>();

    private HashMap<String, String> secretKeys = new HashMap<>();
    private String publicKey;

    private UserServerInterface userServerStub;

    public UserClient(String name) throws Exception {
        this.publicKey = null;
        this.name = name;

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
            this.userServerStub = (UserServerInterface) this.registry.lookup(UserServerInterface.DEF_PATH_USER+name);
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public void setSecretKey(String name, String secretKey){
        secretKeys.put(name, secretKey);
    }

    public CellLocationPair getNextCellLocationPairAB(String contactName){
        return this.nextCellLocationPairAB.get(contactName);
    }

    public CellLocationPair getNextCellLocationPairBA(String contactName){
        return this.nextCellLocationPairBA.get(contactName);
    }

    public Boolean contactAsKeyExchange(String nameContact) throws RemoteException{
        //todo prepare first cell for other
        //      setup secure communication before start
        //      prevent own name
        if(!secretKeys.containsKey(nameContact)){
            String publicKey =  this.userServerStub.initContact(nameContact, this.publicKey);
            Boolean firstCellReceived = firstCellExchange(nameContact);


            return firstCellReceived;
        }

        else return null;
    }

    public boolean firstCellExchange(String nameContact) throws RemoteException{
        CellLocationPair firstCellSend = generateNewCell();
        this.nextCellLocationPairAB.put(nameContact, firstCellSend);

        String firstCellGet = this.userServerStub.getFirstCell(nameContact, firstCellSend.toString());
        String splitted[] = firstCellGet.split(CellLocationPair.divider);

        int index = Integer.valueOf(splitted[0]);
        String tag = splitted[1];

        CellLocationPair firstCell = new CellLocationPair(index, tag);
        this.nextCellLocationPairBA.put(nameContact, firstCell);

        if(firstCell != null) return true;
        else return false;
        //getSecurityParameters(firstCell);

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

}
