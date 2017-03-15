package edu.stanford;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Stanford University Libraries, DLSS
 */
class AuthDBProperties {

    private static Logger log = LogManager.getLogger(AuthDBProperties.class.getName());

    private static final String PROPERTY_RESOURCE = "/server.conf";

    private String server = null;
    private String service = null;
    private String userName = null;
    private String userPass = null;

    public AuthDBProperties() throws IOException {
        Properties properties = loadPropertyResource(PROPERTY_RESOURCE);
        initDataSourceProperties(properties);
    }

    public AuthDBProperties(String propertyFile) throws IOException {
        Properties properties = loadPropertyFile(propertyFile);
        initDataSourceProperties(properties);
    }

    public String getURL() {
        return "jdbc:oracle:thin:@" + this.server + ":1521:" + this.service;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPass() {
        return userPass;
    }

    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }

    private void initDataSourceProperties(Properties properties) {
        this.server = properties.getProperty("SERVER");
        this.service = properties.getProperty("SERVICE_NAME");
        this.userName = properties.getProperty("USER");
        this.userPass = properties.getProperty("PASS");
    }

    private Properties loadPropertyFile(String propertyFile) throws IOException {
        try {
            InputStream iStream = new FileInputStream(propertyFile);
            return loadProperties(iStream);
        } catch (IOException e) {
            log.fatal("Failed to load property file:" + propertyFile , e);
            throw e;
        }
    }

    private Properties loadPropertyResource(String resourceName) throws IOException {
        try {
            InputStream iStream = AuthDBProperties.class.getResourceAsStream(resourceName);
            if (iStream == null) {
                // cls.getResourceAsStream() does not throw an IOException when the resource is missing.
                throw( new FileNotFoundException("Failed to find property resource:" + resourceName) );
            }
            return loadProperties(iStream);
        } catch (IOException e) {
            log.fatal("Failed to load property resource:" + resourceName, e);
            throw e;
        }
    }

    private Properties loadProperties(InputStream iStream) throws IOException {
        Properties props = new Properties();
        props.load(iStream);
        iStream.close();
        log.debug( props.toString() );
        return props;
    }
}
