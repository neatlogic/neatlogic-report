ALTER TABLE `report` ADD COLUMN `sql_edit_mode` varchar(20) DEFAULT 'xml' COMMENT 'SQL编辑模式：xml手写配置，graph绘图配置' AFTER `sql`;
ALTER TABLE `report` ADD COLUMN `sql_graph_config` longtext COMMENT 'SQL绘图配置' AFTER `sql_edit_mode`;
