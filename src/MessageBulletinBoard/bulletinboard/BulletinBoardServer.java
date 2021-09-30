package MessageBulletinBoard.bulletinboard;

import MessageBulletinBoard.data.BulletinCell;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import MessageBulletinBoard.bulletinboard.*;

import java.util.LinkedList;

public class BulletinBoardServer implements BulletinBoardInterface{
    private BulletinCell cells[] = null;
    private int NUMBER_CELLS = 300;

    static Registry registry = null;

    public BulletinBoardServer(){
        this.cells = new BulletinCell[NUMBER_CELLS];

        for(int i=0; i< NUMBER_CELLS; i++){
            this.cells[i] = new BulletinCell();
        }
    }

    public static void main(String[] args){
        try {
            BulletinBoardServer obj = new BulletinBoardServer();
            BulletinBoardInterface stub = (BulletinBoardInterface) UnicastRemoteObject.exportObject(obj, 0);



            try{
                registry = LocateRegistry.createRegistry(2001);
            }catch(Exception e) {
                registry = LocateRegistry.getRegistry(2001);
            }

            registry.bind("rmi:bulletinboard", stub);

            System.err.println("Server ready");
        }catch (Exception e) {
            System.out.println("BulletinBoard Server failed: " + e);
        }
    }

    @Override
    public String get(int i, String b) throws RemoteException {
        return null;
    }

    @Override
    public void add(int index, String value, String tag) throws RemoteException {

    }
}
