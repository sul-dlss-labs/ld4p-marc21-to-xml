package edu.stanford;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;

import java.io.*;
import java.sql.SQLException;

/**
 * Uses the Marc4J library to transform the MARC record to MarcXML.
 * The MARC record must have the authority key delimiter output in the system record dump.
 * When encountering an authority key subfield delimiter ('?' or '=') it will add a subfield 0 to the MarcXML record
 * for each 92X field in order to leverage the functionality of the LOC marc2bibframe converter's ability to create
 * bf:hasAuthority elements for URI's present in that subfield (BF1.0).
 */
class MarcToXMLStream {

    private static MarcReader marcReader = new MarcStreamReader(System.in);
    private static MarcWriter marcWriter = new MarcXmlWriter(System.out, true);

    public static void setMarcReader(MarcReader marcReader) {
        MarcToXMLStream.marcReader = marcReader;
    }

    public static void setMarcWriter(MarcWriter marcWriter) {
        MarcToXMLStream.marcWriter = marcWriter;
    }

    public static void main (String [] args) throws IOException, SQLException {
        convertRecords();
    }

    static void convertRecords() throws IOException, SQLException {
        while (marcReader.hasNext()) {
            Record record = marcReader.next();
            marcWriter.write(authorityLookup(record));
        }
        marcWriter.close();
    }

    static Record authorityLookup(Record record) throws IOException, SQLException {
        AuthDBLookup authLookup = new AuthDBLookup(record);
        authLookup.marcResolveAuthorities();
        authLookup.closeConnection();
        return authLookup.getRecord();
    }

}
