#!/bin/bash

SCRIPT_PATH=$( cd $(dirname $0) && pwd -P )
export LD4P_ROOT=$( cd "${SCRIPT_PATH}/.." && pwd -P )
export LD4P_CONFIG="${LD4P_ROOT}/config/config.sh"
source ${LD4P_CONFIG}

if [ ! -d "${LD4P_MARC}" ]; then
    echo "Failed to configure LD4P_MARC data directory: ${LD4P_MARC}"
    exit 1
fi

if [ ! -d "${LD4P_MARCXML}" ]; then
    echo "Failed to configure LD4P_MARCXML data directory: ${LD4P_MARCXML}"
    exit 1
fi

if [ ! -d "${LD4P_LOGS}" ]; then
    echo "Failed to configure LD4P_LOGS directory: ${LD4P_LOGS}"
    exit 1
fi

echo "Searching MARC files: ${LD4P_MARC}/*.mrc"
for marc_bin in $(find ${LD4P_MARC} -type f -name '*.mrc')
do
    ${CONVERT_SCRIPT} ${marc_bin}
    SUCCESS=$?
    if [ ${SUCCESS} ]; then
        if [ "${LD4P_ARCHIVE_ENABLED}" == "true" ]; then
            # Archive the marc_bin file (preserve timestamps etc.)
            rsync -a --update "${marc_bin}" "${LD4P_MARC_ARCHIVE}/" && rm ${marc_bin}
        fi
    fi
done
echo "Completed MARC files: ${LD4P_MARC}/*.mrc"

