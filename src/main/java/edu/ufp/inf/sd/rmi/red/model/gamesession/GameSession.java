package edu.ufp.inf.sd.rmi.red.model.gamesession;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;


public class GameSession extends UnicastRemoteObject implements GameSessionRI {

    private static final int EXPINSECONDS = 120;
    private static final String ISSUER = "RedWarsService";
    private String token;

    // availableGames will contain all games that were created
    // the integer will represent the game ID
    // the String will be the absolute path on the server where the diretory of savegame.properties file of that specific game is stored
    // the save folder will be called 'saves' and each sub-directory name will be the ID of the game
    // e.g.
    // saves/
    //      0/savegame.properties
    //      1/savageame/properties
    Map<Integer, String> availableGames = new ConcurrentHashMap<>();
    
    public GameSession() throws RemoteException {
        super();
        this.token = this.generateToken();
    }
    
    public String generateToken() {
        return JWT.create()
            .withIssuer(ISSUER)
            //.withExpiresAt(Date.from(Instant.now().plusSeconds(EXPINSECONDS)))
            .withIssuedAt(Date.from(Instant.now()))
            .sign(Algorithm.HMAC256(ISSUER));
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public GameSession verifyToken() {
        // to verify token, expiration time must be greater than time of checking
        try {
            final DecodedJWT jwt = JWT.decode(token);
            if (jwt.getIssuer().compareTo(ISSUER) != 0) {return null;}
            //if (Date.from(Instant.now()).after(jwt.getExpiresAt())) {return null;}
            return this;
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }

    @Override
    public void test(String s) throws RemoteException {
        System.out.println(s);
    }

    @Override
    public String toString() {
        return "{token=" + this.token + "}";
    }
        
}
