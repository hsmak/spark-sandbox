package com.sparkbyexamples.spark.dataframe


import com.sparkbyexamples.spark.MyContext
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{StringType, StructType}
import org.apache.spark.sql.Column
import org.apache.spark.sql.functions.col

/**
  * Observations:
  *     - Use "struct_name.*"     -> for nested structs
  *     - Use "explode(col_name)" -> for flattening an Array
  */
object FlattenNestedStruct extends App with MyContext{

  val spark: SparkSession = SparkSession.builder()
    .master("local[1]")
    .appName("SparkByExamples.com")
    .getOrCreate()

  val structureData = Seq(
    Row(Row("James ","","Smith"),Row(Row("CA","Los Angles"),Row("CA","Sandiago"))),
    Row(Row("Michael ","Rose",""),Row(Row("NY","New York"),Row("NJ","Newark"))),
    Row(Row("Robert ","","Williams"),Row(Row("DE","Newark"),Row("CA","Las Vegas"))),
    Row(Row("Maria ","Anne","Jones"),Row(Row("PA","Harrisburg"),Row("CA","Sandiago"))),
    Row(Row("Jen","Mary","Brown"),Row(Row("CA","Los Angles"),Row("NJ","Newark")))
  )

  val structureSchema = new StructType()
    .add("name",new StructType()

      .add("firstname",StringType)
      .add("middlename",StringType)
      .add("lastname",StringType))

    .add("address",new StructType()

      .add("current",new StructType()
        .add("state",StringType)
        .add("city",StringType))

      .add("previous",new StructType()
        .add("state",StringType)
        .add("city",StringType)))


  val df = spark.createDataFrame(
    spark.sparkContext.parallelize(structureData),structureSchema)
  df.printSchema()
  df.show(false)

  val df2 = df.select(col("name.*"),
    col("address.current.*"),
    col("address.previous.*"))
  df2.show(false)

  val df2Flatten = df2.toDF("fname","mename","lname","currAddState",
    "currAddCity","prevAddState","prevAddCity")
  df2Flatten.printSchema()
  df2Flatten.show(false)



  def flattenStructSchema(schema: StructType, prefix: String = null) : Array[Column] = {
    schema.fields.flatMap(sf => {
      val columnName = if (prefix == null) sf.name else (prefix + "." + sf.name)

      sf.dataType match {
        case st: StructType => flattenStructSchema(st, columnName) // if StructField is of type StructType, recurse into the function
        case _ => Array(col(columnName).as(columnName.replace(".","_")))
      }
    })
  }

  val df3 = df.select(flattenStructSchema(df.schema):_*)
  df3.printSchema()
  df3.show(false)
}
