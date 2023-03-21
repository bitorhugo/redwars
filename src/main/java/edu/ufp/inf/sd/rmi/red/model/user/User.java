package edu.ufp.inf.sd.rmi.red.model.user;

import java.io.Serializable;

import edu.ufp.inf.sd.rmi.red.model.sessiontoken.SessionToken;

public class User implements Serializable {

    private String username;
    private String secret;
    private SessionToken token;

    public User(String username, String secret) {
        this.username = username;
        this.secret = secret;
        this.token = null;
    }

    /**
     * @return the uname
     */
    public String getUname() {
        return this.username;
    }

    /**
     * @param uname the uname to set
     */
    public void setUname(String uname) {
        this.username = uname;
    }

    /**
     * @return the pword
     */
    public String getPword() {
        return this.secret;
    }

    /**
     * @param pword the pword to set
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * @return the token
     */
    public SessionToken getToken() {
        return this.token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(SessionToken token) {
        this.token = token;
    }


    @Override
    public String toString() {
        return "User{" + "uname=" + username + ", secret=" + this.secret + "}";
    }
}