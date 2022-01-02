package MessageBulletinBoard.client;

import MessageBulletinBoard.authenticationserver.AuthenticationClient;
import MessageBulletinBoard.authenticationserver.AuthenticationServerInterface;
import MessageBulletinBoard.data.CellLocationPair;
import MessageBulletinBoard.data.INFO_MESSAGE;
import org.apache.commons.lang3.EnumUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.security.Key;
import java.util.*;
import java.util.Timer;

public class UserGui {
    private JPanel panel;
    private JTextField textFieldNameUser;
    private JButton buttonConfirmNameUser;
    private JTextField textFieldNewContact;
    private JButton buttonConfirmContactName;
    private JComboBox comboBoxContacts;
    private JTextArea textAreaConversation;
    private JTextField textFieldMessage;
    private JButton buttonSendMessage;
    private JButton buttonConnect;
    private JButton buttonGetAuthenticated;

    private UserServer userServer;
    private HashMap<String, UserClient>  userClient = new HashMap();

    private AuthenticationClient authClient;
    private AuthenticationServerInterface authServerStub;

    //AssymEncrypt assymEncrypt = new AssymEncrypt();

    //private BulletinBoardClient boardClient;

    private HashMap<String, String> conversation = new HashMap();
    private String nameUser;

    public UserGui() {
        buttonConfirmNameUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    setUser(textFieldNameUser.getText());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        buttonConfirmContactName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    addContact(textFieldNewContact.getText());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        buttonConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentContact = String.valueOf(comboBoxContacts.getSelectedItem());
                try {
                    connectContact(currentContact);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        buttonSendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentContact = String.valueOf(comboBoxContacts.getSelectedItem());
                try {
                    sendMessage(currentContact, textFieldMessage.getText());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        buttonGetAuthenticated.addActionListener(new ActionListener() {
            //todo: throw except username not yet set.
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    authenticate();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        Timer t = new Timer();

        TimerTask messagePoller = new TimerTask() {
            @Override
            public void run() {
                String currentContact = String.valueOf(comboBoxContacts.getSelectedItem());
                try {
                    getMessage(currentContact);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };

        t.schedule(messagePoller, new Date(), 1000);
        comboBoxContacts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateConversation();
            }
        });

        this.textAreaConversation.setBorder(new JTextField().getBorder());
    }

    private void setUser(String nameUser) throws Exception {
        this.textFieldNameUser.setEnabled(false);
        this.buttonConfirmNameUser.setEnabled(false);
        this.nameUser = nameUser;

        this.authClient = new AuthenticationClient(this.nameUser, true);
        this.authClient.initAuthServer(false);





        try{
            this.userServer = new UserServer(nameUser);
        }catch(Exception ex){
            System.out.println(ex);
            //todo: print notification
            //      use different exceptions to notify
        }
    }

    private INFO_MESSAGE authenticate() throws Exception {
        // todo notify if it fails
        //
        String currentContact = String.valueOf(comboBoxContacts.getSelectedItem());

        if(this.userClient.get(currentContact)!=null){
            LinkedList tokens = this.authClient.getTokens();
            this.userClient.get(currentContact).addTokens(tokens);
            if(tokens != null){
                return INFO_MESSAGE.TOKENS_RECV;
            }else {
                return INFO_MESSAGE.TOKENS_NOT_RECV;
            }

        }
        else return INFO_MESSAGE.NO_CONTACT;

    }

    private void addContact(String nameContact) throws Exception {
        //todo : fix init with new, if you can't find them
        comboBoxContacts.addItem(nameContact);

        connectContact(nameContact);
    }

    private void connectContact(String nameContact) throws Exception {
        //todo: connect new user
        Boolean exchanged = false;

        if(!this.userClient.containsKey(nameContact)){
            this.userClient.put(nameContact, new UserClient(nameContact, this.nameUser));
            this.authenticate();
        }

        if(!this.isConnected(nameContact)){
            try{
                exchanged =  this.userClient.get(nameContact).symmetricKeyExchange(nameContact);
                Key publicKeyOther = this.userServer.getPublicKeyContact(nameContact);
                this.userClient.get(nameContact).setPublicKeyContact(publicKeyOther);

                this.userClient.get(nameContact).sendPublicKeys();

            }catch(RemoteException ex){
                //todo: note contact not available to connect
            }

            if(exchanged){
                this.buttonConfirmContactName.setEnabled(false);
            }

        }else{
            //todo message already connected
        }
    }

    private void sendMessage(String name, String message) throws Exception {
        //todo handle null client
        if(this.userClient.containsKey(name) && this.userClient.get(name).isConnected()){
            //this.to
            INFO_MESSAGE sent=  this.userClient.get(name).sendMessageBoard(message);

            if(sent!=INFO_MESSAGE.MESSAGE_SENT){
                if(sent==INFO_MESSAGE.NO_TOKENS_AIV){
                    LinkedList tokens = this.authClient.getTokens();
                    this.userClient.get(name).addTokens(tokens);
                    sent =  this.userClient.get(name).sendMessageBoard(message);
                }
            }
        }else{
            //todo: print error message
        }
    }

    private void getMessage(String name) throws Exception {
        String currentConv = "";

        if(this.userClient.containsKey(name) && this.isConnected(name)){
            String newMessage = this.userClient.get(name).getMessageBoard();

            if(EnumUtils.isValidEnum(INFO_MESSAGE.class, newMessage)){
                if(INFO_MESSAGE.valueOf(newMessage)==INFO_MESSAGE.NO_TOKENS_AIV){
                    LinkedList tokens = this.authClient.getTokens();
                    this.userClient.get(name).addTokens(tokens);
                }
                newMessage = this.userClient.get(name).getMessageBoard();
            }

            if(newMessage != null){
                newMessage = '\n'+ newMessage;

                if(this.conversation.containsKey(name)){
                    currentConv = this.conversation.get(name);
                    currentConv += newMessage;

                    this.conversation.replace(name, currentConv);
                }else{
                    this.conversation.put(name, newMessage);
                }
                updateConversation();
            }
        }
    }

    // todo update layout
    private void updateConversation(){
        String currentContact = String.valueOf(comboBoxContacts.getSelectedItem());

        String textToShow = this.conversation.get(currentContact);

        this.textAreaConversation.setText(textToShow);
    }

    private boolean isConnected(String contactName){
        boolean statusClient =  this.userClient.get(contactName).isConnected();
        boolean statusServer = this.userServer.isConnected(contactName);

        if(!statusClient && statusServer){
            CellLocationPair cellAB = this.userServer.getFirstCellAB(contactName);
            CellLocationPair cellBA = this.userServer.getFirstCellBA(contactName);

            this.userClient.get(contactName).setFirstCellPair(cellAB, cellBA);


            if(cellAB != null && cellBA != null) return true;
            else return false;

        }else if(statusClient){
            return true;
        }

        //todo check if contact is connected via client or server
        return false;
    }

    private void updateState(){
        //get latest state of bullentin, send to server.
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("User");

        frame.setContentPane(new UserGui().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
