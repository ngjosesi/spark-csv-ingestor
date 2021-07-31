# spark-csv-stream-ingestor

## Steps to start up the server

Go to Open up Windows Powershell and go to %HADOOP_HOME%/sbin

```shell
cd %HADOOP_HOME/sbin
```

Start up hdfs using powershell

```shell
.\start-dfs.cmd
```

Start up yarn using powershell

```shell
.\start-dfs.cmd
```

Try out doing hdfs dfs commands

```shell
hdfs dfs -ls /
```

Accessible URLS
Namenode webpage - http://localhost:9870/dfshealth.html
Datanode webpage - http://localhost:9864/datanode.html
Yarn webpage - http://localhost:8088/cluster