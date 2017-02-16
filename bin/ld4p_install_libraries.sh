#!/bin/bash

SCRIPT_PATH=$(dirname $0)

# The lib/ld4p_converter.jar is copied, unless
# the JAR is already installed in $LD4P_BIN
${SCRIPT_PATH}/ld4p_install_tracer_bullets.sh

# The shared_configs contain private data files
${SCRIPT_PATH}/ld4p_install_shared_configs.sh
