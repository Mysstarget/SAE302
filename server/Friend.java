package Server;

public class Friend {

    private String srcUser;
    private String dstUser;

    private String status;

    private boolean seenSrc;
    private boolean seenDst;

    public Friend(String srcUser, String dstUser) {

        this.srcUser = srcUser;
        this.dstUser = dstUser;

        this.status = "PENDING";

        this.seenSrc = true;
        this.seenDst = false;
    }

    public String getSrcUser() {
        return srcUser;
    }

    public String getDstUser() {
        return dstUser;
    }

    public String getStatus() {
        return status;
    }

    public void accept() {
        status = "ACCEPTED";
        seenSrc = false;
        seenDst = true;
    }

    public void refuse() {
        status = "REFUSED";
        seenSrc = false;
        seenDst = true;
    }

    public boolean isSeenSrc() {
        return seenSrc;
    }

    public boolean isSeenDst() {
        return seenDst;
    }

    public void setSeenSrc(boolean seenSrc) {
        this.seenSrc = seenSrc;
    }

    public void setSeenDst(boolean seenDst) {
        this.seenDst = seenDst;
    }
}