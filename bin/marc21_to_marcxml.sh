#!/bin/bash
#
# Requires one input parameter - the path to a MARC21 binary file.
#
# Process all records in the mrc_file using marc4j and SQL to
# look up authority keys and retrieve any URI values from
# 92X fields and put them in the subfield 0 so that the
# LOC converter can use them correctly.

mrc_file=$1

mrc_name=$(basename ${mrc_file} .mrc)
log_date=$(date +%Y%m%dT%H%M%S)
log_name="${LD4P_LOGS}/${mrc_name}_marc21-to-xml_${log_date}"
log_file="${log_name}.log"
err_file="${log_name}_errors.log"

echo
echo "Converting MARC file:  ${mrc_file}"
echo "Output MARC-XML files: ${LD4P_MARCXML}/*.xml"
echo "Logging conversion to: ${log_file}"

# Java library, built from ./java sources and copied to ./lib
jar="${LD4P_LIB}/xform-marc21-to-xml-jar-with-dependencies.jar"
if [ ! -f "$jar" ]; then
    echo "ERROR: cannot find JAR: $jar"
    exit 1
fi

# $ java -cp ${jar} edu.stanford.MarcToXML -h
#  usage: edu.stanford.MarcToXML
#   -h,--help               help message
#   -i,--inputFile <arg>    MARC input file (binary .mrc file expected; required)
#   -l,--logFile <arg>      Log file output (default: log/MarcToXML.log)
#   -o,--outputPath <arg>   MARC XML output path (default: ENV["LD4P_MARCXML"])
#   -r,--replace            Replace existing XML files (default: false)

java -cp ${jar} edu.stanford.MarcToXML -i ${mrc_file} -o ${LD4P_MARCXML} -l ${log_file} -r

success=$?
if [ ${success} ]; then
    echo "Completed conversion."
else
    echo "ERROR: Conversion failed for ${mrc_file}" | tee --append ${err_file}
fi

echo
exit ${success}
