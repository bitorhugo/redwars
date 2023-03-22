package edu.ufp.inf.sd.rmi.red.model.db;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import edu.ufp.inf.sd.rmi.red.model.session.Session;
import edu.ufp.inf.sd.rmi.red.model.user.User;

public class DB implements DBI {

    private String name;

    public DB(String name) {
        this.name = name;
    }
    
    @Override
    public Optional<User> insert(String username, String secret) {
        User u = null;
        Session session = new Session(); // create new SessionToken
        String hash = this.hash(secret).orElseThrow();
        String sql = "INSERT INTO User (username, secret, token) " +
            "VALUES('" + username + "', '" + hash + "', '" + session.getToken() + "');"; 
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            stmt.close();
            this.close(conn);
            u = new User(username, hash, session);
        } catch (SQLException e) {
            System.err.println(e);
        }
        System.out.println("Inserted: " + u);
        return Optional.ofNullable(u);
    }

    @Override
    public Optional<User> update(User u) {
        return null;
    }

    @Override
    public boolean delete(User u) {
        return false;
    }

    @Override
    public Optional<User> select(String username, String secret) {
        User user = null;
        String hash = this.hash(secret).orElseThrow();
        String sql = "SELECT * FROM User u where u.username = '" + username + "' and secret = '" + hash + "' limit 1";
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                String u = rs.getString("username");
                String s = rs.getString("secret");
                Session session = new Session();
                session.setToken(rs.getString("token"));
                user = new User(u, s, session);
            }
            rs.close();
            stmt.close();
            this.close(conn);
        } catch (SQLException e) {
            System.err.println(e);
        }
        System.out.println(user);
        return Optional.ofNullable(user);
    }

    private Connection connect() throws SQLException{
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String connection_string = "jdbc:sqlite:" + this.name;
        return DriverManager.getConnection(connection_string);
    }

    private void close(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
