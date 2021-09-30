package MessageBulletinBoard.data;

public class CellPair {
    private String value;
    private String tag;

    public String getValue() {
        return value;
    }

    public String getTag() {
        return tag;
    }

    public CellPair(String value, String tag){
        this.value = value;
        this.tag = tag;
    }

    @Override
    public boolean equals(Object obj) {
        CellPair pairToCompare = null;

        if (obj == null)
            return false;
        if (obj == this)
            return true;

        try {
            pairToCompare = (CellPair) obj;
        } catch (Exception e) {
            throw new IllegalArgumentException(obj + " is not a valid CellPair.");
        }

        return this.value.equals(pairToCompare.getValue()) &&  new String(this.tag).equals(new String(pairToCompare.getTag()));
    }
}
