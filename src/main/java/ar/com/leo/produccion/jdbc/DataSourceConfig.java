package ar.com.leo.produccion.jdbc;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Leo
 */
public class DataSourceConfig {

    public static SQLServerDataSource dataSource;

    static {
        try (InputStream in = DataSourceConfig.class.getResourceAsStream("/db.properties")) {
            Properties props = new Properties();
            props.load(in);
            dataSource = new SQLServerDataSource();
            dataSource.setURL(props.getProperty("db.connection.url"));
            dataSource.setUser(props.getProperty("db.connection.username"));
            dataSource.setPassword(props.getProperty("db.connection.password"));
            dataSource.setDatabaseName(props.getProperty("db.connection.databaseName"));
            dataSource.setEncrypt("false");
//            dataSource.setIntegratedSecurity(false);
            dataSource.setTrustServerCertificate(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
