#! /bin/bash

set -e # Do not allow any command to return non-zero value
set -u # Don't allow referencing that are not previously defined
set -o pipefail # If any part of a pipe fails, the return value of the entire piped command will reflect this regardless if the last command succeeds

printHelp() {
    echo "Load script. This script will write down load stats intermittently in a file until it is killed."
    echo ""
    echo "Usage:"
    echo " $0 <number of seconds between saving load to file> <output file>"
    echo "N.B! The output file must NOT exist when starting the script as it is created!"
    echo ""
    echo "The following example will output stats every 10 seconds into the given file name:"
    echo "$0 10 /tmp/load_stat_output.txt"
}

if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters."
    echo ""
    printHelp
    exit 1
fi

NUM_SECS_TO_SLEEP=$1
OUTPUT_FILE=$2

# Check that the first parameter is a non-negative integer
re='^[0-9]+$'
if ! [[ ${NUM_SECS_TO_SLEEP} =~ $re ]] ; then
    echo "First parameter (${NUM_SECS_TO_SLEEP}) must be an integer!"
    echo ""
    printHelp
    exit 1
fi

# Check that the output file does not exist
if [ -f ${OUTPUT_FILE} ]; then
    echo "Output file ${OUTPUT_FILE} already exists!"
    echo ""
    printHelp
    exit 1
fi

# Create output file and verify that it was created
touch ${OUTPUT_FILE}
if [ ! -f ${OUTPUT_FILE} ]; then
    echo "Failed to create output file ${OUTPUT_FILE}! Does the directory exist?"
    exit 1
fi

PROC_LOADAVG_COMMAND="cat /proc/loadavg"
FREE_COMMAND="free -twh"

# Write general information to the output file
echo "${PROC_LOADAVG_COMMAND} information:" >> ${OUTPUT_FILE}
echo "The first three fields in this file are load average figures giving the number of jobs in the run queue (state R) or waiting for disk I/O (state D) averaged over 1, 5, and 15 minutes. They are the same as the load average numbers given by uptime(1) and other programs." >> ${OUTPUT_FILE}
echo "The fourth field consists of two numbers separated by a slash (/). The first of these is the number of currently executing kernel scheduling entities (processes, threads); this will be less than or equal to the number of CPUs. The value after the slash is the number of kernel scheduling entities that currently exist on the system." >> ${OUTPUT_FILE}
echo "The fifth field is the PID of the process that was most recently created on the system." >> ${OUTPUT_FILE}
echo "" >> ${OUTPUT_FILE}

echo "ps command information:" >> ${OUTPUT_FILE}
echo "ps command outputs the 5 most CPU intensive processes, including memory usage etc. The list is sorted on CPU usage." >> ${OUTPUT_FILE}
echo "" >> ${OUTPUT_FILE}

echo "${FREE_COMMAND} information:" >> ${OUTPUT_FILE}
echo "The free command outputs memory information (options: t=total, w=wide format, h=human readable)." >> ${OUTPUT_FILE}
echo "----------------------------------" >> ${OUTPUT_FILE}

# Write to the output file periodically based on the sleep time
while true
do
    # Store date and time
    echo "" >> ${OUTPUT_FILE}
    date +"%d/%m/%Y %H:%M:%S" >> ${OUTPUT_FILE}

    # proc/loadavg information
    echo "" >> ${OUTPUT_FILE}
    echo "${PROC_LOADAVG_COMMAND}:" >> ${OUTPUT_FILE}
    ${PROC_LOADAVG_COMMAND} >> ${OUTPUT_FILE}

    # ps command
    echo "" >> ${OUTPUT_FILE}
    echo "ps -eo pcpu,pmem,pid,user,args | sort -k 1 -r | head -5:" >> ${OUTPUT_FILE} # N.B! Trying to use pipe in variable caused problem when executing
    echo -e "$(eval ps -eo pcpu,pmem,pid,user,args | sort -k 1 -r | head -5)" >> ${OUTPUT_FILE}

    # free command
    echo "" >> ${OUTPUT_FILE}
    echo "${FREE_COMMAND}:" >> ${OUTPUT_FILE}
    ${FREE_COMMAND} >> ${OUTPUT_FILE}

    echo "" >> ${OUTPUT_FILE}
    echo "----------------------------------" >> ${OUTPUT_FILE}
    echo "" >> ${OUTPUT_FILE}

    sleep ${NUM_SECS_TO_SLEEP}
done
