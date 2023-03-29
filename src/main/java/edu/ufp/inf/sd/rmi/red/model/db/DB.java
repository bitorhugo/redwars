package edu.ufp.inf.sd.rmi.red.model.db;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.ufp.inf.sd.rmi.red.model.token.Token;
import edu.ufp.inf.sd.rmi.red.model.user.User;

public class DB implements DBI, GameDBI {

    private String name;

    public DB(String name) {
        this.name = name;
    }
    
    @Override
    public Optional<User> insert(String username, String secret) {
        User u = null;
        Token t = new Token();
        String hash = this.hash(secret).orElseThrow(); // hash secret
        String sql = "INSERT INTO User (username, secret, token) " +
            "VALUES('" + username + "', '" + hash + "', '" + t.getValue() + "');"; 
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            stmt.close();
            this.close(conn);
            u = new User(username, hash, t);
        } catch (SQLException e) {
            System.err.println(e);
        }
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
                String t = rs.getString("token");
                user = new User(u, s, new Token(t));
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

    @Override
    public Optional<User> select(String username) {
        User user = null;
        String sql = "SELECT * FROM User u where u.username = '" + username + "'" + " limit 1";
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                String u = rs.getString("username");
                String s = rs.getString("secret");
                String t = rs.getString("token");
                user = new User(u, s, new Token(t));
            }
            rs.close();
            stmt.close();
            this.close(conn);
        } catch (SQLException e) {
            System.err.println(e);
        }
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<List<Integer>> select() {
        List<Integer> gameIDs = null;
        String sql = "SELECT id FROM Game";
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            
            gameIDs = new ArrayList<>();
            // loop through the result set
            while (rs.next()) {
                gameIDs.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.ofNullable(gameIDs);
    }

    @Override
    public Optional<Integer> select(int id) {
        Integer gameID = null;
        String sql = "SELECT id FROM Game where id = ? limit 1";
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            while (rs.next()) {
                gameID = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.ofNullable(gameID);
    }

    @Override
    public Optional<Integer> insert(String mapname) {
        Integer id = null;
        String ins = "INSERT INTO Game(map, players) VALUES(?,?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, mapname);
            pstmt.setInt(2, 1);
            pstmt.executeUpdate();
            id = pstmt.getGeneratedKeys().getInt(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.ofNullable(id);
    }

    @Override
    public void update(String mapname, int players) {
        // TODO Auto-generated method stub
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Game WHERE id = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Game " + id + " removed");
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
