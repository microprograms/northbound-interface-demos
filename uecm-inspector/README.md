# uecm-inspector

```shell
# 在项目根目录执行

mvn clean && mvn compile assembly:single && docker build --no-cache -f docker/Dockerfile -t uecm-inspector:6.0.0 . # 打包为docker镜像

docker save uecm-inspector:6.0.0 -o uecm-inspector-airgap-images-slim.tar # 导出离线镜像包

# 在服务器执行（在k3s集群中运行）

helm install uecm-inspector uecm-inspector-6.0.0.tgz # 在k3s集群中运行uecm-inspector，监听宿主机30161端口，udp协议

kubectl get pod -o name | sed 's/pod\///' | grep 'uecm-inspector' | head -n 1 | xargs -I{} kubectl cp {}:uecm-inspector $HOME/uecm-inspector && cd $HOME/uecm-inspector && sh install-mib.sh && cd - > /dev/null # 下载uecm-inspector的mib文件

snmpwalk -t 60 -v2c -c public localhost:30161 SNMPv2-SMI::enterprises.29091.10 # 遍历OID，超时时间60s

# 在服务器执行（脱离k3s集群，用docker运行）

docker run -d -p 1161:1161/udp --name uecm-inspector uecm-inspector:6.0.0 # 在宿主机运行uecm-inspector，监听宿主机1161端口，udp协议

docker run --rm -v $HOME:/hostpath --entrypoint cp uecm-inspector:6.0.0 -r uecm-inspector /hostpath && cd $HOME/uecm-inspector && sh install-mib.sh && cd - > /dev/null # 下载uecm-inspector的mib文件

snmpwalk -v2c -c public localhost:1161 SNMPv2-SMI::enterprises.29091.10 # 遍历OID
```