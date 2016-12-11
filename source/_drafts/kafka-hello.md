---

title: kafka 入门
date: 2016-8-11

---


下载  
       http://kafka.apache.org/downloads.html
       
解压  
       tar -zxvf kafka_2.10-0.8.1.1.tgz
       
启动服务  
      首先启动zookeeper服务  
      bin/zookeeper-server-start.sh config/zookeeper.properties  
      启动Kafka  
      bin/kafka-server-start.sh config/server.properties >/dev/null 2>&1 &  
      
创建topic  
        创建一个"test"的topic，一个分区一个副本
          bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
          
查看主题  
       bin/kafka-topics.sh --list --zookeeper localhost:2181
       
查看主题详情  
        bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic test
        
删除主题  
        bin/kafka-run-class.sh kafka.admin.TopicCommand –delete --topic test --zookeeper 192.168.1.161:2181


创建生产者 producer
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test 

创建消费者 consumer
bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic test --from-beginning

参数使用帮组信息查看：
生产者参数查看：bin/kafka-console-producer.sh
消费者参数查看：bin/kafka-console-consumer.sh


安装zk集群
修改配置文件详情
broker.id：   唯一，填数字
host.name：唯一，填服务器
zookeeper.connect=192.168.40.134:2181,192.168.40.132:2181,192.168.40.133:2181






