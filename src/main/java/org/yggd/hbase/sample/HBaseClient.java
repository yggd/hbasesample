package org.yggd.hbase.sample;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HBaseClient {

    public static final Logger logger = LoggerFactory.getLogger(HBaseClient.class);

    public static void main(String[] args) {
        new HBaseClient().execute();
    }

    private void execute() {
        try (Connection con = connection("quickstart.cloudera")) {
            Table t = table(con, "test1");
            ResultScanner scanner = t.getScanner(new Scan());
            logger.info("**** Scan to test1");
            for (Result result : scanner) {
                logger.info("    data1[{}] data2[{}]",
                        getResultValue(result, "data1", ""),
                        getResultValue(result, "data2", ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Connection connection(String zooKeeperHost) throws IOException {
        Configuration cnf = HBaseConfiguration.create();
        // use default port 2181, 60020
        cnf.set("hbase.zookeeper.quorum", zooKeeperHost);
        return ConnectionFactory.createConnection(cnf);
    }

    public String getResultValue(Result result, String family, String qualifier) {
        return Bytes.toString(
                result.getValue(Bytes.toBytes(family), Bytes.toBytes(qualifier))
        );
    }

    public Table table(Connection con, String name) throws IOException {
        return con.getTable(TableName.valueOf(name));
    }
}
