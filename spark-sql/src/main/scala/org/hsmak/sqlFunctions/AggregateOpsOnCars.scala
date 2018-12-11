package org.hsmak.sqlFunctions

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

object AggregateOpsOnCars extends App {

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
    .csv(s"$base_data_dir/car-mileage.csv")


  println("carMileageDF has " + carMileageDF.count() + " rows")
  carMileageDF.show(5)
  carMileageDF.printSchema()

  carMileageDF.describe("mpg", "hp", "weight", "automatic").show()

  carMileageDF.groupBy("automatic").avg("mpg", "torque").show()

  // Even though the following is possible, rather it is awkward. avg() is an aggregate function, so use `df.agg()`.
  carMileageDF.groupBy().avg("mpg", "torque").show()

  // Do the following instead of the previous line

  // import aggregate functions
  import org.apache.spark.sql.functions.{avg, mean}

  carMileageDF.agg(avg(carMileageDF("mpg")), mean(carMileageDF("torque"))).show()
  carMileageDF.groupBy("automatic").agg(avg(carMileageDF("mpg")), mean(carMileageDF("torque"))).show()


  /** *******************************************
    * ########## Using the $ Notation ###########
    * *******************************************/

  val sqlContext = spark.sqlContext
  //import the $ notation
  import sqlContext.implicits._
//  import spark.implicits._ // alternative to the previous line

  // $ is useful when an operation needd to be performed on the value of that column instead of the string value of the column name
  carMileageDF.agg(avg($"mpg"), mean($"torque")).show()
  carMileageDF.groupBy($"automatic").agg(avg($"mpg"), mean($"torque")).show()

}