# spark-csv-ingestor

## Setting up hadoop in local

If you haven't yet, you can setup hadoop in your local using the following links
1. [Setup Hadoop server](https://towardsdatascience.com/installing-hadoop-3-2-1-single-node-cluster-on-windows-10-ac258dd48aef)
2. [Setup Hive server](https://towardsdatascience.com/installing-apache-hive-3-1-2-on-windows-10-70669ce79c79)
3. [Setup Spark](https://kontext.tech/column/spark/450/install-spark-300-on-windows-10)

## Steps to start up the server

Using Powershell, go to %HADOOP_HOME%/sbin to start up hdfs and yarn

```shell
cd %HADOOP_HOME%/sbin
./start-dfs.cmd
./start-yarn.cmd
```

Try out doing hdfs dfs commands

```shell
hdfs dfs -ls /
```

Using powershell, Start up derby instance

```shell
cd %DERBY_HOME%\bin
StartNetworkServer -h 0.0.0.0
```

Using powershell/cygwin, Start up Hive metastore server and hiveQL

```shell
hive --service hiveserver2 start
# use cygwin for below
$HIVE_HOME/hcatalog/sbin/webhcat_server.sh start
```

Test hive connectivity

```shell
hive
show databases
```

Start up hcat server to later let spark connect to thrift server

```shell
# use cygwin for below
./hcatalog/sbin/hcat_server.sh start
```

Accessible URLS
Namenode webpage - http://localhost:9870/dfshealth.html
Datanode webpage - http://localhost:9864/datanode.html
Yarn webpage - http://localhost:8088/cluster

## Setting up the schema and test data

Go to hive and run the following

```shell
create database test_schema;
use test_schema;

CREATE TABLE IF NOT EXISTS residential_property_transactions( sn STRING, project_name STRING, street_name STRING, type STRING, postal_district STRING, market_segment STRING, tenure STRING, type_of_sale STRING, no_of_units STRING, price_sgd STRING, nett_price_sgd STRING, area_sqft STRING, type_of_area STRING, floor_level STRING, unit_price_psf STRING, date_of_sale STRING)
    ROW FORMAT DELIMITED
    FIELDS TERMINATED BY ','
    STORED AS TEXTFILE
    location 'hdfs://localhost:9820/residential_property_transactions/'
    tblproperties ("skip.header.line.count"="1");
```

(Optional) Populate the hdfs with our test data to see if we will be able to fetch the content properly

```shell
hdfs dfs -copyFromLocal data.csv /residential_property_transactions/
```

Go to hive and run the following. The output should be 1418

```shell

msck repair table test_schema.residential_property_transactions;
select count(*) from test_schema.residential_property_transactions

```

Create another table that is using Parquet format

```shell
create database test_schema;
use test_schema;

CREATE TABLE IF NOT EXISTS residential_property_transactions_parquet( sn INT, project_name STRING, street_name STRING, type STRING, postal_district STRING, market_segment STRING, tenure STRING, type_of_sale STRING, no_of_units INT, price_sgd INT, nett_price_sgd INT, area_sqft INT, type_of_area STRING, floor_level STRING, unit_price_psf INT, date_of_sale STRING) STORED AS PARQUET location 'hdfs://localhost:9820/residential_property_transactions_parquet/';
```

Test if we are able to insert data from the CSV table to parquet table. If yes, then that means our CSV can sync with the parquet table which we will use spark for

```shell
insert overwrite table residential_property_transactions_parquet select cast(sn as int), project_name, street_name, type,  postal_district, market_segment, tenure, type_of_sale, cast(no_of_units as int), cast(price_sgd as int), cast(nett_price_sgd as int), cast(area_sqft as int), type_of_area, floor_level, cast(unit_price_psf as int), date_of_sale from residential_property_transactions;
```

## Test Spark Connectivity

Tets if you are able to access spark properly by using the following commands

```shell
spark-shell
pyspark
spark-sql
```

Test if you are able to run one of Spark's default test programs by running below. It should call shutdown properly and not throw any exceptions.

```shell
%SPARK_HOME%\bin\run-example.cmd SparkPi 10
```

While a spark-shell instance is running, go to http://localhost:4040/ and see if you are able to access properly.
