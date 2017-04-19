package edu.stanford;

import oracle.jdbc.pool.OracleDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Stanford University Libraries, DLSS
 */
class AuthDBConnection {

    static Properties cacheProps;

    AuthDBProperties authDBProperties;

    void setAuthDBProperties(AuthDBProperties props) {
        authDBProperties = props;
    }

    Connection open() throws SQLException, IOException {
        return dataSource().getConnection();
    }

    DataSource dataSource() throws SQLException, IOException {
        OracleDataSource ds = new OracleDataSource();
        ds.setURL(authDBProperties.getURL());
        ds.setUser(authDBProperties.getUserName());
        ds.setPassword(authDBProperties.getUserPass());
        ds.setConnectionCachingEnabled(false);
        ds.setConnectionCacheName("CACHE");
        ds.setConnectionCacheProperties(dataSourceCache());
        return ds;
    }

    Properties dataSourceCache() {
        if(cacheProps == null) {
            cacheProps = new Properties();
            cacheProps.setProperty("MinLimit", "1");
            cacheProps.setProperty("InitialLimit", "1");
            cacheProps.setProperty("AbandonedConnectionTimeout", "100");
            cacheProps.setProperty("PropertyCheckInterval", "80");
        }
        return cacheProps;
    }

}
