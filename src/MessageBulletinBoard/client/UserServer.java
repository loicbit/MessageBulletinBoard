package MessageBulletinBoard.client;

import MessageBulletinBoard.bulletinboard.BulletinBoardInterface;
import MessageBulletinBoard.crypto.AsymEncrypt;
import MessageBulletinBoard.data.CellLocationPair;
import org.apache.commons.lang3.SerializationUtils;

import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;

import java.security.Key;

public class UserServer implements UserServerInterface{
    private String firstStateHash = null;
    private int securityParam = 8;
    private String publicKey = null;

    //private HashMap<String, AssymEncrypt> assymEncrypt= new HashMap<>();
    private HashMap<String, Key> publickeys= new HashMap<>();
    private AsymEncrypt asymEncrypt;

    //private HashMap<String, State> firstStates= new HashMap<>();
    //private HashMap<String, Integer> firstStateHashes= new HashMap<>();
    private HashMap<String, String> state= new HashMap<>();
    private Registry registry = null;

    private HashMap<String, CellLocationPair> firsCellsAB = new HashMap();
    private HashMap<String, CellLocationPair> firsCellsBA = new HashMap();

    public UserServer(String nameUser) throws Exception{
        this.asymEncrypt = new AsymEncrypt();

        try {
            UserServerInterface stub = (UserServerInterface) UnicastRemoteObject.exportObject(this, 0);

            try{
                 registry = LocateRegistry.createRegistry(UserServerInterface.REG_PORT);
            }catch(Exception e) {
                 registry = LocateRegistry.getRegistry(UserServerInterface.REG_PORT);
            }

            String nameReg = UserServerInterface.DEF_PATH_USER + nameUser;
            registry.bind(nameReg, stub);

            System.err.println("Server ready");
        }catch (Exception e) {
            throw new Exception(e);
        }
    }

    public CellLocationPair getFirstCellPairAB(String contact){
        return this.firsCellsAB.get(contact);
    }

    @Override
    public byte[] initContact(String nameContact, byte[] publicKeyStr) throws RemoteException {
        Key publicKeyOther = SerializationUtils.deserialize(publicKeyStr);
        this.publickeys.put(nameContact, publicKeyOther);

        return this.asymEncrypt.getPublicKeySer();
    }

    @Override
    public byte[] getFirstCell(byte[] firstCellBA) throws Exception {
        String decrypted = this.asymEncrypt.do_RSADecryption(firstCellBA);

        String[] response = decrypted.split(UserServerInterface.DIV_CELL);
        String nameContact = response[0];


        CellLocationPair newCellBA = convertToObject(response[1]);

        // Todo: replace tag and index generator to one place and change to method
        CellLocationPair newCellAB = generateNewCell();
        //CellLocationPair newCellBA = convertToObject(cellToReceive);


        //todo remove cell save
        this.firsCellsAB.put(nameContact, newCellAB);
        this.firsCellsBA.put(nameContact, newCellBA);


        //Send the other the first cell to look for
        String newCellABStr = newCellAB.toString();
        byte[] encrypted = this.asymEncrypt.do_RSAEncryption(newCellABStr, this.publickeys.get(nameContact));

        return encrypted;
    }

    @Override
    public byte[] checkState(byte[] hashState) throws Exception {
        //todo generate own state
        String sameState = "";

        String decrypted = this.asymEncrypt.do_RSADecryption(hashState);

        String[] response = decrypted.split(UserServerInterface.DIV_CELL);
        String nameContact = response[0];

        if(this.state.get(nameContact).equals(response[1])){
            sameState = "true";
        }
        else{
            sameState = "false";
        }

        //change return to own state
        byte[] encrypted = this.asymEncrypt.do_RSAEncryption(sameState, this.publickeys.get(nameContact));
        return encrypted;
    }

    public boolean isConnected(String contactName){
        return this.firsCellsAB.containsKey(contactName) && this.firsCellsBA.containsKey(contactName);
    }

    public CellLocationPair getFirstCellAB(String contactName){
        return this.firsCellsAB.get(contactName);
    }

    public CellLocationPair getFirstCellBA(String contactName){
        return this.firsCellsBA.get(contactName);
    }

    private CellLocationPair generateNewCell(){
        Random rand = new SecureRandom();
        int index = rand.nextInt(BulletinBoardInterface.NUMBER_CELLS *100)%BulletinBoardInterface.NUMBER_CELLS;

        byte[] array = new byte[BulletinBoardInterface.securityParam];
        new Random().nextBytes(array);
        String tag = new String(array, Charset.forName("ASCII"));

        return new CellLocationPair(index, tag);
    }

    public Key getPublicKeyContact(String contactName){

        return this.publickeys.get(contactName);
    }

    //todo: remove
    private CellLocationPair convertToObject(String pairString){
        String splitted[] = pairString.split(CellLocationPair.divider);

        int index = Integer.valueOf(splitted[0]);
        String tag = splitted[1];

        return new CellLocationPair(index, tag);
    }


    //private CellLocationPair generateFirstCell
}
