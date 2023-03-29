package edu.ufp.inf.sd.rmi.red.model.db;

import java.util.List;
import java.util.Optional;

public interface GameDBI {
    public Optional<List<Integer>> select();
    public Optional<Integer> select(int id);
    public Optional<Integer> insert (String mapname);
    public void update (String mapname, int players);
    public void delete(int id);
}
