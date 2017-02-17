#!/bin/bash
#
# Requires one input parameter - the path to a MARC21 binary file.
#
# Process all records in the MRC_FILE using marc4j and SQL to
# look up authority keys and retrieve any URI values from
# 92X fields and put them in the subfield 0 so that the
# LOC converter (for Bibframe v1) can use them correctly.

#INPUT_DATA_DIR = '/Symphony/Marc'
#INPUT_DATA_DIR = '~/data/casalini-raw'
# FIXME: have this be a java property? (in a properties file or java command line -D argument)
#   or combine input filename and input file directory into the same arg?
INPUT_DATA_DIR='java/src/test/resources'

#OUTPUT_DIR = '~/data/marcxml_output'
OUTPUT_DIR='~/data/local_test'
mkdir -p ${OUTPUT_DIR} || kill -INT $$

# vars above this line would to be changed to process other data
#------------------------------------------------

LOG_DIR='log'
mkdir -p ${LOG_DIR} || kill -INT $$

JAR_DIR='java/target'
JAR="${JAR_DIR}/xform-marc21-to-xml-jar-with-dependencies.jar"

MRC_FILE="${INPUT_DATA_DIR}/$1"

# this var is used in java code
# FIXME: have this be a java property? (in a properties file or java command line -D argument)
export LD4P_MARCXML=${OUTPUT_DIR}

filename=$(basename ${MRC_FILE} .mrc)
LOG_DATE=$(date +%Y%m%dT%H%M%S)
LOG_NAME="${LOG_DIR}/${filename}_marc21-to-xml_${LOG_DATE}"
LOG_FILE="${LOG_NAME}.log"
ERR_FILE="${LOG_NAME}_errors.log"

echo
echo "Converting MARC file:  ${MRC_FILE}"
echo "Output MARC-XML files: ${LD4P_MARCXML}/*.xml"
echo "Logging conversion to: ${LOG_FILE}"

OPTIONS="-i ${MRC_FILE} -o ${LD4P_MARCXML} -l ${LOG_FILE} -r"

java -cp ${JAR} edu.stanford.MarcToXML ${OPTIONS}

SUCCESS=$?
if [ ${SUCCESS} ]; then
    echo "Completed conversion."
else
    echo "ERROR: Conversion failed for ${MRC_FILE}" | tee --append ${ERR_FILE}
fi

exit ${SUCCESS}
