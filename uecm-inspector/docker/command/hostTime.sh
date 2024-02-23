#!bin/sh

##
# 解析参数
##
output="/output"
jobNamePrefix="host-time"
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

##
# 选择要执行job的k3s节点
##
selectedK3sNode=$(/cache/kubectl get node -o name | sed 's/node\///')

##
# 执行job
##
for k3sNode in $selectedK3sNode
do
  cat <<EOF | /cache/kubectl apply -f -
apiVersion: batch/v1
kind: Job
metadata:
  name: $jobNamePrefix-$k3sNode
spec:
  template:
    spec:
      containers:
      - name: $jobNamePrefix-$k3sNode
        image: $jobImage
        imagePullPolicy: IfNotPresent
        command: ["nsenter", "--target", "1", "--mount", "--uts", "--ipc", "--net", "--pid", "--"]
        args:
          - "sh"
          - "-c"
          - "echo \$(hostname) \$(date +%Y-%m-%dT%H:%M:%S)"
        securityContext:
          privileged: true
      nodeSelector:
        kubernetes.io/hostname: $k3sNode
      restartPolicy: Never
      hostIPC: true
      hostNetwork: true
      hostPID: true
  backoffLimit: 0
EOF
done

##
# 等待job执行完毕
##
for k3sNode in $selectedK3sNode
do
  /cache/kubectl wait --for=condition=complete job/$jobNamePrefix-$k3sNode
done

##
# 获取job执行结果
##
for k3sNode in $selectedK3sNode
do
  /cache/kubectl logs job/$jobNamePrefix-$k3sNode | xargs -I{} echo {} >> $output
done

##
# 删除job
##
for k3sNode in $selectedK3sNode
do
  /cache/kubectl delete job/$jobNamePrefix-$k3sNode
done
