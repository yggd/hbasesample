package org.yggd.spark.sample.misc;


import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaDStreamSample {
    private static final Logger logger = LoggerFactory.getLogger(JavaDStreamSample.class);
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf().setAppName(JavaDStreamSample.class.getName())
                .setMaster("local[*]")
                .set("spark.io.compression.codec", "lz4");
        JavaSparkContext ctx = new JavaSparkContext(sparkConf);
        JavaStreamingContext ssc = new JavaStreamingContext(ctx, Durations.seconds(5));
        // 起動後に/tmp/ssc配下のファイルが置かれたら1行ずつ読み取る。
        JavaDStream<String> stream = ssc.textFileStream("/tmp/ssc");
        stream.foreachRDD( v1 -> {

            v1.foreach(s -> logger.info("   rec[{}]", s));
            return null;
        });
        ssc.start();
        ssc.awaitTermination();
        logger.info("**** Termination.");
        ssc.stop();
    }
}
