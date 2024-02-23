package com.iottepa.uecm_inspector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.friendlysnmp.FException;
import org.friendlysnmp.FriendlyAgent;
import org.snmp4j.smi.OID;

import com.iottepa.uecm_inspector.OctetStringFriendlyRoMib.OctetStringFriendlyRoScalar;
import com.iottepa.uecm_inspector.command_executor.CommandExecutor;

public class UecmInspectorApplication {
	private static final String TITLE = "UECM inspector";
	private static final String VERSION = "Build 20220829";
	private static final String PROP_FILE = "friendly-agent.properties";

	public static void main(String[] args) throws IOException, FException {
		Properties prop = FriendlyAgent.loadProps(UecmInspectorApplication.class, PROP_FILE);
		FriendlyAgent agent = new FriendlyAgent(TITLE, VERSION, prop);
		agent.addMIB(new OctetStringFriendlyRoMib(agentWorker -> {
			List<OctetStringFriendlyRoScalar> list = new ArrayList<>();

			// 监控范围1 宿主机
			list.add(new OctetStringFriendlyRoScalar("hostTime",
					new OID(SnmpHelper.OID_DOMAIN_HOST).append("1.0"),
					agentWorker, x -> {
						x.setValueEx(CommandExecutor.executeShellFile("hostTime", 60 * 1000));
					}));
			list.add(new OctetStringFriendlyRoScalar("hostMemory",
					new OID(SnmpHelper.OID_DOMAIN_HOST).append("2.0"),
					agentWorker, x -> {
						x.setValueEx(CommandExecutor.executeShellFile("hostMemory", 60 * 1000));
					}));
			list.add(new OctetStringFriendlyRoScalar("hostDisk",
					new OID(SnmpHelper.OID_DOMAIN_HOST).append("3.0"),
					agentWorker, x -> {
						x.setValueEx(CommandExecutor.executeShellFile("hostDisk", 60 * 1000));
					}));

			// 监控范围2 k8s集群
			list.add(new OctetStringFriendlyRoScalar("clusterCoredns",
					new OID(SnmpHelper.OID_DOMAIN_CLUSTER).append("1.0"),
					agentWorker, x -> {
						x.setValueEx(CommandExecutor.executeShellFile("clusterCoredns", 60 * 1000));
					}));

			// 监控范围3 中间件
			list.add(new OctetStringFriendlyRoScalar("middlewareTidb",
					new OID(SnmpHelper.OID_DOMAIN_MIDDLEWARE).append("1.0"),
					agentWorker, x -> x.setValueEx("TiDB running")));
			list.add(new OctetStringFriendlyRoScalar("middlewareRabbitmq",
					new OID(SnmpHelper.OID_DOMAIN_MIDDLEWARE).append("2.0"),
					agentWorker, x -> x.setValueEx("RabbitMQ running")));
			list.add(new OctetStringFriendlyRoScalar("middlewareRedis",
					new OID(SnmpHelper.OID_DOMAIN_MIDDLEWARE).append("3.0"),
					agentWorker, x -> x.setValueEx("Redis running")));
			list.add(new OctetStringFriendlyRoScalar("middlewareMongodb",
					new OID(SnmpHelper.OID_DOMAIN_MIDDLEWARE).append("4.0"),
					agentWorker, x -> x.setValueEx("MongoDB running")));
			list.add(new OctetStringFriendlyRoScalar("middlewareXxljob",
					new OID(SnmpHelper.OID_DOMAIN_MIDDLEWARE).append("5.0"),
					agentWorker, x -> x.setValueEx("XXL-JOB running")));
			list.add(new OctetStringFriendlyRoScalar("middlewareNacos",
					new OID(SnmpHelper.OID_DOMAIN_MIDDLEWARE).append("6.0"),
					agentWorker, x -> x.setValueEx("Nacos running")));

			// 监控范围4 微服务
			list.add(new OctetStringFriendlyRoScalar("microserviceAuthlocal",
					new OID(SnmpHelper.OID_DOMAIN_MICROSERVICE).append("1.0"),
					agentWorker, x -> x.setValueEx("auth-local running")));
			list.add(new OctetStringFriendlyRoScalar("microserviceChangeCounterfeit",
					new OID(SnmpHelper.OID_DOMAIN_MICROSERVICE).append("2.0"),
					agentWorker, x -> x.setValueEx("change-counterfeit running")));

			// 监控范围5 其他
			list.add(new OctetStringFriendlyRoScalar("otherInfo",
					new OID(SnmpHelper.OID_DOMAIN_OTHER).append("1.0"),
					agentWorker, x -> {
						x.setValueEx(CommandExecutor.executeShellFile("otherInfo", 60 * 1000));
					}));

			return list;
		}));
		agent.init();
		agent.start();
	}

}
