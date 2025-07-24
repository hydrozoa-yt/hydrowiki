package dk.hydrozoa.hydrowiki;

import javax.sql.DataSource;
import java.util.Properties;

public interface ServerContext {

    public Properties getProperties();

    public DataSource getDBConnectionPool();

    public S3Interactor getS3Interactor();

}
