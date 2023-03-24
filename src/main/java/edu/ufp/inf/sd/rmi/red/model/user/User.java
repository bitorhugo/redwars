package edu.ufp.inf.sd.rmi.red.model.user;

import java.io.Serializable;
import java.util.Optional;

import edu.ufp.inf.sd.rmi.red.model.token.Token;

public class User implements Serializable {

    private String username;
    private String secret;
    private Token token;


    public User(String username, String secret) {
        this.username = username;
        this.secret = secret;
    }

    public User(String username, String secret, Token token) {
        this.username = username;
        this.secret = secret;
        this.token = token;
    }

    public User(String username, String secret, String token) {
        this.username = username;
        this.secret = secret;
        this.token = new Token(token);
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

    public Optional<Token> getToken() {
        return Optional.ofNullable(this.token.verifyToken());
    }

    public void setToken(Token token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "User{username=" + this.username + ", secret=" + this.secret + ", token=" + this.token + "}";
    }
}
