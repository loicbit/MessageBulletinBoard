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

    CellLocationPair nextCellLocationPairAB = null;
    CellLocationPair nextCellLocationPairBA = null;

    private BulletinBoardInterface bulletinServerStub;

    public BulletinBoardClient(String contact) throws RemoteException, NotBoundException {
        //this.name = name;

        try{
            this.registry = LocateRegistry.createRegistry(BulletinBoardInterface.REG_PORT);
        }catch(Exception e) {
            this.registry = LocateRegistry.getRegistry(BulletinBoardInterface.REG_PORT);
        }

        this.bulletinServerStub = (BulletinBoardInterface) this.registry.lookup(BulletinBoardInterface.STUB_NAME);
        System.out.println(this.bulletinServerStub);

    }

    public void setNextCellLocationPairAB(CellLocationPair next){
        this.nextCellLocationPairAB = next;

    }

    public Boolean isNextCellLocationPairABSetted(String name){
         return nextCellLocationPairAB != null;
    }

    public void setNextCellLocationPairBA(CellLocationPair next){
        this.nextCellLocationPairBA = next;
    }

    public void sendMessage(String message) throws RemoteException {
        CellLocationPair locationCurrentMessage = this.nextCellLocationPairAB;

        if(locationCurrentMessage != null){
            this.nextCellLocationPairAB = null;

            //todo replace generator
            Random rand = new Random();
            int index = rand.nextInt(BulletinBoardInterface.NUMBER_CELLS *100)%BulletinBoardInterface.NUMBER_CELLS;


            byte[] array = new byte[BulletinBoardInterface.securityParam];
            new Random().nextBytes(array);
            String tag = new String(array, Charset.forName("ASCII"));

            CellLocationPair nextLocationCell = new CellLocationPair(index, tag);

            //todo clear and only save the hash
            this.nextCellLocationPairAB = nextLocationCell;

            String uMessage = message + BulletinBoardInterface.messageDiv + index + BulletinBoardInterface.messageDiv + tag;
            this.bulletinServerStub.add(locationCurrentMessage.getIndex(), uMessage, locationCurrentMessage.getTag());
        } throw new NullPointerException("No cell to send");

    }

    public String getMessage() throws RemoteException{
        if(this.nextCellLocationPairBA != null){
            CellLocationPair nextLocation = this.nextCellLocationPairBA;

            String message = this.bulletinServerStub.get(nextLocation.getIndex(), nextLocation.getTag());

            if(message != null){
                this.nextCellLocationPairBA=null;
                String splitted[] = message.split(BulletinBoardInterface.messageDiv);
                String messageCell = splitted[0];
                int nextIdx= Integer.valueOf(splitted[1]);
                String nextTag = splitted[2];

                CellLocationPair nextPair = new CellLocationPair(nextIdx, nextTag);

                this.nextCellLocationPairBA = nextPair;

                return messageCell;
            }else return null;



        }else return null;

    }

    public boolean isConnected(){
        return this.nextCellLocationPairAB !=null && this.nextCellLocationPairBA != null;
    }
}
