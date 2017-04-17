package edu.stanford;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.marc4j.*;
import org.marc4j.marc.Record;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * Uses the Marc4J library to transform the MARC record to MarcXML.
 * The MARC record must have the authority key delimiter output in the system record dump.
 * When encountering an authority key subfield delimiter ('?' or '=') it will add a subfield 0 to the MarcXML record
 * for each 92X field in order to leverage the functionality of the LOC marc2bibframe converter's ability to create
 * bf:hasAuthority elements for URI's present in that subfield (BF1.0).
 */
class MarcToXML extends MarcConverterWithAuthorityLookup {

    // Apache Commons-CLI Options
    // https://commons.apache.org/proper/commons-cli/introduction.html
    static CommandLine cmd = null;
    static Options options = setOptions();

    static String className = MarcToXML.class.getName();

    static Options setOptions() {
        Options opts = new Options();
        opts.addOption("i", "inputFile", true, "MARC input file (binary .mrc file expected; required)");
        opts.addOption("o", "outputPath", true, "MARC XML output path (default: ENV[\"LD4P_MARCXML\"])");
        opts.addOption("l", "logFile", true, "Log file output (default: " + logFileDefault + ")");
        opts.addOption("r", "replace", false, "Replace existing XML files (default: false)");
        MarcConverterWithAuthorityLookup.addOptions(opts);
        return opts;
    }

    static String marcInputFile = null;

    static void setMarcInputFile(String file) {
        MarcToXML.marcInputFile = file;
    }

    static void parseInputFile() {
        // Parse and set the input file
        String iFile = cmd.getOptionValue("i");
        if (iFile == null) {
            System.err.println("ERROR: No MARC input file specified.");
            printHelp(className, options);
            System.exit(1);
        }
        // Check the input file exists
        File marcFile = new File(iFile.trim());
        if (! marcFile.isFile()) {
            System.err.println("ERROR: MARC input file is not a file.");
            printHelp(className, options);
            System.exit(1);
        }
        setMarcInputFile( marcFile.toString() );
    }


    static MarcReader marcReader = null;

    static void setMarcReader() throws FileNotFoundException {
        FileInputStream marcInputFileStream = new FileInputStream(marcInputFile);
        marcReader = new MarcStreamReader(marcInputFileStream);
    }

    static Boolean xmlReplace = false;

    static void setXmlReplace(Boolean replace) {
        MarcToXML.xmlReplace = replace;
    }

    static String xmlOutputPath = null;

    static void setXmlOutputPath(String path) {
        MarcToXML.xmlOutputPath = path;
    }

    static void parseOutputPath() {
        // Parse and set the output path
        String oPath = cmd.getOptionValue("o");
        if (oPath == null)
            oPath = System.getenv("LD4P_MARCXML");
        if (oPath == null) {
            System.err.println("ERROR: No MARC-XML output path specified.");
            printHelp(className, options);
            System.exit(1);
        }
        // Check the output path exists
        File path = new File(oPath.trim());
        if (! path.isDirectory()) {
            System.err.println("ERROR: MARC-XML output path is not a directory.");
            printHelp(className, options);
            System.exit(1);
        }
        setXmlOutputPath( path.toString() );
    }

    private static Logger log = null;

    private static String logFileDefault = "log/MarcToXML.log";

    static void setLogger(String logFile) {
        // See src/main/resources/log4j2.xml for configuration details.
        // This method uses a programmatic approach to add a file logger.
        addLogFileAppender(logFile);
        log = LogManager.getLogger();
    }

    static void parseLogFile() {
        // Parse and set the log file
        String logFile = cmd.getOptionValue("l");
        if (logFile == null)
            logFile = logFileDefault;
        setLogger( logFile.trim() );
    }

    private static void addLogFileAppender(String filename) {
        if (filename == null)
            return;
        String loggerName = MarcToXML.class.getName();
        String fileAppenderName = "LOGFile";
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        PatternLayout layout = PatternLayout.newBuilder()
                .withConfiguration(config)
                .withPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n")
                .build();
        FileAppender appender = FileAppender.newBuilder()
                .withFileName(filename)
                .withName(fileAppenderName)
                .withLayout(layout)
                .build();
        appender.start();
        config.addAppender(appender);

        AppenderRef ref = AppenderRef.createAppenderRef(fileAppenderName, null, null);
        AppenderRef[] appenderRefs = new AppenderRef[] {ref};

        Boolean loggerAdd = false;
        Filter loggerFilter = null;
        Level loggerLevel = Level.INFO;
        Property[] loggerProperties = null;
        LoggerConfig loggerConfig = LoggerConfig.createLogger(
                loggerAdd,
                loggerLevel,
                loggerName,
                "true",
                appenderRefs,
                loggerProperties,
                config,
                loggerFilter);

        loggerConfig.addAppender(appender, loggerLevel, loggerFilter);
        config.removeLogger(loggerName);
        config.addLogger(loggerName, loggerConfig);
        context.updateLoggers();
    }

    static void parseArgs(String [] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(options, args);
        if (cmd.hasOption('h')) {
            // Print the help message and exit
            printHelp(className, options);
            System.exit(0);
        }
        // Parse required options
        parseInputFile();
        // Parse optional options
        setAuthDBProperties(cmd);
        parseOutputPath();
        parseLogFile();
        setXmlReplace( cmd.hasOption("r") );
    }

    public static void main (String [] args) throws FileNotFoundException, ParseException, SQLException {
        parseArgs(args);
        convertMarcRecords();
    }

    static void convertMarcRecords() throws FileNotFoundException, SQLException {
        setMarcReader();
        while (marcReader.hasNext()) {
            convertMarcRecord(marcReader.next());
        }
        authLookupClose();
    }

    static void convertMarcRecord(Record record) {
        try {
            String xmlFilePath = xmlOutputFilePath(record);
            File xmlFile = new File(xmlFilePath);
            if (doConversion(xmlFile, xmlReplace)) {
                MarcWriter writer = marcRecordWriter(xmlFilePath);
                writer.write(authLookups(record));
                writer.close();
                log.info("Output MARC-XML file: " + xmlFilePath);
            } else {
                log.info("Skipped MARC-XML file: " + xmlFilePath);
            }
        }
        catch (IOException | SQLException | NullPointerException | MarcException e) {
            reportErrors(e);
        }
    }

    // TODO: move this method to a subclass of Record
    static String xmlOutputFilePath(Record record) {
        String cn = record.getControlNumber();
        String outFileName = cn.replaceAll(" ", "_").toLowerCase() + ".xml";
        Path outFilePath = Paths.get(xmlOutputPath, outFileName);
        return outFilePath.toString();
    }

    static Boolean doConversion(File xmlFile, Boolean xmlReplace) {
        if (!xmlFile.exists() || xmlReplace) {
             return true;
         }
         else {
             return false;
         }
    }

    private static MarcWriter marcRecordWriter(String filePath) throws FileNotFoundException {
        OutputStream outFileStream = new FileOutputStream(filePath);;
        return new MarcXmlWriter(outFileStream, true);
    }

    private static void reportErrors(Exception e) {
        String msg = e.getMessage();
        log.fatal(msg);
        System.err.println(msg);
        //e.printStackTrace();
    }
}
