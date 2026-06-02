package Server;

public class GroupMember {

    private int idGroup;

    private String username;

    private String role;

    private boolean seenJoin;

    private int lastSeenMsgId;

    public GroupMember(int idGroup, String username, String role) {

        this.idGroup = idGroup;
        this.username = username;
        this.role = role;

        this.seenJoin = false;
        this.lastSeenMsgId = 0;
    }

    public int getIdGroup() {
        return idGroup;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean isSeenJoin() {
        return seenJoin;
    }

    public void setSeenJoin(boolean seenJoin) {
        this.seenJoin = seenJoin;
    }

    public int getLastSeenMsgId() {
        return lastSeenMsgId;
    }

    public void setLastSeenMsgId(int lastSeenMsgId) {
        this.lastSeenMsgId = lastSeenMsgId;
    }
}