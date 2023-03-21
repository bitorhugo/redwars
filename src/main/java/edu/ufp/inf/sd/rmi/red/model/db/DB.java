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

import com.auth0.jwt.algorithms.Algorithm;

import edu.ufp.inf.sd.rmi.red.model.sessiontoken.SessionToken;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserNotFoundException;
import edu.ufp.inf.sd.rmi.red.model.user.User;

public class DB implements DBI {

    private String name;

    public DB(String name) {
        this.name = name;
    }
    
    @Override
    public void insert(String username, String secret) {
        SessionToken token = new SessionToken(username);
        String sql = "INSERT INTO User (username,secret, token) " +
                "VALUES('" + username + "', '" + secret + "', '" + token.getToken() + "')'"; 
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            stmt.close();
            this.close(conn);
        } catch (SQLException e) {
            System.err.println(e);
        } 
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
        Optional<User> user = null;
        String sql = "SELECT username FROM User u where u.username = " + username + " limit 1";
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String hashed = toHexString(md.digest(secret.getBytes(StandardCharsets.UTF_8)));
            System.out.println("hashed:" + hashed);
            while(rs.next()) {
                user = Optional.ofNullable(
                        new User(rs.getString("username"),
                                 rs.getString(hashed),
                                 rs.getString("token")));
            }
            rs.close();
            stmt.close();
            this.close(conn);
        } catch (SQLException | NoSuchAlgorithmException e) {
            System.err.println(e);
        }
        return user;
    }

    @Override
    public Optional<SessionToken> selectToken(User u) throws RemoteUserNotFoundException {
        String username = u.getUsername();
        String sql = "SELECT username FROM User u where u.username = " + username + " limit 1";
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            while(rs.next()) {
                return Optional.ofNullable(new SessionToken(rs.getString("token")));
            }
            rs.close();
            stmt.close();
            this.close(conn);
        } catch (SQLException e) {
            throw new RemoteUserNotFoundException("User not found");
        }
        return null;
    }

    private Connection connect() throws SQLException{
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String connection_string = "jdbc:sqlite:/home/bitor/projects/redwars/main.db";
        System.out.println("Connected to Database");
        return DriverManager.getConnection(connection_string);
    }

    private void close(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 64)
        {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }

}
