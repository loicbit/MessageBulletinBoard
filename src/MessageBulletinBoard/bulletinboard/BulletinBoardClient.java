package MessageBulletinBoard.bulletinboard;

import MessageBulletinBoard.client.UserServerInterface;
import MessageBulletinBoard.data.CellLocationPair;

import java.nio.charset.Charset;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Random;

public class BulletinBoardClient {
    private Registry registry;
    String name = null;

    public HashMap<String, CellLocationPair> nextCellLocationPairAB = new HashMap<>();
    public HashMap<String, CellLocationPair> nextCellLocationPairBA = new HashMap<>();

    private BulletinBoardInterface bulletinServerStub;

    public BulletinBoardClient(String nameUser) throws RemoteException, NotBoundException {
        this.name = name;

        try{
            this.registry = LocateRegistry.createRegistry(BulletinBoardInterface.REG_PORT);
        }catch(Exception e) {
            this.registry = LocateRegistry.getRegistry(BulletinBoardInterface.REG_PORT);
        }

        this.bulletinServerStub = (BulletinBoardInterface) this.registry.lookup(BulletinBoardInterface.STUB_NAME);
        System.out.println(this.bulletinServerStub);

    }

    public void setNextCellLocationPairAB(String name, CellLocationPair next){
        if(!this.nextCellLocationPairAB.containsValue(name)) nextCellLocationPairAB.replace(name, next);
        else nextCellLocationPairAB.put(name, next);

    }

    public Boolean isNextCellLocationPairABSetted(String name){
        return this.nextCellLocationPairAB.containsKey(name);
    }

    public void setNextCellLocationPairBA(String name, CellLocationPair next){
        if(!this.nextCellLocationPairBA.containsValue(name)) nextCellLocationPairBA.replace(name, next);
        else nextCellLocationPairBA.put(name, next);

    }

    public void sendMessage(String nameContact, String message) throws RemoteException {
        CellLocationPair locationCurrentMessage = this.nextCellLocationPairAB.get(nameContact);

        if(locationCurrentMessage != null){
            this.nextCellLocationPairAB.remove(nameContact);

            //todo replace generator
            Random rand = new Random();
            int index = rand.nextInt(BulletinBoardInterface.NUMBER_CELLS *100)%BulletinBoardInterface.NUMBER_CELLS;


            byte[] array = new byte[BulletinBoardInterface.securityParam];
            new Random().nextBytes(array);
            String tag = new String(array, Charset.forName("ASCII"));

            CellLocationPair nextLocationCell = new CellLocationPair(index, tag);

            //todo clear and only save the hash
            this.nextCellLocationPairAB.put(nameContact, nextLocationCell);

            String uMessage = message + BulletinBoardInterface.messageDiv + index + BulletinBoardInterface.messageDiv + tag;
            this.bulletinServerStub.add(locationCurrentMessage.getIndex(), uMessage, locationCurrentMessage.getTag());
        } throw new NullPointerException("No cell to send");

    }

    public String getMessage(String nameContact) throws RemoteException{
        if(this.nextCellLocationPairBA.containsKey(nameContact)){
            CellLocationPair nextLocation = this.nextCellLocationPairBA.get(nameContact);

            String message = this.bulletinServerStub.get(nextLocation.getIndex(), nextLocation.getTag());

            if(message != null){
                this.nextCellLocationPairBA.remove(nameContact);
                String splitted[] = message.split(CellLocationPair.divider);
                String messageCell = splitted[0];
                int nextIdx= Integer.valueOf(splitted[1]);
                String nextTag = splitted[2];

                CellLocationPair nextPair = new CellLocationPair(nextIdx, nextTag);

                this.nextCellLocationPairBA.put(nameContact, nextPair);

                return messageCell;
            }else return null;



        }else return null;

    }
}
