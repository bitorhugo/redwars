package edu.ufp.inf.sd.rmi.red.model.db;

import java.util.Optional;

import edu.ufp.inf.sd.rmi.red.model.sessiontoken.SessionToken;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserNotFoundException;
import edu.ufp.inf.sd.rmi.red.model.user.User;

public interface DBI {
    public void insert(String username, String secret);

    public Optional<User> select(String username, String secret);

    public Optional<SessionToken> selectToken(User u) throws RemoteUserNotFoundException;

    public Optional<User> update(User u);

    public boolean delete(User u);
}
