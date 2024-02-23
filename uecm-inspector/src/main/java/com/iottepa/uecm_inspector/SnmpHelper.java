package com.iottepa.uecm_inspector;

import org.snmp4j.smi.OID;

public class SnmpHelper {

    public static final OID OID_ENTERPRISE_FRIENDLY_SNMP = new OID("1.3.6.1.4.1.29091"); // oid: 企业
    public static final OID OID_UECM_INSPECTOR = new OID(OID_ENTERPRISE_FRIENDLY_SNMP).append(10); // oid: uecm检视
    public static final OID OID_DOMAIN_HOST = new OID(OID_UECM_INSPECTOR).append(1); // oid: 监控领域1（宿主机）
    public static final OID OID_DOMAIN_CLUSTER = new OID(OID_UECM_INSPECTOR).append(2); // oid: 监控领域2（k8s集群）
    public static final OID OID_DOMAIN_MIDDLEWARE = new OID(OID_UECM_INSPECTOR).append(3); // oid: 监控领域3（中间件）
    public static final OID OID_DOMAIN_MICROSERVICE = new OID(OID_UECM_INSPECTOR).append(4); // oid: 监控领域4（微服务）
    public static final OID OID_DOMAIN_OTHER = new OID(OID_UECM_INSPECTOR).append(5); // oid: 监控领域5（其他）

}
