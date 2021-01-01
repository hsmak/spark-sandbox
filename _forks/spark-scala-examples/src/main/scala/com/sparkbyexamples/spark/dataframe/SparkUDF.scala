package com.sparkbyexamples.spark.dataframe

import com.sparkbyexamples.spark.MyContext
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{Row, SparkSession}

object SparkUDF extends App with MyContext {

  val spark: SparkSession = SparkSession.builder()
    .master("local[1]")
    .appName("SparkByExamples.com")
    .getOrCreate()

  import spark.implicits._
  val columns = Seq("Seqno","Quote")
  val data = Seq(
    ("1", "Be the change that you wish to see in the world"),
    ("2", "Everyone thinks of changing the world, but no one thinks of changing himself."),
    ("3", "The purpose of our lives is to be happy."))

  val df = data.toDF(columns:_*) // flatten out columns seq
  df.show(false)

  val convertCase: (String => String) =
//    rowStr =>  rowStr.split(" ").map(str =>  str.substring(0,1).toUpperCase + str.substring(1,str.length)).mkString(" ") // Java way
    rowStr =>  rowStr.split(" ").map(str =>  str.head.toUpper+:str.tail).mkString(" ") // <- Scala way


  //Using with DataFrame
  val convertUDF = udf(convertCase)
  df.select(col("Seqno"),
    convertUDF(col("Quote")).as("Quote") ).show(false)

  // Using it on SQL
  spark.udf.register("convertUDF", convertCase)

  df.createOrReplaceTempView("QUOTE_TABLE")
  spark.sql("select Seqno, convertUDF(Quote) from QUOTE_TABLE").show(false)

}
