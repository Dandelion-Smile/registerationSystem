-- MySQL dump 10.13  Distrib 8.0.42, for macos15.2 (arm64)
--
-- Host: 192.168.8.3    Database: compserve
-- ------------------------------------------------------
-- Server version	8.4.6

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `QRTZ_BLOB_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_BLOB_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_BLOB_TRIGGERS` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `blob_data` blob COMMENT '存放持久化Trigger对象',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='Blob类型的触发器表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_BLOB_TRIGGERS`
--

LOCK TABLES `QRTZ_BLOB_TRIGGERS` WRITE;
/*!40000 ALTER TABLE `QRTZ_BLOB_TRIGGERS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_BLOB_TRIGGERS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_CALENDARS`
--

DROP TABLE IF EXISTS `QRTZ_CALENDARS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_CALENDARS` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '日历名称',
  `calendar` blob NOT NULL COMMENT '存放持久化calendar对象',
  PRIMARY KEY (`sched_name`,`calendar_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='日历信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_CALENDARS`
--

LOCK TABLES `QRTZ_CALENDARS` WRITE;
/*!40000 ALTER TABLE `QRTZ_CALENDARS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_CALENDARS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_CRON_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_CRON_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_CRON_TRIGGERS` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `cron_expression` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cron表达式',
  `time_zone_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '时区',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='Cron类型的触发器表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_CRON_TRIGGERS`
--

LOCK TABLES `QRTZ_CRON_TRIGGERS` WRITE;
/*!40000 ALTER TABLE `QRTZ_CRON_TRIGGERS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_CRON_TRIGGERS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_FIRED_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_FIRED_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_FIRED_TRIGGERS` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `entry_id` varchar(95) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度器实例id',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度器实例名',
  `fired_time` bigint NOT NULL COMMENT '触发的时间',
  `sched_time` bigint NOT NULL COMMENT '定时器制定的时间',
  `priority` int NOT NULL COMMENT '优先级',
  `state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务名称',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务组名',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否并发',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否接受恢复执行',
  PRIMARY KEY (`sched_name`,`entry_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='已触发的触发器表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_FIRED_TRIGGERS`
--

LOCK TABLES `QRTZ_FIRED_TRIGGERS` WRITE;
/*!40000 ALTER TABLE `QRTZ_FIRED_TRIGGERS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_FIRED_TRIGGERS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_JOB_DETAILS`
--

DROP TABLE IF EXISTS `QRTZ_JOB_DETAILS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_JOB_DETAILS` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务组名',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '相关介绍',
  `job_class_name` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '执行任务类名称',
  `is_durable` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否持久化',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否并发',
  `is_update_data` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否更新数据',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否接受恢复执行',
  `job_data` blob COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`,`job_name`,`job_group`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='任务详细信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_JOB_DETAILS`
--

LOCK TABLES `QRTZ_JOB_DETAILS` WRITE;
/*!40000 ALTER TABLE `QRTZ_JOB_DETAILS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_JOB_DETAILS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_LOCKS`
--

DROP TABLE IF EXISTS `QRTZ_LOCKS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_LOCKS` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `lock_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '悲观锁名称',
  PRIMARY KEY (`sched_name`,`lock_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='存储的悲观锁信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_LOCKS`
--

LOCK TABLES `QRTZ_LOCKS` WRITE;
/*!40000 ALTER TABLE `QRTZ_LOCKS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_LOCKS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_PAUSED_TRIGGER_GRPS`
--

DROP TABLE IF EXISTS `QRTZ_PAUSED_TRIGGER_GRPS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  PRIMARY KEY (`sched_name`,`trigger_group`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='暂停的触发器表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_PAUSED_TRIGGER_GRPS`
--

LOCK TABLES `QRTZ_PAUSED_TRIGGER_GRPS` WRITE;
/*!40000 ALTER TABLE `QRTZ_PAUSED_TRIGGER_GRPS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_PAUSED_TRIGGER_GRPS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_SCHEDULER_STATE`
--

DROP TABLE IF EXISTS `QRTZ_SCHEDULER_STATE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_SCHEDULER_STATE` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '实例名称',
  `last_checkin_time` bigint NOT NULL COMMENT '上次检查时间',
  `checkin_interval` bigint NOT NULL COMMENT '检查间隔时间',
  PRIMARY KEY (`sched_name`,`instance_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='调度器状态表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_SCHEDULER_STATE`
--

LOCK TABLES `QRTZ_SCHEDULER_STATE` WRITE;
/*!40000 ALTER TABLE `QRTZ_SCHEDULER_STATE` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_SCHEDULER_STATE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_SIMPLE_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_SIMPLE_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_SIMPLE_TRIGGERS` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `repeat_count` bigint NOT NULL COMMENT '重复的次数统计',
  `repeat_interval` bigint NOT NULL COMMENT '重复的间隔时间',
  `times_triggered` bigint NOT NULL COMMENT '已经触发的次数',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='简单触发器的信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_SIMPLE_TRIGGERS`
--

LOCK TABLES `QRTZ_SIMPLE_TRIGGERS` WRITE;
/*!40000 ALTER TABLE `QRTZ_SIMPLE_TRIGGERS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_SIMPLE_TRIGGERS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_SIMPROP_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_SIMPROP_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_SIMPROP_TRIGGERS` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `str_prop_1` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'String类型的trigger的第一个参数',
  `str_prop_2` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'String类型的trigger的第二个参数',
  `str_prop_3` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'String类型的trigger的第三个参数',
  `int_prop_1` int DEFAULT NULL COMMENT 'int类型的trigger的第一个参数',
  `int_prop_2` int DEFAULT NULL COMMENT 'int类型的trigger的第二个参数',
  `long_prop_1` bigint DEFAULT NULL COMMENT 'long类型的trigger的第一个参数',
  `long_prop_2` bigint DEFAULT NULL COMMENT 'long类型的trigger的第二个参数',
  `dec_prop_1` decimal(13,4) DEFAULT NULL COMMENT 'decimal类型的trigger的第一个参数',
  `dec_prop_2` decimal(13,4) DEFAULT NULL COMMENT 'decimal类型的trigger的第二个参数',
  `bool_prop_1` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Boolean类型的trigger的第一个参数',
  `bool_prop_2` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Boolean类型的trigger的第二个参数',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='同步机制的行锁表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_SIMPROP_TRIGGERS`
--

LOCK TABLES `QRTZ_SIMPROP_TRIGGERS` WRITE;
/*!40000 ALTER TABLE `QRTZ_SIMPROP_TRIGGERS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_SIMPROP_TRIGGERS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_TRIGGERS` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器的名字',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器所属组的名字',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details表job_name的外键',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details表job_group的外键',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '相关介绍',
  `next_fire_time` bigint DEFAULT NULL COMMENT '上一次触发时间（毫秒）',
  `prev_fire_time` bigint DEFAULT NULL COMMENT '下一次触发时间（默认为-1表示不触发）',
  `priority` int DEFAULT NULL COMMENT '优先级',
  `trigger_state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器状态',
  `trigger_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器的类型',
  `start_time` bigint NOT NULL COMMENT '开始时间',
  `end_time` bigint DEFAULT NULL COMMENT '结束时间',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '日程表名称',
  `misfire_instr` smallint DEFAULT NULL COMMENT '补偿执行的策略',
  `job_data` blob COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  KEY `sched_name` (`sched_name`,`job_name`,`job_group`) USING BTREE,
  CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `QRTZ_JOB_DETAILS` (`sched_name`, `job_name`, `job_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='触发器详细信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_TRIGGERS`
--

LOCK TABLES `QRTZ_TRIGGERS` WRITE;
/*!40000 ALTER TABLE `QRTZ_TRIGGERS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_TRIGGERS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `competition`
--

DROP TABLE IF EXISTS `competition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `competition` (
  `competition_id` bigint NOT NULL AUTO_INCREMENT COMMENT '竞赛ID，主键',
  `competition_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '竞赛名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '竞赛描述',
  `competition_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '竞赛类型（算法设计、软件开发、创新创业等）',
  `register_start_time` datetime NOT NULL COMMENT '报名开始时间',
  `register_end_time` datetime NOT NULL COMMENT '报名结束时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '竞赛创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '竞赛更新时间',
  PRIMARY KEY (`competition_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `competition`
--

LOCK TABLES `competition` WRITE;
/*!40000 ALTER TABLE `competition` DISABLE KEYS */;
INSERT INTO `competition` VALUES (1,'数学建模竞赛','本次竞赛主要考察参赛者的数学建模能力','创新创业','2026-03-01 00:00:00','2026-03-30 23:59:59','2026-01-27 20:52:45','2026-01-28 23:58:10'),(2,'蓝桥杯程序设计竞赛','分组科学,新手友好，适合大一学生参加的算法竞赛·','算法设计','2026-02-01 00:00:00','2026-03-15 23:59:59','2026-01-27 21:37:27','2026-02-01 11:10:17'),(3,'全国高校计算机能力挑战赛','分组科学,新手友好，支持个人或团队参赛','算法设计','2026-02-01 00:00:00','2026-03-15 23:59:59','2026-01-27 21:37:27','2026-01-28 23:58:10'),(4,'中国高校计算机大赛-团体程序设计天梯赛','10人团体赛，强调团队协作,证书保研优势显著','算法设计','2026-03-01 00:00:00','2026-04-15 23:59:59','2026-01-27 21:37:27','2026-01-28 23:58:10'),(5,'ACM-ICPC','算法\"奥林匹克\"，顶尖企业认可度高，3人组队','算法设计','2026-06-01 00:00:00','2026-08-31 23:59:59','2026-01-27 21:37:27','2026-01-28 23:58:10'),(6,'蓝桥杯-网络安全赛道','适合未来想要走网络安全方向的同学','软件开发','2026-02-01 00:00:00','2026-03-15 23:59:59','2026-01-27 21:37:27','2026-01-28 23:58:10'),(7,'全国大学生信息安全与对抗技术竞赛','适合网络安全方向，个人或团队参赛','软件开发','2026-02-01 00:00:00','2026-03-31 23:59:59','2026-01-27 21:37:27','2026-01-28 23:58:10'),(8,'睿抗机器人开发者大赛-大数据赛道','适合未来想要走大数据开发方向的同学','软件开发','2026-04-01 00:00:00','2026-06-30 23:59:59','2026-01-27 21:37:27','2026-01-28 23:58:10'),(9,'全球校园人工智能算法精英大赛','适合未来想要走人工智能算法方向的同学','算法设计','2026-03-01 00:00:00','2026-04-30 23:59:59','2026-01-27 21:37:27','2026-01-28 23:58:10'),(10,'科大讯飞AI开发者大赛(算法赛)','适合AI算法方向，个人或团队参赛，每年6-10月举办','算法设计','2026-04-01 00:00:00','2026-05-31 23:59:59','2026-01-27 21:37:27','2026-01-28 23:58:10'),(11,'华为ICT大赛','适合未来想要走网络、运维等方向的学生','软件开发','2026-08-01 00:00:00','2026-09-30 23:59:59','2026-01-27 21:37:27','2026-01-28 23:58:10'),(12,'\"中国软件杯\"大学生软件设计大赛','企业命题类赛事,项目经历适合写入简历中','软件开发','2026-01-01 00:00:00','2026-02-28 23:59:59','2026-01-27 21:37:27','2026-01-28 23:58:10'),(13,'中国大学生服务外包创新创业大赛','企业命题类赛事,项目经历适合写入简历中','创新创业','2026-09-01 00:00:00','2026-11-30 23:59:59','2026-01-27 21:37:27','2026-01-28 23:58:10'),(14,'全国大学生智能汽车竞赛','覆盖自动控制、传感技术、嵌入式开发全流程','软件开发','2026-02-01 00:00:00','2026-04-30 23:59:59','2026-01-27 21:37:27','2026-01-28 23:58:10'),(15,'中国国际大学生创新大赛(原互联网+)','竞赛含金量高，适合各年级学生参加','创新创业','2026-02-01 00:00:00','2026-04-30 23:59:59','2026-01-27 21:37:27','2026-01-28 23:58:10'),(16,'\"挑战杯\"全国大学生课外学术科技作品竞赛','竞赛含金量高，奇数年举办','创新创业','2026-01-01 00:00:00','2026-02-28 23:59:59','2026-01-27 21:37:27','2026-01-28 23:58:10');
/*!40000 ALTER TABLE `competition` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `competition_participation`
--

DROP TABLE IF EXISTS `competition_participation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `competition_participation` (
  `participation_id` bigint NOT NULL COMMENT '参赛ID（等于报名ID）',
  `ppt_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'PPT材料路径',
  `pdf_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'PDF材料路径',
  `submit_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `final_score` decimal(5,2) DEFAULT NULL COMMENT '最终得分',
  `participation_status` enum('未提交','已提交','已评分') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '未提交',
  PRIMARY KEY (`participation_id`) USING BTREE,
  CONSTRAINT `competition_participation_ibfk_1` FOREIGN KEY (`participation_id`) REFERENCES `competition_register` (`register_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `competition_participation`
--

LOCK TABLES `competition_participation` WRITE;
/*!40000 ALTER TABLE `competition_participation` DISABLE KEYS */;
INSERT INTO `competition_participation` VALUES (1,'path_to_ppt.ppt','path_to_pdf.pdf','2026-01-27 20:52:45',NULL,'未提交'),(2,NULL,NULL,NULL,NULL,'未提交'),(6,'/profile/upload/2026/01/29/测试_20260129185610A002.pptx','/profile/upload/2026/01/29/P020200117405680996848_20260129184826A006.pdf','2026-01-29 18:56:15',NULL,'已提交'),(7,'/profile/upload/2026/01/29/测试_20260129185758A003.pptx','/profile/upload/2026/01/29/P020200117405680996848_20260129185803A004.pdf','2026-01-29 18:58:04',NULL,'已提交'),(8,'/profile/upload/2026/01/29/测试_20260129190743A001.pptx','/profile/upload/2026/01/29/P020200117405680996848_20260129190750A002.pdf','2026-01-29 19:07:51',NULL,'已提交'),(9,NULL,NULL,NULL,NULL,'未提交'),(10,'/profile/upload/2026/02/01/测试_20260201112501A001.pptx','/profile/upload/2026/02/01/P020200117405680996848_20260201112511A002.pdf','2026-02-01 11:25:13',NULL,'已提交'),(11,'/profile/upload/2026/02/01/测试_20260201113804A003.pptx','/profile/upload/2026/02/01/P020200117405680996848_20260201113808A004.pdf','2026-02-01 11:38:10',NULL,'已提交'),(12,'/profile/upload/2026/02/01/测试_20260201115202A001.pptx','/profile/upload/2026/02/01/P020200117405680996848_20260201115209A002.pdf','2026-02-01 11:52:10',NULL,'已提交'),(13,NULL,NULL,NULL,NULL,'未提交'),(14,NULL,NULL,NULL,NULL,'未提交');
/*!40000 ALTER TABLE `competition_participation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `competition_permissions`
--

DROP TABLE IF EXISTS `competition_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `competition_permissions` (
  `user_id` bigint NOT NULL COMMENT '用户ID，外键',
  `competition_id` bigint NOT NULL COMMENT '竞赛ID，外键',
  `permission_id` tinyint NOT NULL COMMENT '权限标识，0表示无评分权限，1表示有评分权限',
  PRIMARY KEY (`user_id`,`competition_id`) USING BTREE COMMENT '联合主键：用户ID和竞赛ID',
  KEY `competition_id` (`competition_id`) USING BTREE,
  CONSTRAINT `competition_permissions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `competition_permissions_ibfk_2` FOREIGN KEY (`competition_id`) REFERENCES `competition` (`competition_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `competition_permissions_chk_1` CHECK ((`permission_id` in (0,1)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `competition_permissions`
--

LOCK TABLES `competition_permissions` WRITE;
/*!40000 ALTER TABLE `competition_permissions` DISABLE KEYS */;
INSERT INTO `competition_permissions` VALUES (104,3,1),(110,2,1),(110,7,1),(111,2,1),(111,3,1),(111,7,1);
/*!40000 ALTER TABLE `competition_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `competition_register`
--

DROP TABLE IF EXISTS `competition_register`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `competition_register` (
  `register_id` bigint NOT NULL AUTO_INCREMENT COMMENT '报名ID，主键',
  `competition_id` bigint DEFAULT NULL COMMENT '关联竞赛ID，外键',
  `team_id` bigint DEFAULT NULL COMMENT '关联队伍ID，外键',
  `team_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '队伍名称',
  `team_members` json DEFAULT NULL COMMENT '队伍成员信息',
  `work_name` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '作品名称',
  `teacher_name` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '指导老师',
  `work_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '作品简介',
  `user_id` bigint DEFAULT NULL COMMENT '报名人用户ID（sys_user表）',
  `register_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  PRIMARY KEY (`register_id`) USING BTREE,
  UNIQUE KEY `competition_team_user` (`competition_id`,`team_id`,`user_id`) COMMENT '联合唯一约束：同一用户在同一竞赛的同一队伍中只能保留一条报名记录',
  KEY `team_id` (`team_id`) USING BTREE,
  KEY `user_id` (`user_id`),
  CONSTRAINT `competition_register_ibfk_1` FOREIGN KEY (`competition_id`) REFERENCES `competition` (`competition_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `competition_register_ibfk_2` FOREIGN KEY (`team_id`) REFERENCES `team` (`team_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `competition_register`
--

LOCK TABLES `competition_register` WRITE;
/*!40000 ALTER TABLE `competition_register` DISABLE KEYS */;
INSERT INTO `competition_register`
(`register_id`,`competition_id`,`team_id`,`team_name`,`team_members`,`work_name`,`teacher_name`,`work_description`,`user_id`,`register_time`) VALUES
(1,1,1,'队伍A','[{\"name\": \"张三\", \"studentNo\": \"202300000001\", \"college\": \"计算机学院\", \"major\": \"计算机科学与技术\", \"phone\": \"13800000001\", \"email\": \"zhangsan@example.com\", \"role\": \"leader\", \"status\": \"approved\"}, {\"name\": \"李四\", \"studentNo\": \"202300000002\", \"college\": \"计算机学院\", \"major\": \"软件工程\", \"phone\": \"13800000002\", \"email\": \"lisi@example.com\", \"role\": \"member\", \"status\": \"approved\"}]','智慧校园助手','王老师','面向校园场景的多端智能协同助手，用于课程通知、任务管理和知识问答。',1,'2026-01-27 20:52:45'),
(2,12,2,'team1','[{\"name\": \"用户14047339\", \"studentNo\": \"202300000003\", \"college\": \"经济学院\", \"major\": \"金融学\", \"phone\": \"13800000003\", \"email\": \"user14047339@example.com\", \"role\": \"leader\", \"status\": \"approved\"}, {\"name\": \"李四\", \"studentNo\": \"202300000004\", \"college\": \"经济学院\", \"major\": \"经济学\", \"phone\": \"13800000004\", \"email\": \"lisi2@example.com\", \"role\": \"member\", \"status\": \"approved\"}]','商赛决策沙盘','赵老师','基于数据看板与情景推演的商业决策训练平台，帮助团队快速验证经营策略。',108,'2026-01-29 08:58:42'),
(6,12,2,'team1','[{\"name\": \"用户14047339\", \"studentNo\": \"202300000003\", \"college\": \"经济学院\", \"major\": \"金融学\", \"phone\": \"13800000003\", \"email\": \"user14047339@example.com\", \"role\": \"leader\", \"status\": \"approved\"}, {\"name\": \"李四\", \"studentNo\": \"202300000004\", \"college\": \"经济学院\", \"major\": \"经济学\", \"phone\": \"13800000004\", \"email\": \"lisi2@example.com\", \"role\": \"member\", \"status\": \"approved\"}]','商赛决策沙盘','赵老师','基于数据看板与情景推演的商业决策训练平台，帮助团队快速验证经营策略。',109,'2026-01-29 14:31:54'),
(7,16,4,'第一队','[{\"name\": \"黄飞鸿\", \"studentNo\": \"202300000005\", \"college\": \"计算机学院\", \"major\": \"计算机科学与技术\", \"phone\": \"13800000005\", \"email\": \"huangfh@example.com\", \"role\": \"leader\", \"status\": \"approved\"}, {\"name\": \"李世民\", \"studentNo\": \"202300000006\", \"college\": \"外国语学院\", \"major\": \"英语\", \"phone\": \"13800000006\", \"email\": \"lishimin@example.com\", \"role\": \"member\", \"status\": \"approved\"}]','跨学科智能翻译箱','张老师','融合本地语音识别与行业术语库的便携式翻译方案，服务多语种竞赛场景。',109,'2026-01-29 18:57:27'),
(8,16,4,'第一队','[{\"name\": \"黄飞鸿\", \"studentNo\": \"202300000005\", \"college\": \"计算机学院\", \"major\": \"计算机科学与技术\", \"phone\": \"13800000005\", \"email\": \"huangfh@example.com\", \"role\": \"leader\", \"status\": \"approved\"}, {\"name\": \"李世民\", \"studentNo\": \"202300000006\", \"college\": \"外国语学院\", \"major\": \"英语\", \"phone\": \"13800000006\", \"email\": \"lishimin@example.com\", \"role\": \"member\", \"status\": \"approved\"}]','跨学科智能翻译箱','张老师','融合本地语音识别与行业术语库的便携式翻译方案，服务多语种竞赛场景。',108,'2026-01-29 19:07:13'),
(9,2,5,'测试队伍1','[{\"name\": \"张三\", \"studentNo\": \"202300000007\", \"college\": \"软件学院\", \"major\": \"软件工程\", \"phone\": \"13800000007\", \"email\": \"zhangsan2@example.com\", \"role\": \"leader\", \"status\": \"approved\"}, {\"name\": \"郭靖\", \"studentNo\": \"202300000008\", \"college\": \"数学学院\", \"major\": \"数学与应用数学\", \"phone\": \"13800000008\", \"email\": \"guojing@example.com\", \"role\": \"member\", \"status\": \"approved\"}]','AI助教答疑系统','李老师','围绕课堂问答、作业批改与知识点追踪设计的 AI 助教产品。',108,'2026-02-01 10:53:45'),
(10,2,6,'一定赢队伍','[{\"name\": \"张三三\", \"studentNo\": \"202300000009\", \"college\": \"物理学院\", \"major\": \"应用物理学\", \"phone\": \"13800000009\", \"email\": \"zhangsansan@example.com\", \"role\": \"leader\", \"status\": \"approved\"}, {\"name\": \"韩梅梅\", \"studentNo\": \"202300000010\", \"college\": \"物理学院\", \"major\": \"电子信息工程\", \"phone\": \"13800000010\", \"email\": \"hanmeimei@example.com\", \"role\": \"member\", \"status\": \"approved\"}]','星图实验助手','高手','面向实验教学场景的智能辅助系统，可帮助学生完成实验数据记录、结果分析和过程提醒。',109,'2026-02-01 11:23:15'),
(11,3,7,'队伍889','[{\"name\": \"张三\", \"studentNo\": \"202300000011\", \"college\": \"物理学院\", \"major\": \"物理学\", \"phone\": \"13800000011\", \"email\": \"zhangsan3@example.com\", \"role\": \"leader\", \"status\": \"approved\"}, {\"name\": \"教师示例队员\", \"studentNo\": \"202300000012\", \"college\": \"物理学院\", \"major\": \"新能源材料\", \"phone\": \"13800000012\", \"email\": \"sample@example.com\", \"role\": \"member\", \"status\": \"approved\"}]','光谱智检平台','教师001','基于传感器数据分析的轻量化检测平台，用于提升实验样本识别与结果判断效率。',109,'2026-02-01 11:37:56'),
(12,6,8,'队伍7788','[{\"name\": \"王琪琪\", \"studentNo\": \"202300000013\", \"college\": \"海洋学院\", \"major\": \"海洋科学\", \"phone\": \"13800000013\", \"email\": \"wangqiqi@example.com\", \"role\": \"leader\", \"status\": \"approved\"}, {\"name\": \"周周\", \"studentNo\": \"202300000014\", \"college\": \"海洋学院\", \"major\": \"海洋技术\", \"phone\": \"13800000014\", \"email\": \"zhouzhou@example.com\", \"role\": \"member\", \"status\": \"approved\"}]','海洋观测轻终端','教师001','面向近岸环境监测的低功耗采集终端，支持多类型传感器数据汇聚。',109,'2026-02-01 11:51:42'),
(13,6,9,'d队伍1222','[{\"name\": \"张三三\", \"studentNo\": \"202300000015\", \"college\": \"计算机学院\", \"major\": \"网络工程\", \"phone\": \"13800000015\", \"email\": \"network@example.com\", \"role\": \"leader\", \"status\": \"approved\"}, {\"name\": \"林林\", \"studentNo\": \"202300000016\", \"college\": \"计算机学院\", \"major\": \"信息安全\", \"phone\": \"13800000016\", \"email\": \"linlin@example.com\", \"role\": \"member\", \"status\": \"approved\"}]','校园安全巡检平台','教师002','结合移动端拍照识别与工单分发的隐患巡检平台。',108,'2026-02-01 12:11:11'),
(14,7,10,'测试11','[{\"name\": \"王11\", \"studentNo\": \"202300000017\", \"college\": \"生命科学学院\", \"major\": \"生物工程\", \"phone\": \"13800000017\", \"email\": \"wang11@example.com\", \"role\": \"leader\", \"status\": \"approved\"}, {\"name\": \"陈晨\", \"studentNo\": \"202300000018\", \"college\": \"生命科学学院\", \"major\": \"生物技术\", \"phone\": \"13800000018\", \"email\": \"chenchen@example.com\", \"role\": \"member\", \"status\": \"approved\"}]','实验室预约排程系统','测试1','支持样品管理、仪器预约与风险预警的一体化实验室排程系统。',108,'2026-02-01 12:16:58');
/*!40000 ALTER TABLE `competition_register` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `competition_score`
--

DROP TABLE IF EXISTS `competition_score`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `competition_score` (
  `score_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评分ID，主键',
  `participation_id` bigint DEFAULT NULL COMMENT '参赛ID，外键',
  `reviewer_id` bigint DEFAULT NULL COMMENT '评审人ID，外键',
  `reviewer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '评审人姓名',
  `score` decimal(5,2) DEFAULT NULL COMMENT '得分',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '评语',
  `score_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评分时间',
  PRIMARY KEY (`score_id`) USING BTREE,
  KEY `participation_id` (`participation_id`) USING BTREE,
  KEY `reviewer_id` (`reviewer_id`) USING BTREE,
  CONSTRAINT `competition_score_ibfk_1` FOREIGN KEY (`participation_id`) REFERENCES `competition_participation` (`participation_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `competition_score_ibfk_2` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `competition_score`
--

LOCK TABLES `competition_score` WRITE;
/*!40000 ALTER TABLE `competition_score` DISABLE KEYS */;
INSERT INTO `competition_score` VALUES (1,1,2,'若依',95.00,'非常好的作品！','2026-01-27 20:52:45'),(2,9,110,'教师001',97.00,'可以','2026-02-01 11:18:27'),(3,10,110,'教师001',93.00,'一般','2026-02-01 11:33:46'),(4,9,111,'教师002',93.00,'尅','2026-02-01 12:08:21'),(5,10,111,'教师002',88.00,'不行11','2026-02-01 12:08:40'),(6,11,111,'教师002',99.00,'很棒','2026-02-01 12:04:52'),(7,14,111,'教师002',76.00,'一般般','2026-02-01 12:19:02');
/*!40000 ALTER TABLE `competition_score` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gen_table`
--

DROP TABLE IF EXISTS `gen_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gen_table` (
  `table_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `tpl_web_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='代码生成业务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gen_table`
--

LOCK TABLES `gen_table` WRITE;
/*!40000 ALTER TABLE `gen_table` DISABLE KEYS */;
/*!40000 ALTER TABLE `gen_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gen_table_column`
--

DROP TABLE IF EXISTS `gen_table_column`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gen_table_column` (
  `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典类型',
  `sort` int DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='代码生成业务表字段';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gen_table_column`
--

LOCK TABLES `gen_table_column` WRITE;
/*!40000 ALTER TABLE `gen_table_column` DISABLE KEYS */;
/*!40000 ALTER TABLE `gen_table_column` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_config`
--

DROP TABLE IF EXISTS `sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='参数配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_config`
--

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
INSERT INTO `sys_config` VALUES (1,'主框架页-默认皮肤样式名称','sys.index.skinName','skin-blue','Y','admin','2026-01-15 06:28:54','',NULL,'蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow'),(2,'用户管理-账号初始密码','sys.user.initPassword','123456','Y','admin','2026-01-15 06:28:54','',NULL,'初始化密码 123456'),(3,'主框架页-侧边栏主题','sys.index.sideTheme','theme-dark','Y','admin','2026-01-15 06:28:54','',NULL,'深色主题theme-dark，浅色主题theme-light'),(4,'账号自助-验证码开关','sys.account.captchaEnabled','true','Y','admin','2026-01-15 06:28:54','',NULL,'是否开启验证码功能（true开启，false关闭）'),(5,'账号自助-是否开启用户注册功能','sys.account.registerUser','true','Y','admin','2026-01-15 06:28:54','',NULL,'是否开启注册用户功能（true开启，false关闭）'),(6,'用户登录-黑名单列表','sys.login.blackIPList','','Y','admin','2026-01-15 06:28:54','',NULL,'设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）'),(7,'用户管理-初始密码修改策略','sys.account.initPasswordModify','1','Y','admin','2026-01-15 06:28:54','',NULL,'0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框'),(8,'用户管理-账号密码更新周期','sys.account.passwordValidateDays','0','Y','admin','2026-01-15 06:28:54','',NULL,'密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dept`
--

DROP TABLE IF EXISTS `sys_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dept` (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门id',
  `ancestors` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '部门名称',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='部门表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dept`
--

LOCK TABLES `sys_dept` WRITE;
/*!40000 ALTER TABLE `sys_dept` DISABLE KEYS */;
INSERT INTO `sys_dept` VALUES (100,0,'0','若依科技',0,'若依','15888888888','ry@qq.com','0','0','admin','2026-01-15 06:28:53','',NULL),(101,100,'0,100','深圳总公司',1,'若依','15888888888','ry@qq.com','0','0','admin','2026-01-15 06:28:53','',NULL),(102,100,'0,100','长沙分公司',2,'若依','15888888888','ry@qq.com','0','0','admin','2026-01-15 06:28:53','',NULL),(103,101,'0,100,101','研发部门',1,'若依','15888888888','ry@qq.com','0','0','admin','2026-01-15 06:28:53','',NULL),(104,101,'0,100,101','市场部门',2,'若依','15888888888','ry@qq.com','0','0','admin','2026-01-15 06:28:53','',NULL),(105,101,'0,100,101','测试部门',3,'若依','15888888888','ry@qq.com','0','0','admin','2026-01-15 06:28:53','',NULL),(106,101,'0,100,101','财务部门',4,'若依','15888888888','ry@qq.com','0','0','admin','2026-01-15 06:28:53','',NULL),(107,101,'0,100,101','运维部门',5,'若依','15888888888','ry@qq.com','0','0','admin','2026-01-15 06:28:53','',NULL),(108,102,'0,100,102','市场部门',1,'若依','15888888888','ry@qq.com','0','0','admin','2026-01-15 06:28:53','',NULL),(109,102,'0,100,102','财务部门',2,'若依','15888888888','ry@qq.com','0','0','admin','2026-01-15 06:28:53','',NULL);
/*!40000 ALTER TABLE `sys_dept` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dict_data`
--

DROP TABLE IF EXISTS `sys_dict_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_data` (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int DEFAULT '0' COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='字典数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict_data`
--

LOCK TABLES `sys_dict_data` WRITE;
/*!40000 ALTER TABLE `sys_dict_data` DISABLE KEYS */;
INSERT INTO `sys_dict_data` VALUES (1,1,'男','0','sys_user_sex','','','Y','0','admin','2026-01-15 06:28:54','',NULL,'性别男'),(2,2,'女','1','sys_user_sex','','','N','0','admin','2026-01-15 06:28:54','',NULL,'性别女'),(3,3,'未知','2','sys_user_sex','','','N','0','admin','2026-01-15 06:28:54','',NULL,'性别未知'),(4,1,'显示','0','sys_show_hide','','primary','Y','0','admin','2026-01-15 06:28:54','',NULL,'显示菜单'),(5,2,'隐藏','1','sys_show_hide','','danger','N','0','admin','2026-01-15 06:28:54','',NULL,'隐藏菜单'),(6,1,'正常','0','sys_normal_disable','','primary','Y','0','admin','2026-01-15 06:28:54','',NULL,'正常状态'),(7,2,'停用','1','sys_normal_disable','','danger','N','0','admin','2026-01-15 06:28:54','',NULL,'停用状态'),(8,1,'正常','0','sys_job_status','','primary','Y','0','admin','2026-01-15 06:28:54','',NULL,'正常状态'),(9,2,'暂停','1','sys_job_status','','danger','N','0','admin','2026-01-15 06:28:54','',NULL,'停用状态'),(10,1,'默认','DEFAULT','sys_job_group','','','Y','0','admin','2026-01-15 06:28:54','',NULL,'默认分组'),(11,2,'系统','SYSTEM','sys_job_group','','','N','0','admin','2026-01-15 06:28:54','',NULL,'系统分组'),(12,1,'是','Y','sys_yes_no','','primary','Y','0','admin','2026-01-15 06:28:54','',NULL,'系统默认是'),(13,2,'否','N','sys_yes_no','','danger','N','0','admin','2026-01-15 06:28:54','',NULL,'系统默认否'),(14,1,'通知','1','sys_notice_type','','warning','Y','0','admin','2026-01-15 06:28:54','',NULL,'通知'),(15,2,'公告','2','sys_notice_type','','success','N','0','admin','2026-01-15 06:28:54','',NULL,'公告'),(16,1,'正常','0','sys_notice_status','','primary','Y','0','admin','2026-01-15 06:28:54','',NULL,'正常状态'),(17,2,'关闭','1','sys_notice_status','','danger','N','0','admin','2026-01-15 06:28:54','',NULL,'关闭状态'),(18,99,'其他','0','sys_oper_type','','info','N','0','admin','2026-01-15 06:28:54','',NULL,'其他操作'),(19,1,'新增','1','sys_oper_type','','info','N','0','admin','2026-01-15 06:28:54','',NULL,'新增操作'),(20,2,'修改','2','sys_oper_type','','info','N','0','admin','2026-01-15 06:28:54','',NULL,'修改操作'),(21,3,'删除','3','sys_oper_type','','danger','N','0','admin','2026-01-15 06:28:54','',NULL,'删除操作'),(22,4,'授权','4','sys_oper_type','','primary','N','0','admin','2026-01-15 06:28:54','',NULL,'授权操作'),(23,5,'导出','5','sys_oper_type','','warning','N','0','admin','2026-01-15 06:28:54','',NULL,'导出操作'),(24,6,'导入','6','sys_oper_type','','warning','N','0','admin','2026-01-15 06:28:54','',NULL,'导入操作'),(25,7,'强退','7','sys_oper_type','','danger','N','0','admin','2026-01-15 06:28:54','',NULL,'强退操作'),(26,8,'生成代码','8','sys_oper_type','','warning','N','0','admin','2026-01-15 06:28:54','',NULL,'生成操作'),(27,9,'清空数据','9','sys_oper_type','','danger','N','0','admin','2026-01-15 06:28:54','',NULL,'清空操作'),(28,1,'成功','0','sys_common_status','','primary','N','0','admin','2026-01-15 06:28:54','',NULL,'正常状态'),(29,2,'失败','1','sys_common_status','','danger','N','0','admin','2026-01-15 06:28:54','',NULL,'停用状态');
/*!40000 ALTER TABLE `sys_dict_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dict_type`
--

DROP TABLE IF EXISTS `sys_dict_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_type` (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE KEY `dict_type` (`dict_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='字典类型表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict_type`
--

LOCK TABLES `sys_dict_type` WRITE;
/*!40000 ALTER TABLE `sys_dict_type` DISABLE KEYS */;
INSERT INTO `sys_dict_type` VALUES (1,'用户性别','sys_user_sex','0','admin','2026-01-15 06:28:53','',NULL,'用户性别列表'),(2,'菜单状态','sys_show_hide','0','admin','2026-01-15 06:28:53','',NULL,'菜单状态列表'),(3,'系统开关','sys_normal_disable','0','admin','2026-01-15 06:28:53','',NULL,'系统开关列表'),(4,'任务状态','sys_job_status','0','admin','2026-01-15 06:28:53','',NULL,'任务状态列表'),(5,'任务分组','sys_job_group','0','admin','2026-01-15 06:28:53','',NULL,'任务分组列表'),(6,'系统是否','sys_yes_no','0','admin','2026-01-15 06:28:53','',NULL,'系统是否列表'),(7,'通知类型','sys_notice_type','0','admin','2026-01-15 06:28:53','',NULL,'通知类型列表'),(8,'通知状态','sys_notice_status','0','admin','2026-01-15 06:28:53','',NULL,'通知状态列表'),(9,'操作类型','sys_oper_type','0','admin','2026-01-15 06:28:53','',NULL,'操作类型列表'),(10,'系统状态','sys_common_status','0','admin','2026-01-15 06:28:54','',NULL,'登录状态列表');
/*!40000 ALTER TABLE `sys_dict_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job`
--

DROP TABLE IF EXISTS `sys_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job` (
  `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`,`job_name`,`job_group`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='定时任务调度表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job`
--

LOCK TABLES `sys_job` WRITE;
/*!40000 ALTER TABLE `sys_job` DISABLE KEYS */;
INSERT INTO `sys_job` VALUES (1,'系统默认（无参）','DEFAULT','ryTask.ryNoParams','0/10 * * * * ?','3','1','1','admin','2026-01-15 06:28:54','',NULL,''),(2,'系统默认（有参）','DEFAULT','ryTask.ryParams(\'ry\')','0/15 * * * * ?','3','1','1','admin','2026-01-15 06:28:54','',NULL,''),(3,'系统默认（多参）','DEFAULT','ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)','0/20 * * * * ?','3','1','1','admin','2026-01-15 06:28:54','',NULL,'');
/*!40000 ALTER TABLE `sys_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job_log`
--

DROP TABLE IF EXISTS `sys_job_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_log` (
  `job_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '日志信息',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '异常信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='定时任务调度日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job_log`
--

LOCK TABLES `sys_job_log` WRITE;
/*!40000 ALTER TABLE `sys_job_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_job_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_logininfor`
--

DROP TABLE IF EXISTS `sys_logininfor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_logininfor` (
  `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '操作系统',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '提示消息',
  `login_time` datetime DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`) USING BTREE,
  KEY `idx_sys_logininfor_s` (`status`) USING BTREE,
  KEY `idx_sys_logininfor_lt` (`login_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=436 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='系统访问记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_logininfor`
--

LOCK TABLES `sys_logininfor` WRITE;
/*!40000 ALTER TABLE `sys_logininfor` DISABLE KEYS */;
INSERT INTO `sys_logininfor` VALUES (100,'admin','127.0.0.1','内网IP','Chrome 143','Windows10','0','登录成功','2026-01-15 06:35:09'),(101,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-15 09:05:13'),(102,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-15 09:44:21'),(103,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-15 09:45:33'),(104,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-15 09:45:37'),(105,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-15 10:02:55'),(106,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-16 07:18:07'),(107,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-16 07:20:48'),(108,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-16 07:21:05'),(109,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-16 07:41:03'),(110,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-16 07:42:45'),(111,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-16 07:45:00'),(112,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-16 07:45:17'),(113,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-16 07:46:07'),(114,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-16 07:46:35'),(115,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-16 08:14:47'),(116,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-16 08:17:23'),(117,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-16 08:18:11'),(118,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-16 08:24:48'),(119,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-16 08:45:33'),(120,'mengzhiqi666','127.0.0.1','内网IP','Edge 143','Windows >=10','0','注册成功','2026-01-16 08:48:31'),(121,'aaa','127.0.0.1','内网IP','Edge 143','Windows >=10','0','注册成功','2026-01-16 08:48:34'),(122,'mengzhiqi666','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-16 08:49:55'),(123,'mengzhiqi666','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-16 08:50:34'),(124,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-16 08:50:53'),(125,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-16 08:54:22'),(126,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-16 09:12:02'),(127,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-16 09:12:51'),(128,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','1','验证码已失效','2026-01-16 09:27:50'),(129,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-16 09:27:54'),(130,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','1','验证码错误','2026-01-17 07:48:16'),(131,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','1','验证码错误','2026-01-17 07:48:17'),(132,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-17 07:48:21'),(133,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 15:12:43'),(134,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 15:30:10'),(135,'mzq666','127.0.0.1','内网IP','Edge 143','Windows >=10','1','用户不存在/密码错误','2026-01-21 15:30:18'),(136,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','1','用户不存在/密码错误','2026-01-21 15:30:29'),(137,'mzq123','127.0.0.1','内网IP','Edge 143','Windows >=10','1','用户不存在/密码错误','2026-01-21 15:30:37'),(138,'mzq66','127.0.0.1','内网IP','Edge 143','Windows >=10','1','用户不存在/密码错误','2026-01-21 15:30:52'),(139,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 15:31:02'),(140,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 15:31:29'),(141,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','注册成功','2026-01-21 15:31:45'),(142,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 15:32:01'),(143,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 15:32:15'),(144,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 15:32:25'),(145,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 15:32:57'),(146,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','1','验证码错误','2026-01-21 15:33:06'),(147,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 15:33:11'),(148,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 15:33:34'),(149,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 15:33:44'),(150,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 15:36:40'),(151,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','注册成功','2026-01-21 15:37:01'),(152,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 15:45:09'),(153,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 15:45:16'),(154,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 15:45:25'),(155,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 15:45:32'),(156,'sys admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','注册成功','2026-01-21 15:50:26'),(157,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 15:51:37'),(158,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 15:51:46'),(159,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 15:52:12'),(160,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:04:23'),(161,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:04:32'),(162,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:15:21'),(163,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','1','验证码错误','2026-01-21 16:15:25'),(164,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:15:30'),(165,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:16:38'),(166,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:16:42'),(167,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:21:23'),(168,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:21:33'),(169,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:22:02'),(170,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:22:14'),(171,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:22:40'),(172,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:22:50'),(173,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:23:16'),(174,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','1','用户不存在/密码错误','2026-01-21 16:23:23'),(175,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:23:32'),(176,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:24:24'),(177,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:24:29'),(178,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:25:11'),(179,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:25:15'),(180,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:25:27'),(181,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:25:32'),(182,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:38:30'),(183,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:38:38'),(184,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:39:16'),(185,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:39:24'),(186,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:40:01'),(187,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:40:10'),(188,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:40:17'),(189,'sys admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:40:30'),(190,'sys admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:40:39'),(191,'sys admin','127.0.0.1','内网IP','Edge 143','Windows >=10','1','验证码已失效','2026-01-21 16:45:42'),(192,'sys admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:45:46'),(193,'sys admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:46:34'),(194,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:47:21'),(195,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:48:09'),(196,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:48:20'),(197,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:49:14'),(198,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:49:19'),(199,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:49:51'),(200,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:49:55'),(201,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:50:51'),(202,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:51:09'),(203,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:52:42'),(204,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:52:49'),(205,'mzq','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:53:50'),(206,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 16:54:09'),(207,'teacher','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 16:55:04'),(208,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','登录成功','2026-01-21 17:03:19'),(209,'admin','127.0.0.1','内网IP','Edge 143','Windows >=10','0','退出成功','2026-01-21 17:03:36'),(210,'student','127.0.0.1','内网IP','Edge 143','Windows >=10','1','用户不存在/密码错误','2026-01-21 17:04:18'),(211,'student','127.0.0.1','内网IP','Edge 143','Windows >=10','0','注册成功','2026-01-21 17:04:49'),(212,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','1','验证码错误','2026-01-24 10:41:16'),(213,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','1','验证码错误','2026-01-24 10:41:16'),(214,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-24 10:41:17'),(215,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-24 10:41:25'),(216,'mzq','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-24 10:41:32'),(217,'mzq','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-24 10:42:01'),(218,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-24 10:42:09'),(219,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-24 10:42:16'),(220,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','1','用户不存在/密码错误','2026-01-24 10:42:22'),(221,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-24 10:42:30'),(222,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-25 17:39:33'),(223,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-25 17:40:14'),(224,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-25 17:40:29'),(225,'admin','127.0.0.1','内网IP','Edge 128','Windows >=10','0','登录成功','2026-01-25 17:53:42'),(226,'admin','127.0.0.1','内网IP','Edge 128','Windows >=10','0','退出成功','2026-01-25 18:06:22'),(227,'admin','127.0.0.1','内网IP','Edge 128','Windows >=10','0','登录成功','2026-01-25 18:43:12'),(228,'admin','127.0.0.1','内网IP','Edge 128','Windows >=10','0','退出成功','2026-01-25 18:43:18'),(229,'admin','127.0.0.1','内网IP','Edge 128','Windows >=10','0','登录成功','2026-01-25 18:43:27'),(230,'admin','127.0.0.1','内网IP','Edge 128','Windows >=10','0','退出成功','2026-01-25 18:47:19'),(231,'admin','127.0.0.1','内网IP','Edge 128','Windows >=10','0','登录成功','2026-01-25 18:47:30'),(232,'admin','127.0.0.1','内网IP','Edge 128','Windows >=10','0','退出成功','2026-01-25 18:48:20'),(233,'admin','127.0.0.1','内网IP','Edge 128','Windows >=10','0','登录成功','2026-01-25 18:48:24'),(234,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-25 19:02:54'),(235,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 02:53:59'),(236,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 02:59:25'),(237,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 03:38:02'),(238,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 03:38:24'),(239,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 05:25:37'),(240,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 05:25:48'),(241,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 05:25:57'),(242,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 05:27:32'),(243,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','1','用户不存在/密码错误','2026-01-26 05:27:36'),(244,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','1','用户不存在/密码错误','2026-01-26 05:27:40'),(245,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 05:27:46'),(246,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 05:33:04'),(247,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 05:33:12'),(248,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 05:38:41'),(249,'sys admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 05:38:52'),(250,'sys admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 05:49:55'),(251,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 05:50:09'),(252,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 05:50:36'),(253,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 05:50:45'),(254,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 05:51:54'),(255,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 05:51:59'),(256,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 05:52:14'),(257,'sys admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 05:52:22'),(258,'sys admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 05:52:46'),(259,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 05:52:52'),(260,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 05:55:41'),(261,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 05:55:44'),(262,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 05:59:03'),(263,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 05:59:07'),(264,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 05:59:23'),(265,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 05:59:29'),(266,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 05:59:32'),(267,'sys admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 05:59:46'),(268,'sys admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 05:59:48'),(269,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 05:59:58'),(270,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:00:52'),(271,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:01:00'),(272,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:01:05'),(273,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:02:18'),(274,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:02:50'),(275,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:02:54'),(276,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:03:10'),(277,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:03:17'),(278,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:03:36'),(279,'sys admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:03:48'),(280,'sys admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:03:50'),(281,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','1','验证码错误','2026-01-26 06:04:03'),(282,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:04:07'),(283,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:06:17'),(284,'sys admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:06:34'),(285,'sys admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:06:40'),(286,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:06:48'),(287,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:07:17'),(288,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','1','验证码错误','2026-01-26 06:07:36'),(289,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:07:40'),(290,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:07:43'),(291,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:08:57'),(292,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:09:01'),(293,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:09:31'),(294,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:09:48'),(295,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:09:53'),(296,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:13:57'),(297,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:14:07'),(298,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:15:23'),(299,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:15:43'),(300,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:15:49'),(301,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:15:57'),(302,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:16:03'),(303,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:16:10'),(304,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:16:12'),(305,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:16:28'),(306,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:16:45'),(307,'sys admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:16:55'),(308,'sys admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:20:36'),(309,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:20:38'),(310,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:20:42'),(311,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:25:33'),(312,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:25:36'),(313,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:26:05'),(314,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:26:12'),(315,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:26:18'),(316,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:26:20'),(317,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:26:27'),(318,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:26:58'),(319,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:27:07'),(320,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:27:11'),(321,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:27:16'),(322,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:28:19'),(323,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:28:26'),(324,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:28:39'),(325,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:28:56'),(326,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:29:05'),(327,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:29:11'),(328,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:31:34'),(329,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','1','验证码已失效','2026-01-26 06:34:00'),(330,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:34:03'),(331,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:34:07'),(332,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','1','验证码错误','2026-01-26 06:34:23'),(333,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:34:26'),(334,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:34:30'),(335,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','1','验证码已失效','2026-01-26 06:37:18'),(336,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:37:21'),(337,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:37:32'),(338,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','1','验证码错误','2026-01-26 06:38:51'),(339,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:38:55'),(340,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:41:40'),(341,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:41:51'),(342,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:41:55'),(343,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:42:00'),(344,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:42:42'),(345,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:42:49'),(346,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:42:59'),(347,'sys admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:43:08'),(348,'sys admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:43:17'),(349,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:43:28'),(350,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:43:57'),(351,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','1','用户不存在/密码错误','2026-01-26 06:44:06'),(352,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:44:13'),(353,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 06:50:58'),(354,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','1','验证码已失效','2026-01-26 06:54:21'),(355,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 06:54:26'),(356,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 07:15:15'),(357,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','1','验证码错误','2026-01-26 07:15:22'),(358,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 07:15:26'),(359,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 07:18:28'),(360,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 07:20:00'),(361,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 07:20:04'),(362,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 07:20:13'),(363,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 07:20:23'),(364,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 07:20:32'),(365,'teacher','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 07:20:41'),(366,'sys admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 07:20:51'),(367,'sys admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 07:20:59'),(368,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 07:40:01'),(369,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 12:23:23'),(370,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','退出成功','2026-01-26 12:23:31'),(371,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','1','用户不存在/密码错误','2026-01-26 12:23:46'),(372,'admin','127.0.0.1','内网IP','Edge 128','Windows >=10','0','登录成功','2026-01-26 12:40:12'),(373,'admin','127.0.0.1','内网IP','Edge 128','Windows >=10','0','退出成功','2026-01-26 12:40:37'),(374,'student','127.0.0.1','内网IP','Edge 128','Windows >=10','0','登录成功','2026-01-26 12:41:11'),(375,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 12:41:23'),(376,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-26 12:41:40'),(377,'student','127.0.0.1','内网IP','Edge 128','Windows >=10','0','退出成功','2026-01-26 13:01:15'),(378,'student','127.0.0.1','内网IP','Edge 128','Windows >=10','0','登录成功','2026-01-26 13:01:34'),(379,'student','127.0.0.1','内网IP','Chrome 143','Windows10','0','登录成功','2026-01-26 13:02:02'),(380,'student','127.0.0.1','内网IP','Edge 128','Windows >=10','0','退出成功','2026-01-26 13:02:12'),(381,'student','127.0.0.1','内网IP','Edge 128','Windows >=10','0','登录成功','2026-01-26 13:02:26'),(382,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-27 10:12:48'),(383,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','1','验证码错误','2026-01-27 10:12:51'),(384,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-27 10:13:19'),(385,'student','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-27 10:26:24'),(386,'admin','127.0.0.1','内网IP','Edge 144','Windows >=10','0','登录成功','2026-01-27 11:11:57'),(387,'admin','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-28 21:48:12'),(388,'admin','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','退出成功','2026-01-28 21:48:37'),(389,'test01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','注册成功','2026-01-28 21:48:51'),(390,'test01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-28 21:49:10'),(391,'test01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','退出成功','2026-01-28 22:18:31'),(392,'st01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','注册成功','2026-01-28 22:18:46'),(393,'st01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-28 22:19:06'),(394,'st01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-28 23:16:17'),(395,'st01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-29 00:04:40'),(396,'st01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-29 08:43:37'),(397,'st01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-29 08:54:42'),(398,'st01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-29 13:11:39'),(399,'st01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','退出成功','2026-01-29 13:20:13'),(400,'st02','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','注册成功','2026-01-29 13:20:26'),(401,'st02','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-29 13:20:41'),(402,'st02','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','退出成功','2026-01-29 13:44:05'),(403,'st01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','1','验证码错误','2026-01-29 13:44:10'),(404,'st01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-29 13:44:14'),(405,'st01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','退出成功','2026-01-29 13:59:12'),(406,'admin','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-29 13:59:21'),(407,'admin','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','退出成功','2026-01-29 13:59:28'),(408,'st01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-29 13:59:41'),(409,'st01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','退出成功','2026-01-29 14:14:17'),(410,'st02','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-29 14:14:21'),(411,'st02','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','1','验证码错误','2026-01-29 18:32:36'),(412,'st02','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','1','验证码错误','2026-01-29 18:32:40'),(413,'st02','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-29 18:32:45'),(414,'st02','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','退出成功','2026-01-29 19:05:18'),(415,'st01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-29 19:05:25'),(416,'st01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','退出成功','2026-01-29 19:08:27'),(417,'st01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-30 08:04:50'),(418,'admin','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-31 20:05:46'),(419,'admin','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-31 20:42:45'),(420,'tc_001','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-31 20:58:17'),(421,'admin','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-01-31 21:53:49'),(422,'t_01','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','1','用户不存在/密码错误','2026-02-01 10:01:02'),(423,'tc_001','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-02-01 10:01:24'),(424,'admin','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','1','验证码已失效','2026-02-01 10:42:45'),(425,'admin','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-02-01 10:42:47'),(426,'st01','127.0.0.1','内网IP','Safari 26.2','Mac OS >=10.15.7','0','登录成功','2026-02-01 10:52:45'),(427,'st01','127.0.0.1','内网IP','Safari 26.2','Mac OS >=10.15.7','0','退出成功','2026-02-01 11:22:00'),(428,'st02','127.0.0.1','内网IP','Safari 26.2','Mac OS >=10.15.7','0','登录成功','2026-02-01 11:22:10'),(429,'st02','127.0.0.1','内网IP','Safari 26.2','Mac OS >=10.15.7','0','退出成功','2026-02-01 12:00:11'),(430,'comp_admin','127.0.0.1','内网IP','Safari 26.2','Mac OS >=10.15.7','0','登录成功','2026-02-01 12:00:17'),(431,'tc_001','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','退出成功','2026-02-01 12:03:02'),(432,'tc_002','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','1','用户不存在/密码错误','2026-02-01 12:03:26'),(433,'tc_002','127.0.0.1','内网IP','Chrome 144','Mac OS >=10.15.7','0','登录成功','2026-02-01 12:03:41'),(434,'comp_admin','127.0.0.1','内网IP','Safari 26.2','Mac OS >=10.15.7','0','退出成功','2026-02-01 12:10:27'),(435,'st01','127.0.0.1','内网IP','Safari 26.2','Mac OS >=10.15.7','0','登录成功','2026-02-01 12:10:36');
/*!40000 ALTER TABLE `sys_logininfor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_menu`
--

DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu` (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '路由名称',
  `is_frame` int DEFAULT '1' COMMENT '是否为外链（0是 1否）',
  `is_cache` int DEFAULT '0' COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2120 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='菜单权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_menu`
--

LOCK TABLES `sys_menu` WRITE;
/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
INSERT INTO `sys_menu` VALUES (1,'系统管理',0,1,'system',NULL,'','',1,0,'M','0','0','','system','admin','2026-01-15 06:28:53','',NULL,'系统管理目录'),(2,'系统监控',0,2,'monitor',NULL,'','',1,0,'M','0','0','','monitor','admin','2026-01-15 06:28:53','',NULL,'系统监控目录'),(3,'系统工具',0,3,'tool',NULL,'','',1,0,'M','0','0','','tool','admin','2026-01-15 06:28:53','',NULL,'系统工具目录'),(100,'用户管理',1,1,'user','system/user/index','','',1,0,'C','0','0','system:user:list','user','admin','2026-01-15 06:28:53','',NULL,'用户管理菜单'),(101,'角色管理',1,2,'role','system/role/index','','',1,0,'C','0','0','system:role:list','peoples','admin','2026-01-15 06:28:53','',NULL,'角色管理菜单'),(102,'菜单管理',1,3,'menu','system/menu/index','','',1,0,'C','0','0','system:menu:list','tree-table','admin','2026-01-15 06:28:53','',NULL,'菜单管理菜单'),(103,'部门管理',1,4,'dept','system/dept/index','','',1,0,'C','0','0','system:dept:list','tree','admin','2026-01-15 06:28:53','',NULL,'部门管理菜单'),(104,'岗位管理',1,5,'post','system/post/index','','',1,0,'C','0','0','system:post:list','post','admin','2026-01-15 06:28:53','',NULL,'岗位管理菜单'),(105,'字典管理',1,6,'dict','system/dict/index','','',1,0,'C','0','0','system:dict:list','dict','admin','2026-01-15 06:28:53','',NULL,'字典管理菜单'),(106,'参数设置',1,7,'config','system/config/index','','',1,0,'C','0','0','system:config:list','edit','admin','2026-01-15 06:28:53','',NULL,'参数设置菜单'),(107,'通知公告',1,8,'notice','system/notice/index','','',1,0,'C','0','0','system:notice:list','message','admin','2026-01-15 06:28:53','',NULL,'通知公告菜单'),(108,'日志管理',1,9,'log','','','',1,0,'M','0','0','','log','admin','2026-01-15 06:28:53','',NULL,'日志管理菜单'),(109,'在线用户',2,1,'online','monitor/online/index','','',1,0,'C','0','0','monitor:online:list','online','admin','2026-01-15 06:28:53','',NULL,'在线用户菜单'),(110,'定时任务',2,2,'job','monitor/job/index','','',1,0,'C','0','0','monitor:job:list','job','admin','2026-01-15 06:28:53','',NULL,'定时任务菜单'),(111,'数据监控',2,3,'druid','monitor/druid/index','','',1,0,'C','0','0','monitor:druid:list','druid','admin','2026-01-15 06:28:53','',NULL,'数据监控菜单'),(112,'服务监控',2,4,'server','monitor/server/index','','',1,0,'C','0','0','monitor:server:list','server','admin','2026-01-15 06:28:53','',NULL,'服务监控菜单'),(113,'缓存监控',2,5,'cache','monitor/cache/index','','',1,0,'C','0','0','monitor:cache:list','redis','admin','2026-01-15 06:28:53','',NULL,'缓存监控菜单'),(114,'缓存列表',2,6,'cacheList','monitor/cache/list','','',1,0,'C','0','0','monitor:cache:list','redis-list','admin','2026-01-15 06:28:53','',NULL,'缓存列表菜单'),(115,'表单构建',3,1,'build','tool/build/index','','',1,0,'C','0','0','tool:build:list','build','admin','2026-01-15 06:28:53','',NULL,'表单构建菜单'),(116,'代码生成',3,2,'gen','tool/gen/index','','',1,0,'C','0','0','tool:gen:list','code','admin','2026-01-15 06:28:53','',NULL,'代码生成菜单'),(117,'系统接口',3,3,'swagger','tool/swagger/index','','',1,0,'C','0','0','tool:swagger:list','swagger','admin','2026-01-15 06:28:53','',NULL,'系统接口菜单'),(500,'操作日志',108,1,'operlog','monitor/operlog/index','','',1,0,'C','0','0','monitor:operlog:list','form','admin','2026-01-15 06:28:53','',NULL,'操作日志菜单'),(501,'登录日志',108,2,'logininfor','monitor/logininfor/index','','',1,0,'C','0','0','monitor:logininfor:list','logininfor','admin','2026-01-15 06:28:53','',NULL,'登录日志菜单'),(1000,'用户查询',100,1,'','','','',1,0,'F','0','0','system:user:query','#','admin','2026-01-15 06:28:53','',NULL,''),(1001,'用户新增',100,2,'','','','',1,0,'F','0','0','system:user:add','#','admin','2026-01-15 06:28:53','',NULL,''),(1002,'用户修改',100,3,'','','','',1,0,'F','0','0','system:user:edit','#','admin','2026-01-15 06:28:53','',NULL,''),(1003,'用户删除',100,4,'','','','',1,0,'F','0','0','system:user:remove','#','admin','2026-01-15 06:28:53','',NULL,''),(1004,'用户导出',100,5,'','','','',1,0,'F','0','0','system:user:export','#','admin','2026-01-15 06:28:53','',NULL,''),(1005,'用户导入',100,6,'','','','',1,0,'F','0','0','system:user:import','#','admin','2026-01-15 06:28:53','',NULL,''),(1006,'重置密码',100,7,'','','','',1,0,'F','0','0','system:user:resetPwd','#','admin','2026-01-15 06:28:53','',NULL,''),(1007,'角色查询',101,1,'','','','',1,0,'F','0','0','system:role:query','#','admin','2026-01-15 06:28:53','',NULL,''),(1008,'角色新增',101,2,'','','','',1,0,'F','0','0','system:role:add','#','admin','2026-01-15 06:28:53','',NULL,''),(1009,'角色修改',101,3,'','','','',1,0,'F','0','0','system:role:edit','#','admin','2026-01-15 06:28:53','',NULL,''),(1010,'角色删除',101,4,'','','','',1,0,'F','0','0','system:role:remove','#','admin','2026-01-15 06:28:53','',NULL,''),(1011,'角色导出',101,5,'','','','',1,0,'F','0','0','system:role:export','#','admin','2026-01-15 06:28:53','',NULL,''),(1012,'菜单查询',102,1,'','','','',1,0,'F','0','0','system:menu:query','#','admin','2026-01-15 06:28:53','',NULL,''),(1013,'菜单新增',102,2,'','','','',1,0,'F','0','0','system:menu:add','#','admin','2026-01-15 06:28:53','',NULL,''),(1014,'菜单修改',102,3,'','','','',1,0,'F','0','0','system:menu:edit','#','admin','2026-01-15 06:28:53','',NULL,''),(1015,'菜单删除',102,4,'','','','',1,0,'F','0','0','system:menu:remove','#','admin','2026-01-15 06:28:53','',NULL,''),(1016,'部门查询',103,1,'','','','',1,0,'F','0','0','system:dept:query','#','admin','2026-01-15 06:28:53','',NULL,''),(1017,'部门新增',103,2,'','','','',1,0,'F','0','0','system:dept:add','#','admin','2026-01-15 06:28:53','',NULL,''),(1018,'部门修改',103,3,'','','','',1,0,'F','0','0','system:dept:edit','#','admin','2026-01-15 06:28:53','',NULL,''),(1019,'部门删除',103,4,'','','','',1,0,'F','0','0','system:dept:remove','#','admin','2026-01-15 06:28:53','',NULL,''),(1020,'岗位查询',104,1,'','','','',1,0,'F','0','0','system:post:query','#','admin','2026-01-15 06:28:53','',NULL,''),(1021,'岗位新增',104,2,'','','','',1,0,'F','0','0','system:post:add','#','admin','2026-01-15 06:28:53','',NULL,''),(1022,'岗位修改',104,3,'','','','',1,0,'F','0','0','system:post:edit','#','admin','2026-01-15 06:28:53','',NULL,''),(1023,'岗位删除',104,4,'','','','',1,0,'F','0','0','system:post:remove','#','admin','2026-01-15 06:28:53','',NULL,''),(1024,'岗位导出',104,5,'','','','',1,0,'F','0','0','system:post:export','#','admin','2026-01-15 06:28:53','',NULL,''),(1025,'字典查询',105,1,'#','','','',1,0,'F','0','0','system:dict:query','#','admin','2026-01-15 06:28:53','',NULL,''),(1026,'字典新增',105,2,'#','','','',1,0,'F','0','0','system:dict:add','#','admin','2026-01-15 06:28:53','',NULL,''),(1027,'字典修改',105,3,'#','','','',1,0,'F','0','0','system:dict:edit','#','admin','2026-01-15 06:28:53','',NULL,''),(1028,'字典删除',105,4,'#','','','',1,0,'F','0','0','system:dict:remove','#','admin','2026-01-15 06:28:53','',NULL,''),(1029,'字典导出',105,5,'#','','','',1,0,'F','0','0','system:dict:export','#','admin','2026-01-15 06:28:53','',NULL,''),(1030,'参数查询',106,1,'#','','','',1,0,'F','0','0','system:config:query','#','admin','2026-01-15 06:28:53','',NULL,''),(1031,'参数新增',106,2,'#','','','',1,0,'F','0','0','system:config:add','#','admin','2026-01-15 06:28:53','',NULL,''),(1032,'参数修改',106,3,'#','','','',1,0,'F','0','0','system:config:edit','#','admin','2026-01-15 06:28:53','',NULL,''),(1033,'参数删除',106,4,'#','','','',1,0,'F','0','0','system:config:remove','#','admin','2026-01-15 06:28:53','',NULL,''),(1034,'参数导出',106,5,'#','','','',1,0,'F','0','0','system:config:export','#','admin','2026-01-15 06:28:53','',NULL,''),(1035,'公告查询',107,1,'#','','','',1,0,'F','0','0','system:notice:query','#','admin','2026-01-15 06:28:53','',NULL,''),(1036,'公告新增',107,2,'#','','','',1,0,'F','0','0','system:notice:add','#','admin','2026-01-15 06:28:53','',NULL,''),(1037,'公告修改',107,3,'#','','','',1,0,'F','0','0','system:notice:edit','#','admin','2026-01-15 06:28:53','',NULL,''),(1038,'公告删除',107,4,'#','','','',1,0,'F','0','0','system:notice:remove','#','admin','2026-01-15 06:28:53','',NULL,''),(1039,'操作查询',500,1,'#','','','',1,0,'F','0','0','monitor:operlog:query','#','admin','2026-01-15 06:28:53','',NULL,''),(1040,'操作删除',500,2,'#','','','',1,0,'F','0','0','monitor:operlog:remove','#','admin','2026-01-15 06:28:53','',NULL,''),(1041,'日志导出',500,3,'#','','','',1,0,'F','0','0','monitor:operlog:export','#','admin','2026-01-15 06:28:53','',NULL,''),(1042,'登录查询',501,1,'#','','','',1,0,'F','0','0','monitor:logininfor:query','#','admin','2026-01-15 06:28:53','',NULL,''),(1043,'登录删除',501,2,'#','','','',1,0,'F','0','0','monitor:logininfor:remove','#','admin','2026-01-15 06:28:53','',NULL,''),(1044,'日志导出',501,3,'#','','','',1,0,'F','0','0','monitor:logininfor:export','#','admin','2026-01-15 06:28:53','',NULL,''),(1045,'账户解锁',501,4,'#','','','',1,0,'F','0','0','monitor:logininfor:unlock','#','admin','2026-01-15 06:28:53','',NULL,''),(1046,'在线查询',109,1,'#','','','',1,0,'F','0','0','monitor:online:query','#','admin','2026-01-15 06:28:53','',NULL,''),(1047,'批量强退',109,2,'#','','','',1,0,'F','0','0','monitor:online:batchLogout','#','admin','2026-01-15 06:28:53','',NULL,''),(1048,'单条强退',109,3,'#','','','',1,0,'F','0','0','monitor:online:forceLogout','#','admin','2026-01-15 06:28:53','',NULL,''),(1049,'任务查询',110,1,'#','','','',1,0,'F','0','0','monitor:job:query','#','admin','2026-01-15 06:28:53','',NULL,''),(1050,'任务新增',110,2,'#','','','',1,0,'F','0','0','monitor:job:add','#','admin','2026-01-15 06:28:53','',NULL,''),(1051,'任务修改',110,3,'#','','','',1,0,'F','0','0','monitor:job:edit','#','admin','2026-01-15 06:28:53','',NULL,''),(1052,'任务删除',110,4,'#','','','',1,0,'F','0','0','monitor:job:remove','#','admin','2026-01-15 06:28:53','',NULL,''),(1053,'状态修改',110,5,'#','','','',1,0,'F','0','0','monitor:job:changeStatus','#','admin','2026-01-15 06:28:53','',NULL,''),(1054,'任务导出',110,6,'#','','','',1,0,'F','0','0','monitor:job:export','#','admin','2026-01-15 06:28:53','',NULL,''),(1055,'生成查询',116,1,'#','','','',1,0,'F','0','0','tool:gen:query','#','admin','2026-01-15 06:28:53','',NULL,''),(1056,'生成修改',116,2,'#','','','',1,0,'F','0','0','tool:gen:edit','#','admin','2026-01-15 06:28:53','',NULL,''),(1057,'生成删除',116,3,'#','','','',1,0,'F','0','0','tool:gen:remove','#','admin','2026-01-15 06:28:53','',NULL,''),(1058,'导入代码',116,4,'#','','','',1,0,'F','0','0','tool:gen:import','#','admin','2026-01-15 06:28:53','',NULL,''),(1059,'预览代码',116,5,'#','','','',1,0,'F','0','0','tool:gen:preview','#','admin','2026-01-15 06:28:53','',NULL,''),(1060,'生成代码',116,6,'#','','','',1,0,'F','0','0','tool:gen:code','#','admin','2026-01-15 06:28:53','',NULL,''),(2000,'竞赛管理',0,5,'competition',NULL,'','',1,0,'M','0','0','','guide','admin','2026-01-27 20:52:45','admin','2026-01-31 21:54:25','竞赛管理目录'),(2001,'用户管理',2000,1,'user',NULL,'','',1,0,'M','0','0','','user','admin','2026-01-27 20:52:45','',NULL,'用户管理目录'),(2002,'教师账号管理',2001,1,'teacher','admin/user/teacher','','',1,0,'C','0','0','admin:user:list','user','admin','2026-01-27 20:52:45','',NULL,'教师账号管理菜单'),(2003,'学生账号管理',2001,2,'student','admin/user/student','','',1,0,'C','0','0','admin:user:list','peoples','admin','2026-01-27 20:52:45','',NULL,'学生账号管理菜单'),(2004,'竞赛信息',2000,2,'competition-info',NULL,'','',1,0,'M','0','0','','list','admin','2026-01-27 20:52:45','',NULL,'竞赛信息目录'),(2005,'竞赛信息维护',2004,1,'info','admin/competition/index','','',1,0,'C','0','0','admin:competition:list','edit','admin','2026-01-27 20:52:45','',NULL,'竞赛信息维护菜单'),(2006,'教师赋权',2000,3,'permission',NULL,'','',1,0,'M','0','0','','redis-list','admin','2026-01-27 20:52:45','admin','2026-02-01 11:07:40','教师赋权目录'),(2007,'赋权管理',2006,1,'grant','admin/permission/index','','',1,0,'C','0','0','admin:permission:list','check','admin','2026-01-27 20:52:45','',NULL,'赋权管理菜单'),(2008,'评分管理',2000,4,'score',NULL,'','',1,0,'M','0','0','','star','admin','2026-01-27 20:52:45','',NULL,'评分管理目录'),(2009,'评分结果',2008,1,'result','admin/score/result','','',1,0,'C','0','0','admin:score:list','document','admin','2026-01-27 20:52:45','',NULL,'评分结果菜单'),(2010,'竞赛排名',2008,2,'ranking','admin/score/ranking','','',1,0,'C','0','0','admin:score:ranking','trophy','admin','2026-01-27 20:52:45','',NULL,'竞赛排名菜单'),(2100,'教师查询',2002,1,'','','','',1,0,'F','0','0','admin:user:query','#','admin','2026-01-27 20:52:45','',NULL,''),(2101,'教师新增',2002,2,'','','','',1,0,'F','0','0','admin:user:add','#','admin','2026-01-27 20:52:45','',NULL,''),(2102,'教师修改',2002,3,'','','','',1,0,'F','0','0','admin:user:edit','#','admin','2026-01-27 20:52:45','',NULL,''),(2103,'教师删除',2002,4,'','','','',1,0,'F','0','0','admin:user:remove','#','admin','2026-01-27 20:52:45','',NULL,''),(2104,'教师重置密码',2002,5,'','','','',1,0,'F','0','0','admin:user:resetPwd','#','admin','2026-01-27 20:52:45','',NULL,''),(2105,'学生查询',2003,1,'','','','',1,0,'F','0','0','admin:user:query','#','admin','2026-01-27 20:52:45','',NULL,''),(2106,'学生修改',2003,2,'','','','',1,0,'F','0','0','admin:user:edit','#','admin','2026-01-27 20:52:45','',NULL,''),(2107,'学生重置密码',2003,3,'','','','',1,0,'F','0','0','admin:user:resetPwd','#','admin','2026-01-27 20:52:45','',NULL,''),(2108,'竞赛查询',2005,1,'','','','',1,0,'F','0','0','admin:competition:query','#','admin','2026-01-27 20:52:45','',NULL,''),(2109,'竞赛新增',2005,2,'','','','',1,0,'F','0','0','admin:competition:add','#','admin','2026-01-27 20:52:45','',NULL,''),(2110,'竞赛修改',2005,3,'','','','',1,0,'F','0','0','admin:competition:edit','#','admin','2026-01-27 20:52:45','',NULL,''),(2111,'竞赛删除',2005,4,'','','','',1,0,'F','0','0','admin:competition:remove','#','admin','2026-01-27 20:52:45','',NULL,''),(2112,'赋权查询',2007,1,'','','','',1,0,'F','0','0','admin:permission:query','#','admin','2026-01-27 20:52:45','',NULL,''),(2113,'赋权新增',2007,2,'','','','',1,0,'F','0','0','admin:permission:add','#','admin','2026-01-27 20:52:45','',NULL,''),(2114,'赋权删除',2007,3,'','','','',1,0,'F','0','0','admin:permission:remove','#','admin','2026-01-27 20:52:45','',NULL,''),(2115,'评分查询',2009,1,'','','','',1,0,'F','0','0','admin:score:query','#','admin','2026-01-27 20:52:45','',NULL,''),(2116,'评分导出',2009,2,'','','','',1,0,'F','0','0','admin:score:export','#','admin','2026-01-27 20:52:45','',NULL,''),(2117,'排名查询',2010,1,'','','','',1,0,'F','0','0','admin:score:ranking','#','admin','2026-01-27 20:52:45','',NULL,''),(2118,'排名确认',2010,2,'','','','',1,0,'F','0','0','admin:score:confirm','#','admin','2026-01-27 20:52:45','',NULL,''),(2119,'排名导出',2010,3,'','','','',1,0,'F','0','0','admin:score:export','#','admin','2026-01-27 20:52:45','',NULL,'');
/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_notice`
--

DROP TABLE IF EXISTS `sys_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice` (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='通知公告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_notice`
--

LOCK TABLES `sys_notice` WRITE;
/*!40000 ALTER TABLE `sys_notice` DISABLE KEYS */;
INSERT INTO `sys_notice` VALUES (1,'温馨提醒：2018-07-01 若依新版本发布啦','2',_binary '新版本内容','0','admin','2026-01-15 06:28:54','',NULL,'管理员'),(2,'维护通知：2018-07-01 若依系统凌晨维护','1',_binary '维护内容','0','admin','2026-01-15 06:28:54','',NULL,'管理员');
/*!40000 ALTER TABLE `sys_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_oper_log`
--

DROP TABLE IF EXISTS `sys_oper_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_oper_log` (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '模块标题',
  `business_type` int DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '请求方式',
  `operator_type` int DEFAULT '0' COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '返回参数',
  `status` int DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint DEFAULT '0' COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`) USING BTREE,
  KEY `idx_sys_oper_log_bt` (`business_type`) USING BTREE,
  KEY `idx_sys_oper_log_s` (`status`) USING BTREE,
  KEY `idx_sys_oper_log_ot` (`oper_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=136 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='操作日志记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_oper_log`
--

LOCK TABLES `sys_oper_log` WRITE;
/*!40000 ALTER TABLE `sys_oper_log` DISABLE KEYS */;
INSERT INTO `sys_oper_log` VALUES (100,'角色管理',1,'org.iflytek.web.controller.system.SysRoleController.add()','POST',1,'admin','研发部门','/system/role','127.0.0.1','内网IP','{\"admin\":false,\"createBy\":\"admin\",\"deptCheckStrictly\":true,\"deptIds\":[],\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[],\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-16 07:29:46',328),(101,'角色管理',1,'org.iflytek.web.controller.system.SysRoleController.add()','POST',1,'admin','研发部门','/system/role','127.0.0.1','内网IP','{\"admin\":false,\"createBy\":\"admin\",\"deptCheckStrictly\":true,\"deptIds\":[],\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[],\"params\":{},\"roleId\":101,\"roleKey\":\"student\",\"roleName\":\"学生\",\"roleSort\":0,\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-16 07:31:33',290),(102,'角色管理',1,'org.iflytek.web.controller.system.SysRoleController.add()','POST',1,'admin','研发部门','/system/role','127.0.0.1','内网IP','{\"admin\":false,\"createBy\":\"admin\",\"deptCheckStrictly\":true,\"deptIds\":[],\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[],\"params\":{},\"roleId\":102,\"roleKey\":\"sys admin\",\"roleName\":\"系统管理员\",\"roleSort\":0,\"status\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-16 07:32:16',300),(103,'角色管理',3,'org.iflytek.web.controller.system.SysRoleController.remove()','DELETE',1,'admin','研发部门','/system/role/2','127.0.0.1','内网IP','[2] ',NULL,1,'普通角色已分配,不能删除','2026-01-16 08:21:21',270),(104,'用户管理',3,'org.iflytek.web.controller.system.SysUserController.remove()','DELETE',1,'admin','研发部门','/system/user/101','127.0.0.1','内网IP','[101] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-16 08:51:06',363),(105,'用户管理',1,'org.iflytek.web.controller.system.SysUserController.add()','POST',1,'admin','研发部门','/system/user','127.0.0.1','内网IP','{\"admin\":false,\"createBy\":\"admin\",\"nickName\":\"teacher\",\"params\":{},\"postIds\":[],\"roleIds\":[],\"status\":\"0\",\"userId\":102,\"userName\":\"teacher\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-16 08:51:56',367),(106,'用户管理',2,'org.iflytek.web.controller.system.SysUserController.edit()','PUT',1,'admin','研发部门','/system/user','127.0.0.1','内网IP','{\"admin\":false,\"avatar\":\"\",\"createBy\":\"\",\"createTime\":\"2026-01-16 08:48:27\",\"delFlag\":\"0\",\"email\":\"\",\"loginDate\":\"2026-01-16 16:46:21\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"mengzhiqi666\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2026-01-16 16:44:52\",\"roleIds\":[101],\"roles\":[],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"admin\",\"userId\":100,\"userName\":\"mengzhiqi666\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-21 15:21:06',541),(107,'用户管理',2,'org.iflytek.web.controller.system.SysUserController.edit()','PUT',1,'admin','研发部门','/system/user','127.0.0.1','内网IP','{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2026-01-16 08:51:56\",\"delFlag\":\"0\",\"email\":\"\",\"loginIp\":\"\",\"nickName\":\"teacher\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"roleIds\":[100],\"roles\":[],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"admin\",\"userId\":102,\"userName\":\"teacher\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-21 15:21:17',319),(108,'用户管理',3,'org.iflytek.web.controller.system.SysUserController.remove()','DELETE',1,'admin','研发部门','/system/user/100','127.0.0.1','内网IP','[100] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-21 15:31:26',227),(109,'用户管理',2,'org.iflytek.web.controller.system.SysUserController.edit()','PUT',1,'admin','研发部门','/system/user','127.0.0.1','内网IP','{\"admin\":false,\"avatar\":\"\",\"createBy\":\"\",\"createTime\":\"2026-01-21 15:31:45\",\"delFlag\":\"0\",\"email\":\"\",\"loginDate\":\"2026-01-21 23:28:23\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"mzq\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2026-01-21 23:28:06\",\"roleIds\":[101],\"roles\":[],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"admin\",\"userId\":103,\"userName\":\"mzq\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-21 15:32:47',287),(110,'用户管理',3,'org.iflytek.web.controller.system.SysUserController.remove()','DELETE',1,'admin','研发部门','/system/user/102','127.0.0.1','内网IP','[102] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-21 15:32:50',210),(111,'用户管理',2,'org.iflytek.web.controller.system.SysUserController.edit()','PUT',1,'admin','研发部门','/system/user','127.0.0.1','内网IP','{\"admin\":false,\"avatar\":\"\",\"createBy\":\"\",\"createTime\":\"2026-01-21 15:50:26\",\"delFlag\":\"0\",\"email\":\"\",\"loginIp\":\"\",\"nickName\":\"sys admin\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2026-01-21 23:46:47\",\"roleIds\":[102],\"roles\":[],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"admin\",\"userId\":105,\"userName\":\"sys admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-21 15:52:30',317),(112,'用户管理',2,'org.iflytek.web.controller.system.SysUserController.edit()','PUT',1,'admin','研发部门','/system/user','127.0.0.1','内网IP','{\"admin\":false,\"avatar\":\"\",\"createBy\":\"\",\"createTime\":\"2026-01-21 15:37:01\",\"delFlag\":\"0\",\"email\":\"\",\"loginDate\":\"2026-01-22 00:18:35\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"teacher\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2026-01-21 23:33:22\",\"roleIds\":[100],\"roles\":[],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"admin\",\"userId\":104,\"userName\":\"teacher\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-21 16:23:08',286),(113,'用户管理',2,'org.iflytek.web.controller.system.SysUserController.edit()','PUT',1,'admin','研发部门','/system/user','127.0.0.1','内网IP','{\"admin\":false,\"avatar\":\"\",\"createBy\":\"\",\"createTime\":\"2026-01-21 15:50:26\",\"delFlag\":\"0\",\"email\":\"\",\"loginIp\":\"\",\"nickName\":\"sys admin\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2026-01-21 23:46:47\",\"roleIds\":[102],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":102,\"roleKey\":\"sys admin\",\"roleName\":\"系统管理员\",\"roleSort\":0,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"admin\",\"userId\":105,\"userName\":\"sys admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-21 16:23:11',268),(114,'用户管理',2,'org.iflytek.web.controller.system.SysUserController.edit()','PUT',1,'admin','研发部门','/system/user','127.0.0.1','内网IP','{\"admin\":false,\"avatar\":\"\",\"createBy\":\"\",\"createTime\":\"2026-01-21 15:31:45\",\"delFlag\":\"0\",\"email\":\"\",\"loginDate\":\"2026-01-22 00:17:54\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"mzq\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2026-01-21 23:28:06\",\"roleIds\":[101],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":101,\"roleKey\":\"student\",\"roleName\":\"学生\",\"roleSort\":0,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"admin\",\"userId\":103,\"userName\":\"mzq\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-21 16:23:13',329),(115,'用户管理',2,'org.iflytek.web.controller.system.SysUserController.edit()','PUT',1,'admin','研发部门','/system/user','127.0.0.1','内网IP','{\"admin\":false,\"avatar\":\"\",\"createBy\":\"\",\"createTime\":\"2026-01-21 17:04:47\",\"delFlag\":\"0\",\"email\":\"\",\"loginDate\":\"2026-01-24 18:37:36\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"student\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2026-01-22 01:01:08\",\"roleIds\":[101],\"roles\":[],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"admin\",\"userId\":106,\"userName\":\"student\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-26 05:27:21',903),(116,'用户管理',3,'org.iflytek.web.controller.system.SysUserController.remove()','DELETE',1,'admin','研发部门','/system/user/103','127.0.0.1','内网IP','[103] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-26 06:44:24',238),(117,'用户管理',5,'org.iflytek.web.controller.system.SysUserController.export()','POST',1,'admin','研发部门','/system/user/export','127.0.0.1','内网IP','{\"pageSize\":\"10\",\"pageNum\":\"1\"}',NULL,0,NULL,'2026-01-27 11:14:24',1241),(118,'字典类型',9,'org.iflytek.web.controller.system.SysDictTypeController.refreshCache()','DELETE',1,'admin','研发部门','/system/dict/type/refreshCache','127.0.0.1','内网IP','','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-27 11:14:48',996),(119,'用户管理',1,'org.iflytek.web.controller.admin.AdminUserController.batchCreateTeachers()','POST',1,'admin','研发部门','/admin/user/teachers/batch','127.0.0.1','内网IP','{\"accountPrefix\":\"tc_\",\"count\":2,\"nicknamePrefix\":\"教师\"} ','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"failCount\":0,\"successCount\":2,\"accounts\":[{\"password\":\"tc_001123456\",\"nickname\":\"教师001\",\"userId\":110,\"account\":\"tc_001\"},{\"password\":\"tc_002123456\",\"nickname\":\"教师002\",\"userId\":111,\"account\":\"tc_002\"}]}}',0,NULL,'2026-01-31 20:57:32',364),(120,'用户管理',2,'org.iflytek.web.controller.admin.AdminUserController.resetTeacherPassword()','PUT',1,'admin','研发部门','/admin/user/teachers/110/resetPwd','127.0.0.1','内网IP','110 {} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-31 20:58:08',126),(121,'角色管理',2,'org.iflytek.web.controller.system.SysRoleController.edit()','PUT',1,'admin','研发部门','/system/role','127.0.0.1','内网IP','{\"admin\":false,\"createTime\":\"2026-01-15 06:28:53\",\"dataScope\":\"2\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,116,1055,1056,1057,1058,1059,1060,117],\"params\":{},\"remark\":\"普通角色\",\"roleId\":2,\"roleKey\":\"common\",\"roleName\":\"普通角色\",\"roleSort\":2,\"status\":\"0\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-31 21:54:01',46),(122,'菜单管理',3,'org.iflytek.web.controller.system.SysMenuController.remove()','DELETE',1,'admin','研发部门','/system/menu/4','127.0.0.1','内网IP','4 ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-31 21:54:07',20),(123,'菜单管理',2,'org.iflytek.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createTime\":\"2026-01-27 20:52:45\",\"icon\":\"guide\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2000,\"menuName\":\"竞赛管理\",\"menuType\":\"M\",\"orderNum\":5,\"params\":{},\"parentId\":0,\"path\":\"competition\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-01-31 21:54:25',23),(124,'权限管理',3,'org.iflytek.web.controller.admin.AdminPermissionController.revokePermission()','DELETE',1,'admin','研发部门','/admin/permission/revoke/1/2','127.0.0.1','内网IP','1 2 ','{\"msg\":\"操作成功\",\"code\":200,\"data\":1}',0,NULL,'2026-02-01 10:49:17',22),(125,'权限管理',1,'org.iflytek.web.controller.admin.AdminPermissionController.batchGrantPermission()','POST',1,'admin','研发部门','/admin/permission/grant','127.0.0.1','内网IP','{\"competitionId\":2,\"teacherIds\":[104,110,111]} ',NULL,1,'class java.lang.Integer cannot be cast to class java.lang.Long (java.lang.Integer and java.lang.Long are in module java.base of loader \'bootstrap\')','2026-02-01 10:54:00',9),(126,'权限管理',1,'org.iflytek.web.controller.admin.AdminPermissionController.batchGrantPermission()','POST',1,'admin','研发部门','/admin/permission/grant','127.0.0.1','内网IP','{\"competitionId\":2,\"teacherIds\":[104,110,111]} ','{\"msg\":\"操作成功\",\"code\":200,\"data\":0}',0,NULL,'2026-02-01 10:56:00',278),(127,'权限管理',1,'org.iflytek.web.controller.admin.AdminPermissionController.batchGrantPermission()','POST',1,'admin','研发部门','/admin/permission/grant','127.0.0.1','内网IP','{\"competitionId\":2,\"teacherIds\":[104,110,111]} ','{\"msg\":\"操作成功\",\"code\":200,\"data\":0}',0,NULL,'2026-02-01 10:56:34',53),(128,'权限管理',1,'org.iflytek.web.controller.admin.AdminPermissionController.batchGrantPermission()','POST',1,'admin','研发部门','/admin/permission/grant','127.0.0.1','内网IP','{\"competitionId\":2,\"teacherIds\":[110,111]} ','{\"msg\":\"操作成功\",\"code\":200,\"data\":2}',0,NULL,'2026-02-01 10:59:08',36),(129,'菜单管理',2,'org.iflytek.web.controller.system.SysMenuController.edit()','PUT',1,'admin','研发部门','/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createTime\":\"2026-01-27 20:52:45\",\"icon\":\"redis-list\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2006,\"menuName\":\"教师赋权\",\"menuType\":\"M\",\"orderNum\":3,\"params\":{},\"parentId\":2000,\"path\":\"permission\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-02-01 11:07:40',23),(130,'竞赛管理',2,'org.iflytek.web.controller.admin.AdminCompetitionController.edit()','PUT',1,'admin','研发部门','/admin/competition','127.0.0.1','内网IP','{\"competitionId\":2,\"competitionName\":\"蓝桥杯程序设计竞赛\",\"competitionType\":\"算法设计\",\"createTime\":\"2026-01-27 21:37:27\",\"description\":\"分组科学,新手友好，适合大一学生参加的算法竞赛·\",\"registerEndTime\":\"2026-03-15 23:59:59\",\"registerStartTime\":\"2026-02-01 00:00:00\",\"updateTime\":\"2026-01-28 23:58:10\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-02-01 11:10:17',20),(131,'权限管理',1,'org.iflytek.web.controller.admin.AdminPermissionController.batchGrantPermission()','POST',1,'admin','研发部门','/admin/permission/grant','127.0.0.1','内网IP','{\"competitionId\":3,\"teacherIds\":[104,110,111]} ','{\"msg\":\"操作成功\",\"code\":200,\"data\":2}',0,NULL,'2026-02-01 11:50:37',126),(132,'角色管理',2,'org.iflytek.web.controller.system.SysRoleController.edit()','PUT',1,'admin','研发部门','/system/role','127.0.0.1','内网IP','{\"admin\":false,\"createTime\":\"2026-01-16 07:32:15\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2000,2001,2002,2100,2101,2102,2103,2104,2003,2105,2106,2107,2004,2005,2108,2109,2110,2111,2006,2007,2112,2113,2114,2008,2009,2115,2116,2010,2117,2118,2119],\"params\":{},\"roleId\":102,\"roleKey\":\"comp_admin\",\"roleName\":\"系统管理员\",\"roleSort\":0,\"status\":\"0\",\"updateBy\":\"admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-02-01 11:59:25',23),(133,'用户管理',1,'org.iflytek.web.controller.system.SysUserController.add()','POST',1,'admin','研发部门','/system/user','127.0.0.1','内网IP','{\"admin\":false,\"createBy\":\"admin\",\"deptId\":100,\"nickName\":\"comp_admin\",\"params\":{},\"postIds\":[],\"roleIds\":[102],\"status\":\"0\",\"userId\":112,\"userName\":\"comp_admin\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-02-01 12:00:01',98),(134,'用户管理',2,'org.iflytek.web.controller.system.SysUserController.resetPwd()','PUT',1,'admin','研发部门','/system/user/resetPwd','127.0.0.1','内网IP','{\"admin\":false,\"params\":{},\"updateBy\":\"admin\",\"userId\":111} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-02-01 12:03:37',94),(135,'权限管理',1,'org.iflytek.web.controller.admin.AdminPermissionController.batchGrantPermission()','POST',1,'admin','研发部门','/admin/permission/grant','127.0.0.1','内网IP','{\"competitionId\":7,\"teacherIds\":[110,111]} ','{\"msg\":\"操作成功\",\"code\":200,\"data\":2}',0,NULL,'2026-02-01 12:18:19',36);
/*!40000 ALTER TABLE `sys_oper_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_post`
--

DROP TABLE IF EXISTS `sys_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='岗位信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_post`
--

LOCK TABLES `sys_post` WRITE;
/*!40000 ALTER TABLE `sys_post` DISABLE KEYS */;
INSERT INTO `sys_post` VALUES (1,'ceo','董事长',1,'0','admin','2026-01-15 06:28:53','',NULL,''),(2,'se','项目经理',2,'0','admin','2026-01-15 06:28:53','',NULL,''),(3,'hr','人力资源',3,'0','admin','2026-01-15 06:28:53','',NULL,''),(4,'user','普通员工',4,'0','admin','2026-01-15 06:28:53','',NULL,'');
/*!40000 ALTER TABLE `sys_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) DEFAULT '1' COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) DEFAULT '1' COMMENT '部门树选择项是否关联显示',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=103 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='角色信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES (1,'超级管理员','admin',1,'1',1,1,'0','0','admin','2026-01-15 06:28:53','',NULL,'超级管理员'),(2,'普通角色','common',2,'2',1,1,'0','0','admin','2026-01-15 06:28:53','admin','2026-01-31 21:54:01','普通角色'),(100,'教师','teacher',0,'1',1,1,'0','0','admin','2026-01-16 07:29:45','',NULL,NULL),(101,'学生','student',0,'1',1,1,'0','0','admin','2026-01-16 07:31:33','',NULL,NULL),(102,'系统管理员','comp_admin',0,'1',1,1,'0','0','admin','2026-01-16 07:32:15','admin','2026-02-01 11:59:25',NULL);
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_dept`
--

DROP TABLE IF EXISTS `sys_role_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_dept` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`,`dept_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='角色和部门关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_dept`
--

LOCK TABLES `sys_role_dept` WRITE;
/*!40000 ALTER TABLE `sys_role_dept` DISABLE KEYS */;
INSERT INTO `sys_role_dept` VALUES (2,100),(2,101),(2,105);
/*!40000 ALTER TABLE `sys_role_dept` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_menu`
--

DROP TABLE IF EXISTS `sys_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='角色和菜单关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_menu`
--

LOCK TABLES `sys_role_menu` WRITE;
/*!40000 ALTER TABLE `sys_role_menu` DISABLE KEYS */;
INSERT INTO `sys_role_menu` VALUES (2,1),(2,2),(2,3),(2,100),(2,101),(2,102),(2,103),(2,104),(2,105),(2,106),(2,107),(2,108),(2,109),(2,110),(2,111),(2,112),(2,113),(2,114),(2,115),(2,116),(2,117),(2,500),(2,501),(2,1000),(2,1001),(2,1002),(2,1003),(2,1004),(2,1005),(2,1006),(2,1007),(2,1008),(2,1009),(2,1010),(2,1011),(2,1012),(2,1013),(2,1014),(2,1015),(2,1016),(2,1017),(2,1018),(2,1019),(2,1020),(2,1021),(2,1022),(2,1023),(2,1024),(2,1025),(2,1026),(2,1027),(2,1028),(2,1029),(2,1030),(2,1031),(2,1032),(2,1033),(2,1034),(2,1035),(2,1036),(2,1037),(2,1038),(2,1039),(2,1040),(2,1041),(2,1042),(2,1043),(2,1044),(2,1045),(2,1046),(2,1047),(2,1048),(2,1049),(2,1050),(2,1051),(2,1052),(2,1053),(2,1054),(2,1055),(2,1056),(2,1057),(2,1058),(2,1059),(2,1060),(102,2000),(102,2001),(102,2002),(102,2003),(102,2004),(102,2005),(102,2006),(102,2007),(102,2008),(102,2009),(102,2010),(102,2100),(102,2101),(102,2102),(102,2103),(102,2104),(102,2105),(102,2106),(102,2107),(102,2108),(102,2109),(102,2110),(102,2111),(102,2112),(102,2113),(102,2114),(102,2115),(102,2116),(102,2117),(102,2118),(102,2119);
/*!40000 ALTER TABLE `sys_role_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '手机号码',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '密码',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime DEFAULT NULL COMMENT '密码最后更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=113 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,103,'admin','若依','00','ry@163.com','15888888888','1','','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0','127.0.0.1','2026-02-01 10:42:47','2026-01-15 06:28:53','admin','2026-01-15 06:28:53','',NULL,'管理员'),(2,105,'ry','若依','00','ry@qq.com','15666666666','1','','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0','127.0.0.1','2026-01-15 06:28:53','2026-01-15 06:28:53','admin','2026-01-15 06:28:53','',NULL,'测试员'),(100,NULL,'mengzhiqi666','mengzhiqi666','00','','','0','','$2a$10$OFFsMyv2fN146P96MDxA6..dZvVE.cIMmF8uuBHYXDSUpPuR2nhly','0','2','127.0.0.1','2026-01-16 16:46:21','2026-01-16 16:44:52','','2026-01-16 08:48:27','admin','2026-01-21 15:21:06',NULL),(101,NULL,'aaa','aaa','00','','','0','','$2a$10$2gLnddYCHpydhNGAYeSclOikcjeq5LY1M3STc3iGoHgPu4v8V9HKG','0','2','',NULL,'2026-01-16 16:48:33','','2026-01-16 08:48:34','',NULL,NULL),(102,NULL,'teacher','teacher','00','','','0','','$2a$10$LbVwnXaqU9psNa8GaWeDXu1fcheYODlPlImqRoLviQmY7MM5JUYTi','0','2','',NULL,NULL,'admin','2026-01-16 08:51:56','admin','2026-01-21 15:21:17',NULL),(103,NULL,'mzq','mzq','00','','','0','','$2a$10$RhaoeYd1c39Nf9A5A8Uq8.j0SVktmWX0Gmx8KOSfwtfY9z/6zPc7S','0','2','127.0.0.1','2026-01-24 18:37:51','2026-01-21 23:28:06','','2026-01-21 15:31:45','admin','2026-01-21 16:23:13',NULL),(104,NULL,'teacher','teacher','00','','','0','','$2a$10$PH0CkOUKgzIQ3sBHEgkIne0ZFIH40GwL6LuDHyCQO.jsLn8DAntcy','0','0','127.0.0.1','2026-01-26 15:16:50','2026-01-21 23:33:22','','2026-01-21 15:37:01','admin','2026-01-21 16:23:08',NULL),(105,NULL,'sys admin','sys admin','00','','','0','','$2a$10$oP3vWiDi7a1YI/T.QJpaSOicyZ3Qs7.wDpANA13o/vi0B6ybr/CZG','0','0','127.0.0.1','2026-01-26 15:17:10','2026-01-21 23:46:47','','2026-01-21 15:50:26','admin','2026-01-21 16:23:11',NULL),(106,NULL,'student','student','00','','','0','','$2a$10$eFLstPuJg0AV1MfEaw7li.m.tIbHnFlOGdqrWD.h8SQaZtYnTuLOK','0','0','127.0.0.1','2026-01-27 18:26:25','2026-01-22 01:01:08','','2026-01-21 17:04:47','admin','2026-01-26 05:27:21',NULL),(107,NULL,'test01','test01','00','','','0','','$2a$10$R7AA37ZtoqTfIGvmU3R3..teZF0.Xv4jTqzL2SsxUoseCD9iTJmXO','0','0','127.0.0.1','2026-01-28 21:49:10','2026-01-28 21:48:51','','2026-01-28 21:48:51','',NULL,NULL),(108,NULL,'st01','st01','00','','','0','','$2a$10$B.P6d49uZnV5cwsDGT3wJOJJ1gFgJJogkCMFXAyMcsoU8Q3/WvcU6','0','0','127.0.0.1','2026-02-01 12:10:36','2026-01-28 22:18:46','','2026-01-28 22:18:45','',NULL,NULL),(109,NULL,'st02','st02','00','','','0','','$2a$10$y1t.5juRN4784K2WT/3f0ed/2wz1Kmw1ZHYr.gi/EJv1HUryFMLC.','0','0','127.0.0.1','2026-02-01 11:22:11','2026-01-29 13:20:27','','2026-01-29 13:20:26','',NULL,NULL),(110,NULL,'tc_001','教师001','00','','','0','','$2a$10$bmMQ.7dgxEs5DKZr0MThbeu0qz2Okb567GhuouH9uGgn1Baigfr0S','0','0','127.0.0.1','2026-02-01 10:01:24',NULL,'admin','2026-01-31 20:57:32','','2026-01-31 20:58:08',NULL),(111,NULL,'tc_002','教师002','00','','','0','','$2a$10$YuPlKF2LKMqMJ7jgAAKnLepdUm9qfaiUINGy3hXi5sp8ozEf6mqke','0','0','127.0.0.1','2026-02-01 12:03:41','2026-02-01 12:03:37','admin','2026-01-31 20:57:32','','2026-02-01 12:03:37',NULL),(112,100,'comp_admin','comp_admin','00','','','0','','$2a$10$lEvonRZRjYWltt.yYpFZWu.a7mqF7N3mmxtupmIJEV06GwzY4CWra','0','0','127.0.0.1','2026-02-01 12:00:17',NULL,'admin','2026-02-01 12:00:01','',NULL,NULL);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_post`
--

DROP TABLE IF EXISTS `sys_user_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_post` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`,`post_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户与岗位关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_post`
--

LOCK TABLES `sys_user_post` WRITE;
/*!40000 ALTER TABLE `sys_user_post` DISABLE KEYS */;
INSERT INTO `sys_user_post` VALUES (1,1),(2,2);
/*!40000 ALTER TABLE `sys_user_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_role`
--

DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户和角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_role`
--

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
INSERT INTO `sys_user_role` VALUES (1,1),(2,2),(104,100),(105,102),(106,101),(108,101),(109,101),(110,100),(111,100),(112,102);
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team`
--

DROP TABLE IF EXISTS `team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team` (
  `team_id` bigint NOT NULL AUTO_INCREMENT COMMENT '队伍ID，主键',
  `competition_id` bigint DEFAULT NULL COMMENT '关联竞赛ID，外键',
  `team_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '队伍名称',
  `team_members` json NOT NULL COMMENT '队伍成员信息，JSON格式存储',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '队伍创建时间',
  PRIMARY KEY (`team_id`) USING BTREE,
  KEY `competition_id` (`competition_id`) USING BTREE,
  CONSTRAINT `team_ibfk_1` FOREIGN KEY (`competition_id`) REFERENCES `competition` (`competition_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team`
--

LOCK TABLES `team` WRITE;
/*!40000 ALTER TABLE `team` DISABLE KEYS */;
INSERT INTO `team` VALUES (1,1,'队伍A','[{\"name\": \"张三\", \"major\": \"计算机\", \"student_no\": \"2023001\", \"teacher_name\": \"王老师\"}]','2026-01-27 20:52:45'),(2,12,'team1','[{\"name\": \"222\", \"major\": \"44\", \"studentNo\": \"333\", \"teacherName\": \"444\"}, {\"name\": \"李四\", \"major\": \"经济\", \"studentNo\": \"898989\", \"teacherName\": \"张三\"}, {\"name\": \"郭靖\", \"major\": \"武术\", \"studentNo\": \"787878788\", \"teacherName\": \"王五\"}]','2026-01-29 08:58:42'),(3,12,'队伍2','[{\"name\": \"张三\", \"major\": \"软件\", \"studentNo\": \"12333\", \"teacherName\": \"22222\"}]','2026-01-29 13:29:04'),(4,16,'第一队','[{\"name\": \"黄飞鸿\", \"major\": \"计算机\", \"studentNo\": \"202389999\", \"teacherName\": \"张三\"}, {\"name\": \"李世民\", \"major\": \"外文\", \"studentNo\": \"2023333323\", \"teacherName\": \"纵横\"}, {\"name\": \"赵匡胤\", \"major\": \"物理\", \"studentNo\": \"238989899\", \"teacherName\": \"爱因斯坦\"}]','2026-01-29 18:57:27'),(5,2,'测试队伍1','[{\"name\": \"张三\", \"major\": \"软件\", \"studentNo\": \"28989898989\", \"teacherName\": \"李四\"}, {\"name\": \"郭靖\", \"major\": \"数学\", \"studentNo\": \"7878787878\", \"teacherName\": \"洪七公\"}]','2026-02-01 10:53:45'),(6,2,'一定赢队伍','[{\"name\": \"张三三\", \"major\": \"物理\", \"studentNo\": \"8989889\", \"teacherName\": \"高手\"}]','2026-02-01 11:23:15'),(7,3,'队伍889','[{\"name\": \"张三\", \"major\": \"物理\", \"studentNo\": \"76110022\", \"teacherName\": \"教师001\"}]','2026-02-01 11:37:56'),(8,6,'队伍7788','[{\"name\": \"王琪琪\", \"major\": \"海洋\", \"studentNo\": \"1266666\", \"teacherName\": \"教师001\"}]','2026-02-01 11:51:42'),(9,6,'d队伍1222','[{\"name\": \"张三三\", \"major\": \"计算机\", \"studentNo\": \"2222\", \"teacherName\": \"教师002\"}]','2026-02-01 12:11:11'),(10,7,'测试11','[{\"name\": \"王11\", \"major\": \"生物\", \"studentNo\": \"1266222\", \"teacherName\": \"测试1\"}]','2026-02-01 12:16:58');
/*!40000 ALTER TABLE `team` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `tb_teacher_team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_teacher_team` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `teacher_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `work_unit` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `political_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `birth_date` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `team_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_team_phone` (`team_id`,`phone`) USING BTREE,
  KEY `idx_team_id` (`team_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键',
  `account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录账号，唯一',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户昵称',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户密码',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户邮箱',
  `role` enum('学生','教师','学校管理员','系统管理员') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户角色：学生、教师、学校管理员、系统管理员',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '用户创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '用户更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE KEY `account` (`account`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'sysadmin','系统管理员','sysadmin123',NULL,NULL,'系统管理员','2026-01-27 20:52:45','2026-01-27 20:52:45'),(2,'admin1','学校管理员1','admin123',NULL,NULL,'学校管理员','2026-01-27 20:52:45','2026-01-27 20:52:45');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

--
-- Incremental schema for review assignment support
--

CREATE TABLE IF NOT EXISTS `competition_review_assignment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `competition_id` bigint NOT NULL COMMENT '赛事ID',
  `participation_id` bigint NOT NULL COMMENT '参赛记录ID',
  `team_id` bigint NOT NULL COMMENT '队伍ID',
  `reviewer_id` bigint NOT NULL COMMENT '评审老师用户ID',
  `reviewer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '评审老师姓名',
  `assignment_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ASSIGNED' COMMENT '分配状态',
  `assigned_by` bigint DEFAULT NULL COMMENT '分配操作人',
  `assigned_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_participation_reviewer` (`participation_id`,`reviewer_id`) USING BTREE,
  KEY `idx_assignment_competition` (`competition_id`) USING BTREE,
  KEY `idx_assignment_reviewer` (`reviewer_id`) USING BTREE,
  KEY `idx_assignment_team` (`team_id`) USING BTREE,
  CONSTRAINT `fk_assignment_competition` FOREIGN KEY (`competition_id`) REFERENCES `competition` (`competition_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_assignment_participation` FOREIGN KEY (`participation_id`) REFERENCES `competition_participation` (`participation_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_assignment_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-01 12:20:51
