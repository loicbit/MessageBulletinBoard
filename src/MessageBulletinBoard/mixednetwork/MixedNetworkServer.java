package MessageBulletinBoard.mixednetwork;

import MessageBulletinBoard.authenticationserver.AuthenticationClient;
import MessageBulletinBoard.authenticationserver.AuthenticationServerInterface;
import MessageBulletinBoard.bulletinboard.BulletinBoardClient;
import MessageBulletinBoard.bulletinboard.BulletinBoardInterface;
import MessageBulletinBoard.crypto.DiffieH;
import MessageBulletinBoard.data.CellLocationPair;
import MessageBulletinBoard.data.INFO_MESSAGE;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;


public class MixedNetworkServer implements MixedNetworkServerInterface {
    static Registry registry = null;

    private Signature sign = null;
    private PublicKey publicKeySignature = null;
    private AuthenticationClient authenticationClient = null;

    private BulletinBoardClient bulletinBoardClient = null;

    private final HashMap<String, byte[]> cellStates = new HashMap<>();
    private final HashMap<String, LinkedList<byte[]>> validTokens = new HashMap<>();

    private final HashMap<String, DiffieH> diffieEncrypt = new HashMap<>();

    public MixedNetworkServer() throws Exception {
        int randomId = (int) Math.floor(Math.random() * 1000);
        String authName = MixedNetworkServerInterface.DEF_NAME + " " + randomId;

        this.bulletinBoardClient = new BulletinBoardClient();
        System.err.println("Bulletinboard server connected");
        this.authenticationClient = new AuthenticationClient(authName, false);
        System.err.println("Authentication server connected");

        this.publicKeySignature = this.authenticationClient.getPublicSignKey();
        if (this.publicKeySignature != null) {
            this.initVerifyToken();
            System.err.println("Got publicKey for verification tokens");
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            MixedNetworkServer obj = new MixedNetworkServer();
            MixedNetworkServerInterface stub = (MixedNetworkServerInterface) UnicastRemoteObject.exportObject(obj, 0);

            try {
                registry = LocateRegistry.createRegistry(MixedNetworkServerInterface.REG_PORT);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(MixedNetworkServerInterface.REG_PORT);
            }

            String nameReg = MixedNetworkServerInterface.DEF_PATH;
            registry.bind(nameReg, stub);

            System.err.println("Server ready");

            obj.connectBulletinBoard();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @Override
    public PublicKey initContact(String name, PublicKey publicKeyOther) throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        this.validTokens.put(name, new LinkedList<>());
        this.diffieEncrypt.put(name, new DiffieH(false));
        this.diffieEncrypt.get(name).generateSecretKeyObject(publicKeyOther);
        return this.diffieEncrypt.get(name).getPubkeyObject();
    }

    @Override
    public byte[] get(byte[] indexEnc, byte[] tagEnc, byte[] tokenEnc, byte[] hashEnc, String nameUser) throws Exception {
        byte[] token = this.diffieEncrypt.get(nameUser).decryptBytes(tokenEnc);

        if (verifyToken(token, nameUser)) {
            String indexStr = new String(this.diffieEncrypt.get(nameUser).decryptBytes(indexEnc));
            String tag = new String(this.diffieEncrypt.get(nameUser).decryptBytes(tagEnc));

            byte[] stateHash = this.diffieEncrypt.get(nameUser).decryptBytes(hashEnc);
            int index = Integer.parseInt(indexStr);

            if (verifyState(stateHash, index, tag)) {
                String message = this.bulletinBoardClient.get(index, tag);

                if (message != null) {
                    return this.diffieEncrypt.get(nameUser).encryptBytes(message.getBytes());
                }
            } else {
                return BulletinBoardInterface.emptyMessage;
            }
        }
        return BulletinBoardInterface.emptyMessage;
    }

    @Override
    public void add(byte[] indexEnc, byte[] valueEnc, byte[] tagEnc, byte[] tokenEnc, byte[] hashEnc, String nameUser) throws Exception {
        String indexStr = new String(this.diffieEncrypt.get(nameUser).decryptBytes(indexEnc));
        String value = new String(this.diffieEncrypt.get(nameUser).decryptBytes(valueEnc));
        String tag = new String(this.diffieEncrypt.get(nameUser).decryptBytes(tagEnc));
        byte[] token = this.diffieEncrypt.get(nameUser).decryptBytes(tokenEnc);

        byte[] stateHash = this.diffieEncrypt.get(nameUser).decryptBytes(hashEnc);
        int index = Integer.parseInt(indexStr);

        if (verifyToken(token, nameUser)) {
            this.addState(stateHash, index, tag);
            this.bulletinBoardClient.add(index, value, tag);
        }
    }

    private void connectBulletinBoard() throws Exception {
        this.bulletinBoardClient = new BulletinBoardClient();
    }


    private void initVerifyToken() throws NoSuchAlgorithmException, InvalidKeyException {
        this.sign = Signature.getInstance(AuthenticationServerInterface.SIGN_INSTANCE);
        this.sign.initVerify(this.publicKeySignature);
    }

    private boolean verifyToken(byte[] tokenSigned, String user) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException, RemoteException {
        //return true;

        Boolean valid = false;

        if (this.publicKeySignature == null) {
            return false;
        }

        boolean allRecev = false;

        while (!allRecev) {
            byte[] token = this.authenticationClient.getTokensSign(user);

            if (new String(token).equals(INFO_MESSAGE.NO_TOKENS_AIV.name())) allRecev = true;
            else {
                this.validTokens.get(user).add(token);
            }
        }

        //check if all tokens received
        if (this.validTokens.get(user).isEmpty()) {
            return false;
        }

        for (byte[] token : this.validTokens.get(user)) {
            Signature signVerify = Signature.getInstance(AuthenticationServerInterface.SIGN_INSTANCE);
            signVerify.initVerify(this.publicKeySignature);
            signVerify.update(token);

            if (signVerify.verify(tokenSigned)) {
                valid = true;
                break;
            }
        }
        return valid;
    }


    private void addState(byte[] hash, int index, String tag) {
        CellLocationPair cellPair = new CellLocationPair(index, tag);
        String key = cellPair.toString();

        this.cellStates.put(key, hash);
    }

    private boolean verifyState(byte[] hash, int index, String tag) {
        CellLocationPair cellPair = new CellLocationPair(index, tag);
        // The hash of the tag is used in the key.
        String keyString = cellPair.getIndex() + CellLocationPair.divider + cellPair.getTagHash();

        if (this.cellStates.get(keyString) != null) {
            return Arrays.equals(hash, this.cellStates.get(keyString));
        }
        return false;
    }

}
