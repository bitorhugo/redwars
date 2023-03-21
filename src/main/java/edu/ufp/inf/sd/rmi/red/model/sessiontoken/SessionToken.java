package edu.ufp.inf.sd.rmi.red.model.sessiontoken;

import java.io.Serializable;
import java.time.Instant;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

public class SessionToken implements Serializable {

    private static final int EXPINSECONDS = 5;
    private String token;

    public SessionToken(String issuer) {
        this.token = JWT.create().withIssuer(issuer).withExpiresAt(Instant.now().plusSeconds(EXPINSECONDS)).sign(Algorithm.none());
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
