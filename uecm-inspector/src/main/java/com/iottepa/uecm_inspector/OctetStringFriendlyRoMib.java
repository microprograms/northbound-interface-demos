package com.iottepa.uecm_inspector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.friendlysnmp.AgentWorker;
import org.friendlysnmp.FException;
import org.friendlysnmp.FScalar;
import org.friendlysnmp.event.FScalarGetListener;
import org.friendlysnmp.mib.BaseMib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOServer;
import org.snmp4j.agent.mo.DefaultMOFactory;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOFactory;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

public class OctetStringFriendlyRoMib extends BaseMib {
    private static final Logger logger = LoggerFactory.getLogger(OctetStringFriendlyRoMib.class);
    private MibInitCallback mibInitCallback;
    private Map<String, OctetStringFriendlyRoScalar> _map = new HashMap<>();

    public OctetStringFriendlyRoMib(MibInitCallback mibInitCallback) {
        this.mibInitCallback = mibInitCallback;
    }

    @Override
    public void init(AgentWorker agentWorker) throws FException {
        super.init(agentWorker);
        for (OctetStringFriendlyRoScalar x : mibInitCallback.buildFriendlyRoScalarList(agentWorker)) {
            _map.put(x.getName(), x);
            addNode(x.getFriendlyScalar());
        }
    }

    @Override
    public void registerMOs(MOServer server, OctetString context) throws DuplicateRegistrationException {
        _map.forEach((k, v) -> {
            try {
                server.register(v.getScalar(), context);
            } catch (DuplicateRegistrationException e) {
                logger.error("registerMOs error", e);
            }
        });
    }

    @Override
    public void unregisterMOs(MOServer server, OctetString context) {
        _map.forEach((k, v) -> server.unregister(v.getScalar(), context));
    }

    public OctetStringFriendlyRoScalar get(String name) {
        return _map.get(name);
    }

    public static interface MibInitCallback {
        List<OctetStringFriendlyRoScalar> buildFriendlyRoScalarList(AgentWorker agentWorker);
    }

    public static class OctetStringFriendlyRoScalar {
        private static final String TC_MODULE_SNMPV2_TC = "SNMPv2-TC";
        private static final String TC_DISPLAYSTRING = "DisplayString";
        private static final MOFactory MO_FACTORY = DefaultMOFactory.getInstance();

        private final String name;
        private final MOScalar<OctetString> scalar;
        private final FScalar friendlyScalar;

        public OctetStringFriendlyRoScalar(String name, String oid, AgentWorker agentWorker,
                FScalarGetListener friendlyScalarGetListener) {
            this(name, new OID(oid), agentWorker, friendlyScalarGetListener);
        }

        public OctetStringFriendlyRoScalar(String name, OID oid, AgentWorker agentWorker,
                FScalarGetListener friendlyScalarGetListener) {
            this.name = name;
            this.scalar = buildRoScalar(oid);
            this.friendlyScalar = buildFriendlyRoScalar(name, scalar, agentWorker);
            this.friendlyScalar.addGetListener(friendlyScalarGetListener);
        }

        public String getName() {
            return name;
        }

        public MOScalar<OctetString> getScalar() {
            return scalar;
        }

        public FScalar getFriendlyScalar() {
            return friendlyScalar;
        }

        private static MOScalar<OctetString> buildRoScalar(OID oid) {
            return MO_FACTORY.createScalar(oid,
                    MO_FACTORY.createAccess(MOAccessImpl.ACCESSIBLE_FOR_READ_ONLY),
                    null,
                    TC_MODULE_SNMPV2_TC, TC_DISPLAYSTRING);
        }

        private static FScalar buildFriendlyRoScalar(String name, MOScalar<OctetString> scalar,
                AgentWorker agentWorker) {
            return new FScalar(name, scalar, agentWorker);
        }
    }
}