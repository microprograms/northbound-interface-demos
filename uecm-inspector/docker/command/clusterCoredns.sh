#!bin/sh

##
# 解析参数
##
output="/output"
jobNamePrefix="cluster-coredns"
jobImage="rancher/library-busybox:1.31.1"

for key in "$@"; do
  case $key in
    --output=*)
      output="${key#*=}"
      shift # past argument=value
      ;;
    --jobNamePrefix=*)
      jobNamePrefix="${key#*=}"
      shift # past argument=value
      ;;
    --jobImage=*)
      jobImage="${key#*=}"
      shift # past argument=value
      ;;
    *)
      ;;
  esac
done

echo "CoreDNS running" >> $output
