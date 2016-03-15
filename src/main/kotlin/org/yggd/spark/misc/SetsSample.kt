package org.yggd.spark.misc

import org.yggd.spark.sample.SparkClient
import scala.Tuple2
import java.io.Serializable
import java.util.*

fun main(args : Array<String>) {

    // for left join
    val lists1 = arrayListOf(
            Tuple2("001", Tuple2("Taro", 24)),
            Tuple2("002", Tuple2("Jiro", 13)),
            Tuple2("003", Tuple2("Saburo", 43)),
            Tuple2("004", Tuple2("Siro1", 38)),
            Tuple2("004", Tuple2("Siro2", 38)),
            Tuple2("005", Tuple2("Goro", 18)),
            Tuple2("006", Tuple2("Rokuro", 29)),
            Tuple2("007", Tuple2("Nanako", 41)),
            Tuple2("xxx", Tuple2("Hachibe", 17)),
            Tuple2("009", Tuple2("KyuTaro", 16)),
            Tuple2("010", Tuple2("Toh", 33)))

    // for right join
    val lists2 = arrayListOf(
            Tuple2("001", "Tokyo"),
            Tuple2("aaa", "Kanagawa"),
            Tuple2("003", "Tochigi"),
            Tuple2("004", "Saitama"),
            Tuple2("005", "Osaka"),
            Tuple2("006", "Aichi"),
            Tuple2("007", "Akita"),
            Tuple2("xxx", "Hokkaido"),
            Tuple2("009", "Chiba"))

    val sc = SparkClient.javaSparkContext(SparkClient::class.java)
    sc.use {
        val pairRDD1 = sc.parallelizePairs(lists1)
        val pairRDD2 = sc.parallelizePairs(lists2)

        val f = Cmp<String> { s, t ->  s.compareTo(t) }

        // join sample
        println("*** join:")
        pairRDD1
            .join(pairRDD2)
            .sortByKey(f)
            .collect()
            .forEach { t ->
                println("key:${t._1} value(name):${t._2._1._1} value(age):${t._2._1._2} value(pref):${t._2._2}")}

        // full outer join sample
        println("*** full outer join:")
        pairRDD1
            .fullOuterJoin(pairRDD2)
            .sortByKey(f)
            .collect()
            .forEach { t ->
                println("key:${t._1} value(name):${t._2._1.orNull()?._1} value(age):${t._2._1.orNull()?._2} value(pref):${t._2._2.or("absent!")}")}

        // distinct sample(not work by key distinct...)
        println("*** distinct:")
        pairRDD1
            .distinct()
            .sortByKey(f)
            .collect()
            .forEach { t ->
                println("key:${t._1} value(name):${t._2._1} value(age):${t._2._2}")}

        // groupByKey sample
        println("*** distinct by key(using groupByKey):")
        pairRDD1
            .groupByKey()
            .mapToPair{ t -> Tuple2(t._1, Tuple2(t._2.first()._1, t._2.first()._2)) }
            .sortByKey(f)
            .collect()
            .forEach { t ->
                println("key:${t._1} value(name):${t._2._1} value(age):${t._2._2}")}
    }
}

fun <T> Cmp(comp: (T, T) -> Int): Comparator<T> = object : Comparator<T>, Serializable {
    override fun compare(t1: T, t2: T) = comp(t1, t2)
}

