package edu.stanford;

import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 *
 */
public class SqliteUtils {

    static Connection sqliteConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        } catch(SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        }
        return connection;
    }

    static DataSource sqliteDataSource() {
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setDatabaseName(":memory:");
        return ds;
    }

    static AuthDBConnection sqliteAuthDBConnection() throws IOException, SQLException {
        AuthDBConnection spyAuthDBConnection = spy(AuthDBConnection.class);
        doReturn(SqliteUtils.sqliteDataSource()).when(spyAuthDBConnection).dataSource();
        return spyAuthDBConnection;
    }

    static AuthDBLookup sqliteAuthDBLookup() throws IOException, SQLException {
        AuthDBLookup authLookup = new AuthDBLookup();
        authLookup.setAuthDBConnection(sqliteAuthDBConnection());
        return authLookup;
    }

}