package edu.stanford;

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

    static AuthDBLookup authLookup = null;

    static Record authLookups(Record record) throws IOException, SQLException {
        authLookupInit();
        return authLookup.marcResolveAuthorities(record);
    }

    static void authLookupInit() throws IOException, SQLException {
        if (authLookup == null) {
            authLookup = new AuthDBLookup();
            authLookup.openConnection();
        }
    }

    static void authLookupClose() throws SQLException {
        if (authLookup != null) {
            authLookup.closeConnection();
            authLookup = null;
        }
    }

}
