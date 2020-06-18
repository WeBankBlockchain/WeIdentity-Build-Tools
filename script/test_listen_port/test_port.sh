#!/bin/bash
for line in `cat listen_port.ini`
do
echo begin test $line
ip=`echo $line | cut -d \: -f 1`
port=`echo $line | cut -d \: -f 2`
port=$(echo $port | sed -e 's/\r//g')
result=`echo -e "\n" | timeout 5 telnet $ip $port 2>/dev/null | grep Connected | wc -l`
 
if [ $result -eq 1 ]; then
      echo "Network is Open."
else
      echo "Network is Closed."
fi
done