package MessageBulletinBoard.data;

import java.io.Serializable;

public class State implements Serializable {
    private String name = null;
    private String publicKey = null;
    private int cellLocationIndex = -1;
    private String tag = null;

    //todo: maybe remove publicKey from constructor
    public State(String name, String publicKey){
        this.name = name;
        this.publicKey = publicKey;
        this.cellLocationIndex = -1;
        this.tag = null;
    }
    public State(String name, String publicKey, int cellLocationIndex, String tag){
        this.name = name;
        this.publicKey = publicKey;
        this.cellLocationIndex = cellLocationIndex;
        this.tag = tag;
    }

    public void setCellLocationIndex(int index){
        this.cellLocationIndex = index;
    }

    public void setTag(String tag){
        this.tag = tag;
    }
}
