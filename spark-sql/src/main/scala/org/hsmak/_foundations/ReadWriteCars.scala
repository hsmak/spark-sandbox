package org.hsmak._foundations

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{SaveMode, SparkSession}

object ReadWriteCars extends App {

  //turn off Logging
  Logger.getLogger("org").setLevel(Level.OFF)


  case class Employee(EmployeeID: String,
                      LastName: String, FirstName: String, Title: String,
                      BirthDate: String, HireDate: String,
                      City: String, State: String, Zip: String, Country: String,
                      ReportsTo: String)

  case class Order(OrderID: String, CustomerID: String, EmployeeID: String,
                   OrderDate: String, ShipCountry: String)

  case class OrderDetails(OrderID: String, ProductID: String, UnitPrice: Double,
                          Qty: Int, Discount: Double)

  case class Product(ProductID: String, ProductName: String, UnitPrice: Double, UnitsInStock: Int, UnitsOnOrder: Int, ReorderLevel: Int, Discontinued: Int)


  val base_data_dir = s"file://${System.getProperty("user.dir")}/_data/car-data"


  /** ******************************************************
    * ############ Creating SparkSession ###########
    * ******************************************************/

  val spark = SparkSession
    .builder
    .master("local[*]") // ToDO: Which config takes precedence? MainApp hard-coded or spark-submit argument; mvn exec:exec?
    .appName("DatasetRunner")
    .getOrCreate()


  /** ******************************************************
    * ############ Creating DataFrames from CSVs ###########
    * ******************************************************/


  val carMileageDF = spark.read
    .option("header", "true")
    .option("inferSchema", "true")
    .csv(s"$base_data_dir/cars.csv")


  println("carMileageDF has " + carMileageDF.count() + " rows")
  carMileageDF.show(5)
  carMileageDF.printSchema()


  /** ******************************************************
    * ############ Writing DataFrames back #################
    * ******************************************************/

  //Write DF back in CSV format
  carMileageDF.write
    .mode(SaveMode.Overwrite)
    .option("header", true)
    .csv(s"${base_data_dir}/out/cars-out-csv")


  //Write DF back in Parquet format
  carMileageDF.write
    .mode(SaveMode.Overwrite)
    .option("header", true)
    .partitionBy("year")
    .parquet(s"${base_data_dir}/out/cars-out-pqt")

}