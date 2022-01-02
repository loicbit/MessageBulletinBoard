package MessageBulletinBoard.data;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class BulletinCell {
    public Set<CellPair> getCellPairs() {
        return cellPairs;
    }

    private final Set<CellPair> cellPairs = new HashSet<>();


    public BulletinCell() {

    }
    public BulletinCell(CellPair cellPair) {
        this.cellPairs.add(cellPair);
    }

    public BulletinCell(LinkedList<CellPair> cellPairs) {
        this.cellPairs.addAll(cellPairs);
    }

    public void removePair(CellPair pair){
        this.cellPairs.remove(pair);
    }

    public void addPair(CellPair pair){
        this.cellPairs.add(pair);
    }
}
