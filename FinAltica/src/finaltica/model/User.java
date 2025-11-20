package finaltica.model;

public class User {
    private int id;
    private String fullName;
    private String username;
    private String password;

    public User(int id, String fullName, String username, String password) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
    }

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    @Override
    public String toString() {
        return "User{id=" + id + ", fullName='" + fullName + "', username='" + username + "', password='[hidden]'}";
    }
}