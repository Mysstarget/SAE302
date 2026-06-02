
public class Message {

    private int idMsg;

    private String srcUser;

    private String dstUser;

    private String dstGroup;

    private String type;

    private String content;

    private boolean delivered;

    private boolean read;

    public Message(int idMsg, String srcUser, String dstUser, String dstGroup, String type, String content) {
        this.idMsg = idMsg;
        this.srcUser = srcUser;
        this.dstUser = dstUser;
        this.dstGroup = dstGroup;

        this.type = type;
        this.content = content;

        this.delivered = false;
        this.read = false;
    }

    public int getIdMsg() {
        return idMsg;
    }

    public String getSrcUser() {
        return srcUser;
    }

    public String getDstUser() {
        return dstUser;
    }

    public String getDstGroup() {
        return dstGroup;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}