package org.yggd.spark.sample;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

public class SparkClient {

    private static final Logger logger = LoggerFactory.getLogger(SparkClient.class);

    public static void main(String[] args) {execute();}

    public static void execute() {

        SparkConf sparkConf = new SparkConf().setAppName(SparkClient.class.getName()).setMaster("local[*]");

        try (JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf)) {
            JavaPairRDD<ImmutableBytesWritable, Result> result = resolveRDD(javaSparkContext);

            logger.info("**** Scan to test1");

            result.foreach(t ->
                logger.info("    KEY[{}] VALUE(data1)[{}] VALUE(data2)[{}]",
                    getKey(t),
                    getStringValue(t, "data1", ""),
                    getStringValue(t, "data2", ""))
            );
        }
    }

    public static JavaPairRDD<ImmutableBytesWritable, Result> resolveRDD(JavaSparkContext context) {

        // on HBase to RDD
        Configuration hbaseConf = HBaseConfiguration.create();
        hbaseConf.set("hbase.zookeeper.quorum", "quickstart.cloudera");
        hbaseConf.set(TableInputFormat.INPUT_TABLE, "test1");

        return context.newAPIHadoopRDD(hbaseConf, TableInputFormat.class,
                ImmutableBytesWritable.class, Result.class);
    }

    public static String getKey(Tuple2<ImmutableBytesWritable, Result> t) {
        return Bytes.toString(t._1.get());
    }

    public static String getStringValue(Tuple2<ImmutableBytesWritable, Result> t, String colFamily, String qualifier) {
        return Bytes.toString(t._2.getValue(Bytes.toBytes(colFamily), Bytes.toBytes(qualifier)));
    }
}
