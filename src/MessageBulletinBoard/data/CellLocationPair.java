package MessageBulletinBoard.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CellLocationPair {
    private int index;
    private String tag;

    public String getTagHash() {
        return tagHash;
    }

    private String tagHash;


    public int getIndex() {
        return index;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        this.tagHash = new String(md.digest(tag.getBytes()));

    }

    public CellLocationPair(int index, String tag) {
        this.index = index;
        this.tag = tag;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            this.tagHash = new String(md.digest(tag.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }


    @Override
    public boolean equals(Object obj) {
        CellLocationPair pairToCompare = null;

        if (obj == null)
            return false;
        if (obj == this)
            return true;

        try {
            pairToCompare = (CellLocationPair) obj;
        } catch (Exception e) {
            throw new IllegalArgumentException(obj + " is not a valid CellLocationPair.");
        }

        return this.index == pairToCompare.getIndex() && new String(this.tag).equals(new String(pairToCompare.getTag()));
    }
}
