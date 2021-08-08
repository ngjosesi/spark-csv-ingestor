import org.apache.log4j.Level;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkIngestorApp {

  private static final Logger logger = LoggerFactory.getLogger(SparkIngestorApp.class);

  public static void main(String[] args) throws Exception {
    org.apache.log4j.Logger.getLogger("org").setLevel(Level.ERROR);
    org.apache.log4j.Logger.getLogger("akka").setLevel(Level.ERROR);

    String sourceFile = "file:///J:\\projects\\spark-csv-ingestor\\data.csv"; // Should be some file on your system
    String destinationFolder = "/residential_property_transactions_parquet/";
    String parquetTable = "test_schema.residential_property_transactions_parquet";

    SparkSession spark = SparkSession
                         .builder()
                         .appName("Simple Application")
                         .config("spark.sql.warehouse.dir", "/user/hive/warehouse/")
                         .enableHiveSupport()
                         .getOrCreate();

    logger.info("Reading the file: {}", sourceFile);
    Dataset<Row> data = spark.read().csv(sourceFile).toDF("sn", "project_name", "street_name", "type",  "postal_district", "market_segment",
                                                          "tenure", "type_of_sale", "no_of_units", "price_sgd", "nett_price_sgd", "area_sqft",
                                                          "type_of_area", "floor_level", "unit_price_psf", "date_of_sale");
    long expectedCountCheck = data.count();
    logger.info("Total rows of data in the file is {}", expectedCountCheck);

    data = data.withColumn("sn",data.col("sn").cast("int"))
               .withColumn("no_of_units",data.col("no_of_units").cast("int"))
               .withColumn("price_sgd",data.col("price_sgd").cast("int"))
               .withColumn("nett_price_sgd",data.col("nett_price_sgd").cast("int"))
               .withColumn("area_sqft",data.col("area_sqft").cast("int"))
               .withColumn("unit_price_psf",data.col("unit_price_psf").cast("int"));

    logger.info("Writing into parquet location");
    data.write().mode("overwrite").parquet(destinationFolder);
    logger.info("Completed writing into parquet location");

    logger.info("Performing Count check....");

    long actualCountCheck = spark.sql("select * from "+parquetTable).count();

    logger.info("Count check output {}", actualCountCheck);

    if(expectedCountCheck == actualCountCheck) {
      logger.info("Counts are matching!");
    } else {
      logger.info("Counts are not matching expectedCountCheck = {} vs actualCountCheck = {}", expectedCountCheck, actualCountCheck);
      throw new Exception("Counts not matching");
    }

    spark.stop();
  }
}