#!/usr/bin/env bash
jpid_list=$(jps)
echo "----------- Java Process List ----------"
echo "$jpid_list"
pid_with_name=$(jps | grep 'SamzaContainer')
echo "----------- Target Process -------------"
echo "$pid_with_name"
echo "----------------------------------------"
pid=$(echo $pid_with_name | awk '{print $1}')
echo "killing $pid?"
echo "(press ENTER to confirm, or CTRL-C to abort.)" & read dummy
kill -9 $pid