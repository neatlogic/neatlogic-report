-- ----------------------------
-- Table structure for report
-- ----------------------------
CREATE TABLE `report` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  `type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '类型',
  `sql` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'sql配置',
  `condition` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '条件模板',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '内容模板',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否激活',
  `visit_count` int DEFAULT '0' COMMENT '使用次数',
  `fcu` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `lcu` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_type` (`type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表模版表';

-- ----------------------------
-- Table structure for report_auth
-- ----------------------------
CREATE TABLE `report_auth` (
  `report_id` bigint NOT NULL COMMENT '报表模版ID',
  `type` enum('user','role','team') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'user：用户,role：角色,team：分组',
  `auth_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'uuid',
  PRIMARY KEY (`report_id`,`type`,`auth_uuid`) USING BTREE,
  KEY `idx_uuid` (`auth_uuid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表模版授权表';

-- ----------------------------
-- Table structure for report_blackwhitelist
-- ----------------------------
CREATE TABLE `report_blackwhitelist` (
  `id` bigint NOT NULL COMMENT 'id',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '说明',
  `item_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '表名或字段名，支持*作为模糊匹配，如果是字段名且带.，代表精确管理某张表的字段',
  `item_type` enum('table','column') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '对象类型',
  `type` enum('whitelist','blacklist') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '先根据白名单开放，再根据黑名单排除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk` (`item_name`,`item_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表黑白名单表';

-- ----------------------------
-- Table structure for report_china_country
-- ----------------------------
CREATE TABLE `report_china_country` (
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '国家名称',
  `adcode` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '区号',
  `level` enum('district') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '水平',
  `x` float DEFAULT NULL COMMENT 'x轴',
  `y` float DEFAULT NULL COMMENT 'y轴',
  PRIMARY KEY (`adcode`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表国家配置';

-- ----------------------------
-- Table structure for report_param
-- ----------------------------
CREATE TABLE `report_param` (
  `report_id` bigint NOT NULL COMMENT '报表id',
  `id` bigint DEFAULT NULL COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `type` enum('forminput','formselect','formcheckbox','formradio','formdate','formdaterange','formselects') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '类型',
  `label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '标签',
  `sort` int DEFAULT NULL COMMENT '排序',
  `width` int DEFAULT NULL COMMENT '宽度',
  `config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '配置',
  PRIMARY KEY (`report_id`,`name`) USING BTREE,
  KEY `idx_id` (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表模版参数表';

-- ----------------------------
-- Table structure for report_receiver
-- ----------------------------
CREATE TABLE `report_receiver` (
  `report_send_job_id` bigint NOT NULL COMMENT '报表发送计划ID',
  `receiver` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接收人UUID或邮箱地址',
  `type` enum('to','cc') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'to：收件人；cc：抄送人',
  PRIMARY KEY (`report_send_job_id`,`receiver`,`type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表发送接收人表';

-- ----------------------------
-- Table structure for report_send_job
-- ----------------------------
CREATE TABLE `report_send_job` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `email_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '邮件标题',
  `email_content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '邮件正文',
  `cron` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cron表达式',
  `is_active` tinyint NOT NULL COMMENT '1：启用；0：禁用',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '修改时间',
  `lcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表发送计划表';

-- ----------------------------
-- Table structure for report_send_job_relation
-- ----------------------------
CREATE TABLE `report_send_job_relation` (
  `report_send_job_id` bigint NOT NULL COMMENT '报表发送计划ID',
  `report_id` bigint NOT NULL COMMENT '报表ID',
  `condition` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '条件',
  `config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '配置',
  `sort` int DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`report_send_job_id`,`report_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表发送计划-报表关联表';

-- ----------------------------
-- Table structure for report_statement
-- ----------------------------
CREATE TABLE `report_statement` (
  `id` bigint DEFAULT NULL COMMENT 'id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  `is_active` tinyint(1) DEFAULT NULL COMMENT '是否激活',
  `config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '画布额外配置',
  `widget_list` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '组件列表,json数组格式',
  `width` int DEFAULT NULL COMMENT '宽度',
  `height` int DEFAULT NULL COMMENT '高度',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '描述',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '首次创建时间',
  `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '首次创建人',
  `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '最近修改时间',
  `lcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最近修改人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表配置';

-- ----------------------------
-- Table structure for reportinstance
-- ----------------------------
CREATE TABLE `reportinstance` (
  `id` bigint NOT NULL COMMENT 'id',
  `report_id` bigint DEFAULT NULL COMMENT '报表id',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '报表名称',
  `visit_count` int DEFAULT NULL COMMENT '浏览次数',
  `is_active` tinyint(1) DEFAULT NULL COMMENT '是否激活',
  `config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '配置json格式',
  `fcu` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `lcu` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '修改时间',
  `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表实例表';

-- ----------------------------
-- Table structure for reportinstance_auth
-- ----------------------------
CREATE TABLE `reportinstance_auth` (
  `reportinstance_id` bigint NOT NULL COMMENT '报表实例ID',
  `type` enum('user','role','team') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'user：用户,role：角色,team：分组',
  `auth_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'uuid',
  PRIMARY KEY (`reportinstance_id`,`type`,`auth_uuid`) USING BTREE,
  KEY `idx_uuid` (`auth_uuid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表实例授权表';

-- ----------------------------
-- Table structure for reportinstance_table_column
-- ----------------------------
CREATE TABLE `reportinstance_table_column` (
  `reportinstance_id` bigint NOT NULL COMMENT '报表实例ID',
  `table_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '表格ID',
  `column` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字段名',
  `sort` int DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`reportinstance_id`,`table_id`,`column`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表实例中含有的表格列表';