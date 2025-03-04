package ed.inf.adbs.blazedb;

import java.io.IOException;
import java.util.List;

public interface RowSource {
    public boolean hasNext();
    public List<String> next();
    public void close() throws IOException;
}
