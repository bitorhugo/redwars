package edu.ufp.inf.sd.rmi.red.model.db;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.ufp.inf.sd.rmi.red.model.token.Token;
import edu.ufp.inf.sd.rmi.red.model.user.User;

public class VolatileDB implements DBI {

    Map<String, User> users = Collections.synchronizedMap(new HashMap<>());

    public Map<String, User> getUsers() {
        return this.users;
    }

    public void setUsers (Map<String, User> users) {
        this.users = users;
    }

    @Override
    public Optional<User> insert(String username, String secret) {
        User u = null;
        if (!this.users.containsKey(username)) {
            Token t = new Token();
            String hash = this.hash(secret).orElseThrow(); // hash secret
            u = new User(username, hash, t);
            this.users.put(username, u);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "User {0} inserted", username);
        }
        return Optional.ofNullable(u);
    }

    @Override
    public Optional<User> select(String username, String secret) {
        User u = users.get(username);
        if (u != null && u.getSecret().compareTo(hash(secret).orElseThrow()) == 0) {
            return Optional.ofNullable(u);
        }
        return Optional.ofNullable(null);
    }

    @Override
    public Optional<User> select(String username) {
        User u = users.get(username);
        return Optional.ofNullable(u);
    }

    @Override
    public Optional<User> update(User u) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean delete(User u) {
        // TODO Auto-generated method stub
        return false;
    }

    private Optional<String> hash(String input) {
        String hash = null;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            hash  = toHexString(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(hash);
    }

    private static String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 64)
            {
                hexString.insert(0, '0');
            }
        return hexString.toString();
    }
    
}
