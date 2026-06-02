package Server;

public class Groupe {

    private int idGroup;
    private String groupName;
    private String owner;

    public Groupe(int idGroup, String groupName, String owner) {

        this.idGroup = idGroup;
        this.groupName = groupName;
        this.owner = owner;
    }

    public int getIdGroup() {
        return idGroup;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getOwner() {
        return owner;
    }
}

