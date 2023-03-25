package edu.ufp.inf.sd.rmi.red.model.token;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class Token {
    private static final int EXPINSECONDS = 25;
    private static final String ISSUER = "RedWarsService";
    private String value;

    public Token() {
        this.value = this.generate();
    }

    public Token(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    private String generate() {
        return JWT.create()
            .withIssuer(ISSUER)
            //.withExpiresAt(Date.from(Instant.now().plusSeconds(EXPINSECONDS)))
            .withIssuedAt(Date.from(Instant.now()))
            .sign(Algorithm.HMAC256(ISSUER));
    }

    public Token verifyToken() {
        // to verify token, expiration time must be greater than time of checking
        try {
            final DecodedJWT jwt = JWT.decode(value);
            if (jwt.getIssuer().compareTo(ISSUER) != 0) {return null;}
            //if (Date.from(Instant.now()).after(jwt.getExpiresAt())) {return null;}
            return this;
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }


    @Override
    public String toString() {
        return this.value;
    }
}
