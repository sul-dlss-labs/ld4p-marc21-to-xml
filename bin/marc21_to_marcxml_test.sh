#!/bin/bash

SCRIPT_PATH=$( cd $(dirname $0) && pwd -P )
export LD4P_ROOT=$( cd "${SCRIPT_PATH}/.." && pwd -P )

# Do not use the config/config.sh file because it gets linked
# to the shared configs file on deployment.  This test script
# must define some file IO paths specific for this test script so it
# cannot collide with the real data paths on a deployed system.

# ---
# Utility paths and scripts

export LD4P_LOGS="${LD4P_ROOT}/log"
export LD4P_BIN="${LD4P_ROOT}/bin"
export LD4P_LIB="${LD4P_ROOT}/lib"

CONVERT_SCRIPT="${LD4P_BIN}/marc21_to_marcxml.sh"
if [ ! -f "${CONVERT_SCRIPT}" ]; then
    echo "Failed to locate convert script: ${CONVERT_SCRIPT}"
    exit 1
fi

# ---
# Data paths

export LD4P_DATA="${LD4P_ROOT}/data"
export LD4P_MARC="${LD4P_DATA}/Marc"
export LD4P_MARCXML="${LD4P_DATA}/MarcXML"

MARC_BIN="${LD4P_MARC}/one_record.mrc"
if [ ! -f ${MARC_BIN} ]; then
    echo "Failed to locate MARC21 file: ${MARC_BIN}"
    exit 1
fi

# ---
# Run the test conversion and check the result

${CONVERT_SCRIPT} ${MARC_BIN}

# The conversion should output this file:
MARC_XML="${LD4P_MARCXML}/1629059.xml"
if [ -s ${MARC_XML} ]; then
    echo "SUCCESS created MARC-XML file: ${MARC_XML}"
    # remove this output file, so this test is idempotent
    rm -f ${MARC_XML}
    echo "CLEANUP removed test MARC-XML file: ${MARC_XML}"
else
    echo "FAILURE to create MARC-XML file: ${MARC_XML}"
    exit 1
fi

