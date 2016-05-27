package sergicarolbosch.needmorecookies;

/**
 * This class contains information about the user and the current session
 */

// Shows name, email and other info
public class User_Info {

    // Global variables for class
    // Name,email
    private String name;
    private String email;
    // Offline mode
    private boolean offline_mode;

    //Instantiate
    private static User_Info user_info = new User_Info();
    public static User_Info getInstance(){
        return user_info;
    }

    // Dummy class init
    public User_Info(){}

    // Actual class init
    public User_Info(String name,String email){
        super();
        // Set variable with values
        this.name = name;
        this.email = email;
    }

    // Get values
    public String getName(){
        return name;
    }
    public String getEmail() {
        return email;
    }
    public Boolean getOffline_mode() { return offline_mode;}

    // Set values
    public void setName(String name) {
        this.name = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setOffline_mode(Boolean status) {this.offline_mode = status;}

}
