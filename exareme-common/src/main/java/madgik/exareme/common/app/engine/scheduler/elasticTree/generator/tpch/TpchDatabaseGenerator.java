/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.generator.tpch;


import madgik.exareme.common.app.engine.scheduler.elasticTree.generator.DatabaseGenerator;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.data.Database;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.data.Table;

/**
 * @author heraldkllapi
 */
public class TpchDatabaseGenerator implements DatabaseGenerator {
    private final double scale;
    private final int parts;

    public TpchDatabaseGenerator(double scale, int parts) {
        this.scale = scale;
        this.parts = parts;
    }

    @Override public Database generateDatabase() {
        Database db = new Database();
        // lineitem 71.3 %
        Table lineitem = new Table("lineitem");
        for (int i = 0; i < parts; ++i) {
            lineitem.addPartition(0.713 * scale * 1024.0 / parts);
        }
        db.addTable(lineitem);
        // orders 13.8 %
        Table orders = new Table("orders");
        for (int i = 0; i < parts; ++i) {
            orders.addPartition(0.138 * scale * 1024.0 / parts);
        }
        db.addTable(orders);
        // partsupp 10.5 %
        Table partsupp = new Table("partsupp");
        partsupp.addPartition(0.105 * scale * 1024.0);
        db.addTable(partsupp);
        // part 2.2 %
        Table part = new Table("part");
        part.addPartition(0.022 * scale * 1024.0);
        db.addTable(part);
        // customer 2.1 %
        Table customer = new Table("customer");
        customer.addPartition(0.021 * scale * 1024.0);
        db.addTable(customer);
        // supplier 0.1 %
        Table supplier = new Table("supplier");
        supplier.addPartition(0.001 * scale * 1024.0);
        db.addTable(supplier);
        // region
        Table region = new Table("region");
        region.addPartition(1.0);
        db.addTable(region);
        // nation
        Table nation = new Table("nation");
        nation.addPartition(1.0);
        db.addTable(nation);
        return db;
    }
}
