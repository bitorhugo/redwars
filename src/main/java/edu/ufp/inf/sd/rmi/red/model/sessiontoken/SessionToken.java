package edu.ufp.inf.sd.rmi.red.model.sessiontoken;

import java.io.Serializable;

public class SessionToken implements Serializable {

    private String token;

    public SessionToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "{token=" + this.token + "}";
    }
        
}
