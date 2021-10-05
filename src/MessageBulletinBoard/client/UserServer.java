package MessageBulletinBoard.client;

import MessageBulletinBoard.bulletinboard.BulletinBoardInterface;
import MessageBulletinBoard.data.CellLocationPair;
import MessageBulletinBoard.data.State;

import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class UserServer implements UserServerInterface{
    private String firstStateHash = null;
    private int securityParam = 8;
    private String publicKey = null;

    private HashMap<String, String> publickeys= new HashMap<>();

    private HashMap<String, State> firstStates= new HashMap<>();
    private HashMap<String, Integer> firstStateHashes= new HashMap<>();
    private Registry registry = null;

    private HashMap<String, CellLocationPair> firsCellsAB = new HashMap();
    private HashMap<String, CellLocationPair> firsCellsBA = new HashMap();

    public UserServer(String nameUser) throws Exception{
        //todo: generate publickey
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
    public String initContact(String name, String publicKey) throws RemoteException {
        //todo: add asyncrhone encryption
        this.publickeys.put(name, publicKey);

        State newState = new State(name, publicKey);
        this.firstStates.put(name, newState);

        return this.publicKey;
    }

    @Override
    public String getFirstCell(String name, String cellToReceive) throws RemoteException {
        // Todo: replace tag and index generator to one place and change to method



        CellLocationPair newCellAB = generateNewCell();
        CellLocationPair newCellBA = convertToObject(cellToReceive);

        /*
        State stateContact = this.firstStates.get(name);
        this.firstStates.remove(name);

        stateContact.setCellLocationIndex(newCellAB.getIndex());
        stateContact.setTag(newCellAB.getTag());

        int stateHash = Objects.hashCode(stateContact);
        firstStateHashes.put(name, stateHash);*/


        //todo remove cell save
        this.firsCellsAB.put(name, newCellAB);
        this.firsCellsBA.put(name, newCellBA);

        return  newCellAB.getIndex() + CellLocationPair.divider + newCellAB.getTag();
    }

    private CellLocationPair generateNewCell(){
        Random rand = new Random();
        int index = rand.nextInt(BulletinBoardInterface.NUMBER_CELLS *100)%BulletinBoardInterface.NUMBER_CELLS;

        byte[] array = new byte[BulletinBoardInterface.securityParam];
        new Random().nextBytes(array);
        String tag = new String(array, Charset.forName("ASCII"));

        return new CellLocationPair(index, tag);
    }

    private CellLocationPair convertToObject(String pairString){
        String splitted[] = pairString.split(CellLocationPair.divider);

        int index = Integer.valueOf(splitted[0]);
        String tag = splitted[1];

        return new CellLocationPair(index, tag);
    }

    //private CellLocationPair generateFirstCell
}
