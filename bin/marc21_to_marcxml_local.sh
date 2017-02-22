#!/bin/bash
#
# Requires one input parameter - the path to a MARC21 binary file.
#
# note that log_dir and OUTPUT_DIR are expected to exist already.
#
# Process all records in the mrc_file using marc4j and SQL to
# look up authority keys and retrieve any URI values from
# 92X fields and put them in the subfield 0 so that the
# LOC converter (for Bibframe v1) can use them correctly.

#INPUT_DATA_DIR = '/Symphony/Marc'
#INPUT_DATA_DIR = '~/data/casalini-raw'
# FIXME: have this be a java property? (in a properties file or java command line -D argument)
#   or combine input filename and input file directory into the same arg?
INPUT_DATA_DIR='java/src/test/resources'

# FIXME: this is hardcoded - it should be in a properties file or java command line -D arg)
#OUTPUT_DIR = '~/data/marcxml_output'
OUTPUT_DIR='../../../data/test'

# vars above this line would to be changed to process other data
#------------------------------------------------

log_dir='log'

jar_dir='java/target'
jar="${jar_dir}/xform-marc21-to-xml-jar-with-dependencies.jar"

mrc_file="${INPUT_DATA_DIR}/$1"

# this var is used in java code
# FIXME: have this be a java property? (in a properties file or java command line -D argument)
export LD4P_MARCXML=${OUTPUT_DIR}

filename=$(basename ${mrc_file} .mrc)
log_date=$(date +%Y%m%dT%H%M%S)
log_name="${log_dir}/${filename}_marc21-to-xml_${log_date}"
log_file="${log_name}.log"
err_file="${log_name}_errors.log"

echo
echo "Converting MARC file:  ${mrc_file}"
echo "Output MARC-XML files: ${LD4P_MARCXML}/*.xml"
echo "Logging conversion to: ${log_file}"

options="-i ${mrc_file} -o ${LD4P_MARCXML} -l ${log_file} -r"

java -cp ${jar} edu.stanford.MarcToXML ${options}

success=$?
if [ ${success} ]; then
    echo "Completed conversion."
else
    echo "ERROR: Conversion failed for ${mrc_file}" | tee --append ${err_file}
fi

echo
exit ${success}
