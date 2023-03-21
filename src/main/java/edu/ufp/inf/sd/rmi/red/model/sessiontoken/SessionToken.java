package edu.ufp.inf.sd.rmi.red.model.sessiontoken;

import java.io.Serializable;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

public class SessionToken implements Serializable {

    private static final int EXPINSECONDS = 5;
    private String token;

    public SessionToken(String issuer) {
        try {
            this.token = JWT.create().withIssuer(issuer).sign(Algorithm.none());
        } catch (Exception e) {
            System.err.println(e);
        }
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
