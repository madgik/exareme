/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.remote.operator.admin;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.master.connector.AdpDBConnectorUtil;
import madgik.exareme.master.connector.DataSerialization;
import madgik.exareme.worker.art.concreteOperator.AbstractNiNo;
import madgik.exareme.worker.art.container.netMgr.NetSession;
import madgik.exareme.worker.art.container.netMgr.session.NetSessionSimple;
import madgik.exareme.worker.art.parameter.Parameter;
import org.apache.log4j.Logger;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author heraldkllapi
 */
public class AdpDBNetReaderOperator extends AbstractNiNo {
    private final transient Logger log = Logger.getLogger(AdpDBNetReaderOperator.class);

    public AdpDBNetReaderOperator() {
    }

    @Override public void run() {
        try {
            log.debug("---- NetReader ----");
            for (Parameter parameter : getParameterManager().getParameters()) {
                log.debug(parameter.getName() + " = " + parameter.getValue());
            }
            log.debug("---- NetReader ----");

            Map<String, Object> alsoIncludeProps = null;
            boolean sendHeader = Boolean
                .parseBoolean(getParameterManager().getParameter("sendHeader").get(0).getValue());
            if (sendHeader == false) {
                alsoIncludeProps = null;
            } else {

                alsoIncludeProps = new HashMap<String, Object>();
                alsoIncludeProps.put("time", -1);
                alsoIncludeProps.put("errors", new ArrayList<Object>());
            }
            log.debug("---- sendHeader : " + sendHeader);

            String database = getParameterManager().getParameter("database").get(0).getValue();
            log.debug("---- database : " + database);

            String tabName = getParameterManager().getParameter("table").get(0).getValue();
            log.debug("---- tableName : " + tabName);

            int part =
                Integer.parseInt(getParameterManager().getParameter("part").get(0).getValue());
            log.debug("---- part : " + part);

            String ip = getParameterManager().getParameter("ip").get(0).getValue();
            log.debug("---- ip : " + ip);

            int port =
                Integer.parseInt(getParameterManager().getParameter("port").get(0).getValue());
            log.debug("--- port : " + port);

            DataSerialization dataSerialization = DataSerialization
                .valueOf(getParameterManager().getParameter("dataSerialization").get(0).getValue());
            if (dataSerialization == null) dataSerialization = DataSerialization.ldjson;
            log.debug("--- ds : " + dataSerialization);

            NetSession net = new NetSessionSimple();
            OutputStream out = net.openOutputStream(new EntityName(ip + "_" + port, ip, port));

            AdpDBConnectorUtil.readLocalTablePart(tabName, part, database, alsoIncludeProps, dataSerialization, out);
            out.close();
            log.debug("---- NetReader ----");
        } catch (Exception e) {
            log.error("Cannot get results", e);
        }
        exit(0);
    }
}
