package madgik.exareme.master.engine.executor.remote.operator.admin;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.master.connector.AdpDBConnectorUtil;
import madgik.exareme.worker.art.concreteOperator.AbstractMiMo;
import madgik.exareme.worker.art.container.netMgr.NetSession;
import madgik.exareme.worker.art.container.netMgr.session.NetSessionSimple;
import madgik.exareme.worker.art.parameter.Parameter;
import org.apache.log4j.Logger;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author alex
 */
public class DBAdminWorker extends AbstractMiMo {
    private static final Logger log = Logger.getLogger(DBAdminWorker.class);

    @Override
    public void run() throws Exception {
        try {
            log.debug("---- DBAdminWorker ----");
            for (Parameter parameter : getParameterManager().getParameters()) {
                log.debug(parameter.getName() + " = " + parameter.getValue());
            }
            log.debug("---- DBAdminWorker ----");

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

            NetSession net = new NetSessionSimple();
            OutputStream out = net.openOutputStream(new EntityName(ip + "_" + port, ip, port));

            AdpDBConnectorUtil.readLocalTablePart(tabName, part, database, alsoIncludeProps, out);
            out.close();
            log.debug("---- DBAdminWorker ----");

        } catch (Exception e) {
            log.error("Cannot get results", e);
        }
        exit(0);
    }

}
