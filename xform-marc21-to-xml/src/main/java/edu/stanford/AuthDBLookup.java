package edu.stanford;

import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

/**
 * Uses the Marc4J library to transform the MARC record to MarcXML.
 * The MARC record must have the authority key delimiter output in the system record dump.
 * When encountering an authority key subfield delimiter ('?' or '=') it will add a subfield 0 to the MarcXML record
 * for each 92X field in order to leverage the functionality of the LOC marc2bibframe converter's ability to create
 * bf:hasAuthority elements for URI's present in that subfield (BF1.0).
 */
class AuthDBLookup {

    Connection authDB = null;

    Record record;

    public Record getRecord() {
        return record;
    }

    public AuthDBLookup(Record record) throws IOException, SQLException {
        this.record = record;
        setAuthConnection();
    }

    public void marcResolveAuthorities() {
        List subFieldList;
        DataField dataField;
        MarcFactory factory = MarcFactory.newInstance();

        List fields = record.getDataFields();
        Iterator dataFieldIterator = fields.iterator();

        while (dataFieldIterator.hasNext()) {
            dataField = (DataField) dataFieldIterator.next();

            subFieldList = dataField.getSubfields();
            Object [] subFields = subFieldList.toArray(new Object[subFieldList.size()]);

            for (int s = 0; s < subFields.length; s++) {
                Subfield sf = (Subfield) subFields[s];
                char code = sf.getCode();
                String codeStr = String.valueOf(code);
                String data = sf.getData();

                if (codeStr.equals("=")) {
                    addAuthURIandRemoveSubfields(data, dataField, sf, factory);
                }
                if (codeStr.equals("?")) {
                    dataField.removeSubfield(sf);
                }
            }
        }
    }

    private void addAuthURIandRemoveSubfields(String data, DataField dataField,
                                                     Subfield sf, MarcFactory factory) {

        String key = data.substring(2);
        String authID = lookupAuthID(key);

        //TODO consider just getting all the URI's from the authority record here
        String[] tags = {"920", "921", "922"};
        for (String tag : tags) {
            String uri = lookupAuthURI(authID, tag);
            if (uri.length() > 0)
                dataField.addSubfield(factory.newSubfield('0', uri));
        }
        dataField.removeSubfield(sf);
    }

    private String lookupAuthID(String key) {
        String sql = "select authority_id from authority where authority_key = '" + key + "'";
        return queryAuth(sql);
    }

    private String lookupAuthURI(String authID, String tagNum) {
        String sql = "SELECT AUTHORVED.tag FROM AUTHORVED LEFT JOIN AUTHORITY ON AUTHORVED.offset = AUTHORITY.ved_offset" +
                " where AUTHORITY.authority_id='" + authID + "' and AUTHORVED.tag_number='" + tagNum + "'";
        return queryAuth(sql);
    }

    private String queryAuth(String sql) {
        String result = "";
        try {
            Statement s = authDB.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                result = rs.getString(1).trim();
            }
            rs.close();
            s.close();
        } catch(SQLException e) {
            System.err.println("AuthDBLookup SQLException:" + e.getMessage());
        }
        return result;
    }

    private void setAuthConnection() throws IOException, SQLException {
        if ( authDB == null )
            authDB = AuthDBConnection.open();
    }
}
