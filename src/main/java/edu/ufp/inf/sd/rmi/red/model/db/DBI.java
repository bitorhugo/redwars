package edu.ufp.inf.sd.rmi.red.model.db;

import java.io.Serializable;
import java.util.Optional;

import edu.ufp.inf.sd.rmi.red.model.user.User;

public interface DBI extends Serializable {
    public Optional<User> insert(String username, String secret);

    public Optional<User> select(String username, String secret);

    public Optional<User> select(String username);

    public Optional<User> update(User u);

    public boolean delete(User u);
}
