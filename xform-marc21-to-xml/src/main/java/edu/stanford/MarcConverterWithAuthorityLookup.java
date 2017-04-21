package edu.stanford;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.marc4j.marc.Record;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Uses the Marc4J library to transform the MARC record to MarcXML.
 * The MARC record must have the authority key delimiter output in the system record dump.
 * When encountering an authority key subfield delimiter ('?' or '=') it will add a subfield 0 to the MarcXML record
 * for each 92X field in order to leverage the functionality of the LOC marc2bibframe converter's ability to create
 * bf:hasAuthority elements for URI's present in that subfield (BF1.0).
 */
class MarcConverterWithAuthorityLookup {

    AuthDBProperties authDBProperties;
    AuthDBConnection authDBConnection;
    AuthDBLookup authDBLookup;

    static void addOptions(Options opts) {
        opts.addOption("h", "help", false, "help message");
        opts.addOption("p", "auth-db-property-file", true, "Authority DB connection property file");
    }

    static void printHelp(String className, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(className, options);
    }

    void setAuthDBProperties(CommandLine cmd) {
        // Set the authority-db properties
        try {
            if (cmd.hasOption("p")) {
                // Use a properties file given on the command line
                String dbPropFile = cmd.getOptionValue("p").trim();
                authDBProperties = new AuthDBProperties(dbPropFile);
            } else {
                // Use a properties file packaged in the JAR resources
                authDBProperties = new AuthDBProperties();
            }
        } catch (IOException ex) {
            System.err.println("ERROR: Failure to set Authority-DB properties.");
            System.err.print(ex.getStackTrace());
            System.exit(1);
        }
    }

    Record authLookups(Record record) throws IOException, SQLException {
        authLookupInit();
        return authDBLookup.marcResolveAuthorities(record);
    }

    void authLookupInit() throws IOException, SQLException {
        if (authDBProperties == null)
            authDBProperties = new AuthDBProperties();
        if (authDBConnection == null)
            authDBConnection = authDBConnection();
        if (authDBLookup == null)
            authDBLookup = authDBLookup();
    }

    AuthDBLookup authDBLookup() throws IOException, SQLException {
        AuthDBLookup lookup = new AuthDBLookup();
        lookup.setAuthDBConnection(authDBConnection);
        lookup.openConnection();
        return lookup;
    }

    AuthDBConnection authDBConnection() throws IOException, SQLException {
        AuthDBConnection conn = new AuthDBConnection();
        conn.setAuthDBProperties(authDBProperties);
        return conn;
    }

    void authLookupClose() throws SQLException {
        if (authDBLookup != null) {
            authDBLookup.closeConnection();
            authDBLookup = null;
        }
    }
}

