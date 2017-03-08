#!/bin/bash

# An LD4P_ROOT path must be defined by any scripts calling this configuration.
if [ "$LD4P_ROOT" == "" ]; then
    echo "ERROR: The LD4P configuration requires an LD4P_ROOT path: ${LD4P_ROOT}" 1>&2
    kill -INT $$
fi

export LD4P_CONFIG="${LD4P_ROOT}/config/config.sh"
export LD4P_LOGS="${LD4P_ROOT}/log"
export LD4P_BIN="${LD4P_ROOT}/bin"
export LD4P_LIB="${LD4P_ROOT}/lib"

export LD4P_DATA="${LD4P_ROOT}/data"
export LD4P_MARC="${LD4P_DATA}/Marc"
export LD4P_MARCXML="${LD4P_DATA}/MarcXML"

export LD4P_ARCHIVE_ENABLED=false
export LD4P_MARC_ARCHIVE="${LD4P_DATA}/Marc_Archive"

CONVERT_SCRIPT="${LD4P_BIN}/marc21_to_marcxml.sh"
if [ ! -f "${CONVERT_SCRIPT}" ]; then
    echo "Failed to locate convert script: ${CONVERT_SCRIPT}"
    exit 1
fi
