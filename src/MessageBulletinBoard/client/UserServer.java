package MessageBulletinBoard.client;

import MessageBulletinBoard.bulletinboard.BulletinBoardInterface;
import MessageBulletinBoard.crypto.AsymEncrypt;
import MessageBulletinBoard.data.CellLocationPair;
import org.apache.commons.lang3.SerializationUtils;

import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;

import java.security.Key;

public class UserServer implements UserServerInterface{
    private final HashMap<String, Key> publickeys= new HashMap<>();
    private final AsymEncrypt asymEncrypt;

    private final HashMap<String, String> state= new HashMap<>();
    private Registry registry = null;

    private final HashMap<String, CellLocationPair> firsCellsAB = new HashMap();
    private final HashMap<String, CellLocationPair> firsCellsBA = new HashMap();

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

    @Override
    public byte[] initContact(String nameContact, byte[] publicKeyStr) throws RemoteException {
        Key publicKeyOther = SerializationUtils.deserialize(publicKeyStr);
        this.publickeys.put(nameContact, publicKeyOther);

        return this.asymEncrypt.getPublicKeySer();
    }

    @Override
    public byte[] getFirstCell(byte[] firstCellBA) throws Exception {
        String decrypted = this.asymEncrypt.decryptionToString(firstCellBA);

        String[] response = decrypted.split(UserServerInterface.DIV_CELL);
        String nameContact = response[0];

        CellLocationPair newCellBA = convertToObject(response[1]);

        CellLocationPair newCellAB = generateNewCell();

        this.firsCellsAB.put(nameContact, newCellAB);
        this.firsCellsBA.put(nameContact, newCellBA);


        //Send the other the first cell to look for
        String newCellABStr = newCellAB.toString();
        byte[] encrypted = this.asymEncrypt.encryptionTBytes(newCellABStr, this.publickeys.get(nameContact));

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

        byte[] array = new byte[BulletinBoardInterface.TAG_LENGTH];
        new Random().nextBytes(array);
        String tag = new String(array, StandardCharsets.US_ASCII);

        return new CellLocationPair(index, tag);
    }

    public Key getPublicKeyContact(String contactName){

        return this.publickeys.get(contactName);
    }

    private CellLocationPair convertToObject(String pairString){
        String[] splitted = pairString.split(CellLocationPair.divider);

        int index = Integer.valueOf(splitted[0]);
        String tag = splitted[1];

        return new CellLocationPair(index, tag);
    }
}
