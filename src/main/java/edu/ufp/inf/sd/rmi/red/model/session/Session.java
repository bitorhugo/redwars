package edu.ufp.inf.sd.rmi.red.model.session;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

public class Session implements Serializable {

    private static final int EXPINSECONDS = 5;
    private static final String ISSUER = "RedWarsService";
    private String token;

    public Session() {
        this.token = this.generateToken();
    }
    
    public String generateToken() {
        return JWT.create()
            .withIssuer(ISSUER)
            .withExpiresAt(Date.from(Instant.now().plusSeconds(EXPINSECONDS)))
            .withIssuedAt(Date.from(Instant.now()))
            .sign(Algorithm.none());
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
