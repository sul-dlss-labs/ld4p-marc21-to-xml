#!/bin/bash

SCRIPT_PATH=$( cd $(dirname $0) && pwd -P )
export LD4P_ROOT=$( cd "${SCRIPT_PATH}/.." && pwd -P )
export LD4P_CONFIG="${LD4P_ROOT}/config/config.sh"
source ${LD4P_CONFIG}

MARC_BIN="${LD4P_MARC}/one_record.mrc"
if [ ! -f ${MARC_BIN} ]; then
    echo "Failed to locate MARC21 file: ${MARC_BIN}"
    exit 1
fi

${CONVERT_SCRIPT} ${MARC_BIN}

# Check the conversion worked, it should output this file.
MARC_XML="${LD4P_MARCXML}/1629059.xml"
if [ -s ${MARC_XML} ]; then
    echo "SUCCESS created MARC-XML file: ${MARC_XML}"
else
    echo "FAILURE to create MARC-XML file: ${MARC_XML}"
    exit 1
fi

