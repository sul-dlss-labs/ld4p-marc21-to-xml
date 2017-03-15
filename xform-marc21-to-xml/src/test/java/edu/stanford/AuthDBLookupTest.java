package edu.stanford;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static edu.stanford.AuthDBLookup.*;
import static java.nio.file.Files.createTempDirectory;
import static org.junit.Assert.*;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "oracle.xdb.XMLType"})
@PrepareForTest({ AuthDBLookup.class, AuthDBConnection.class })
public class AuthDBLookupTest {

    @Test
    public void setAuthConnectionTest() {
/*        try {
            assertTrue(AuthDBLookup.authDB == null);
            AuthDBLookup.setAuthConnection();
        } catch (Throwable expected) {
            assertNotEquals(IOException.class, expected.getClass());
            assertNotEquals(SQLException.class, expected.getClass());
        }*/

/*        try {
            AuthDBLookup.authDB = Mockito.mock(Connection.class);
            AuthDBLookup.setAuthConnection();
            assertFalse(AuthDBLookup.authDB == null);
        } catch (Throwable expected) {
            assertNotEquals(IOException.class, expected.getClass());
            assertNotEquals(SQLException.class, expected.getClass());
        }*/
    }

}