package edu.ufp.inf.sd.rmi.red.model.user;

import java.io.Serializable;

import edu.ufp.inf.sd.rmi.red.model.session.Session;

public class User implements Serializable {

    private String username;
    private String secret;
    private Session session;

    public User(String username, String secret) {
        this.username = username;
        this.secret = secret;
        this.session = null;
    }

    public User(String username, String secret, Session session) {
        this.username = username;
        this.secret = secret;
        this.session = session;
    }
    
    /**
     * @return the uname
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * @param uname the uname to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the pword
     */
    public String getSecret() {
        return this.secret;
    }

    /**
     * @param pword the pword to set
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * @return the session
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * @param session session to be set
     */
    public void setSession(Session session) {
        this.session = session;
    }


    @Override
    public String toString() {
        return "User{username=" + this.username + ", secret=" + this.secret + "}";
    }
}
