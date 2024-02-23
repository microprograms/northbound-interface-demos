#!bin/sh

mkdir -p $HOME/.snmp/mibs
cp UECM-INSPECTOR-MIB.TXT $HOME/.snmp/mibs/UECM-INSPECTOR-MIB.TXT
echo "mibs +UECM-INSPECTOR-MIB" >> $HOME/.snmp/snmp.conf
