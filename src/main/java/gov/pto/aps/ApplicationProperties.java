package gov.pto.aps;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "uspto")
public class ApplicationProperties {
    
    private String userFilePath;
	private String serverURL;
	private String activitiURL;
	private String userName;
	private String password;


    public String getUserFilePath() {
        return userFilePath;
    }
    public void setUserFilePath(String userFilePath) {
        this.userFilePath = userFilePath;
    }
    public String getServerURL() {
        return serverURL;
    }
    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }
    public String getActivitiURL() {
        return activitiURL;
    }
    public void setActivitiURL(String activitiURL) {
        this.activitiURL = activitiURL;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    

    
}
