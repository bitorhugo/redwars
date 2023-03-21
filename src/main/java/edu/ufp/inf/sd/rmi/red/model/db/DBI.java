package edu.ufp.inf.sd.rmi.red.model.db;

import java.util.Optional;

import edu.ufp.inf.sd.rmi.red.model.sessiontoken.SessionToken;
import edu.ufp.inf.sd.rmi.red.model.user.RemoteUserNotFoundException;
import edu.ufp.inf.sd.rmi.red.model.user.User;

public interface DBI {
    public boolean insert(User u);

    public Optional<User> selectUser(User u);

    public Optional<SessionToken> selectToken(User u) throws RemoteUserNotFoundException;

    public Optional<User> update(User u);

    public boolean delete(User u);
}
