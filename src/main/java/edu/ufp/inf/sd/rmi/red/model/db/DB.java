package edu.ufp.inf.sd.rmi.red.model.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import edu.ufp.inf.sd.rmi.red.model.sessiontoken.SessionToken;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserNotFoundException;
import edu.ufp.inf.sd.rmi.red.model.user.User;

public class DB implements DBI {

    private String name;

    public DB(String name) {
        this.name = name;
    }
    
    @Override
    public boolean insert(User u) {
        
        return false;
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
    public Optional<User> selectUser(User u) {
        
        return null;
    }

    @Override
    public Optional<SessionToken> selectToken(User u) throws RemoteUserNotFoundException {
        String username = u.getUname();
        String sql = "SELECT name FROM User u where u.name = " + username + " limit 1";
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
        String connection_string = "jdbc:sqlite:test.db";
        return DriverManager.getConnection(connection_string);
    }

    private void close(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
