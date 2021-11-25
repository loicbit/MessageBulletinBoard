package MessageBulletinBoard.data;

import java.io.Serializable;

public class State implements Serializable {
    private String name = null;
    private String publicKey = null;
    private int cellLocationIndex = -1;
    private String tag = null;

    public State(String name, CellLocationPair cellLocationPairAB, CellLocationPair cellLocationPairBA){
        this.name = name;
        this.publicKey = publicKey;
        this.cellLocationIndex = -1;
        this.tag = null;
    }
    public State(String name, String sharedKey, CellLocationPair cellLocationPair){
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
