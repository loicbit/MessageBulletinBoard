package MessageBulletinBoard.mixednetwork;

import MessageBulletinBoard.bulletinboard.BulletinBoardInterface;
import MessageBulletinBoard.crypto.AssymEncrypt;
import org.apache.commons.lang3.SerializationUtils;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Key;
import java.util.LinkedList;
import java.util.List;

public class MixedNetworkClient {
    private List<byte []> tokens;
    private MixedNetworkServerInterface tokenServerStub;
    private Registry registry;
    private String nameUser;
    private AssymEncrypt assymEncrypt;
    private Key publicKeyTokenServer;

    public MixedNetworkClient(String nameUser) throws Exception {
        this.nameUser = nameUser;
        this.tokens = new LinkedList<>();
        this.assymEncrypt = new AssymEncrypt();

        try{
            this.registry = LocateRegistry.createRegistry(BulletinBoardInterface.REG_PORT);
        }catch(Exception e) {
            this.registry = LocateRegistry.getRegistry(BulletinBoardInterface.REG_PORT);
        }

        try{
            this.tokenServerStub = (MixedNetworkServerInterface) this.registry.lookup(MixedNetworkServerInterface.DEF_PATH);
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public void initTokenServer() throws RemoteException {
        //todo: return boolean if succeed
        byte[] publicKeyUserSer = this.assymEncrypt.getPublicKeySer();

        byte[] publicKeyOtherSer = this.tokenServerStub.initContact(this.nameUser, publicKeyUserSer);
        this.publicKeyTokenServer = SerializationUtils.deserialize(publicKeyOtherSer);
    }

    private boolean verifyTokens(List<byte[]> tokens){
        return false;
    }
}
