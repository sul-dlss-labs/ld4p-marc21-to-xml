package edu.stanford;

import org.apache.commons.io.FileUtils;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.createTempFile;

/**
 *
 */
public class MarcUtils {

    // For additional test data, consider the marc4j data at
    // https://github.com/marc4j/marc4j/tree/master/test/resources
    String marcFileResource = "/one_record.mrc";
    String marcFilePath = getFileResource(marcFileResource);

    Path outputPath;
    Path outputFile;

    Path createOutputFile(String prefix, String suffix) throws IOException {
        createOutputPath();
        return createTempFile(outputPath, prefix, suffix);
    }

    void createOutputPath() throws IOException {
        if (outputPath == null)
            outputPath = createTempDirectory("MarcUtils_");
    }

    void deleteOutputPath() throws IOException {
        if (outputPath != null)
            FileUtils.deleteDirectory(outputPath.toFile());
    }


    String getFileResource(String fileResource) {
        return getClass().getResource(fileResource).getFile();
    }

    MarcStreamReader getMarcReader() throws FileNotFoundException {
        return getMarcReader(marcFilePath);
    }

    MarcStreamReader getMarcReader(String filePath) throws FileNotFoundException {
        return new MarcStreamReader(new FileInputStream(filePath));
    }

    MarcWriter getMarcWriter() throws IOException {
        // Output MARC-XML where these tests can access it easily.
        outputFile = createOutputFile("marc_record", ".xml");
        OutputStream outFileStream = new FileOutputStream(outputFile.toString());
        return new MarcXmlWriter(outFileStream, true);
    }

    Record getMarcRecord() throws Exception {
        return getMarcRecord(marcFilePath);
    }

    Record getMarcRecord(String filePath) throws Exception {
        return getMarcReader(filePath).next();
    }

}