/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.report.api;

import com.lowagie.text.pdf.BaseFont;
import neatlogic.framework.util.ChineseFont;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestMain {
    public static void main(String[] args) throws IOException {
        String html1 = """
                <html xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40">
                <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"></meta>
                <style type="text/css">
                html {font-family: "PingFang SC", "Helvetica Neue", "思源黑体", "Microsoft YaHei", "黑体", Helvetica;line-height: 1.42857143; color: #666666;font-size: 14px;}
                table{width: 100%; max-width: 100%; margin-bottom: 10px; margin-top: 0;border-collapse:collapse;border-spacing:0;border-top:1px solid #ddd;}
                th,td{padding: 8px; line-height: 1.42857143;  vertical-align: top; border-top: 1px solid #dddddd;}
                th{text-align: left;color: #999999;}
                .table-condensed th,.table-condensed td{padding: 5px;}
                div.well {  min-height: 20px; padding: 19px; line-height: 1.8; border-radius: 4px;  background: #fffdf2; border: 1px solid #ffd821;box-shadow: 0 0 5px 0 rgba(0,0,0,0.10); border-radius: 5px;}
                .text-primary { color: #336eff;}
                </style>
                </head>
                <body>
                <div class="ivu-card ivu-card-dis-hover ivu-card-shadow">
                  <div><div id="totalTableData" class="ivu-card ivu-card-dis-hover ivu-card-shadow"><div class="ivu-card-head">工单总数</div><div class="ivu-card-body tstable-container tstable-normal border tstable-no-fixedHeader block-large"><div class="tstable-main bg-op"><table tableName="工单总数" class="table-main tstable-body"><thead><tr class="th-left"><th>工单总数</th></tr></thead><tbody class="tbody-main"><tr><td>1666</td></tr></tbody></table></div></div></div></div>
                    <div class="ivu-card-body tstable-container tstable-normal border tstable-no-fixedHeader block-large">
                        <div class="tstable-main bg-op">
                            <table class="table-main tstable-body">
                              \s
                              <tbody class="tbody-main">
                                 <tr>
                                  \s
                                   <td valign="top"><div><div id="tableData" class="ivu-card ivu-card-dis-hover ivu-card-shadow"><div class="ivu-card-head">服务工单数量统计</div><div class="ivu-card-body tstable-container tstable-normal border tstable-no-fixedHeader block-large"><div class="tstable-main bg-op"><table tableName="服务工单数量统计" class="table-main tstable-body"><thead><tr class="th-left"><th>工单数量</th><th>服务名称</th></tr></thead><tbody class="tbody-main"><tr><td>52</td><td>分流步骤</td></tr><tr><td>49</td><td>linbq_1215_测试自动处理节点</td></tr><tr><td>46</td><td>移动端表单测试</td></tr><tr><td>45</td><td>事件登记</td></tr><tr><td>36</td><td>linbq_0407_测试子任务</td></tr><tr><td>35</td><td>dbf_测试移动端</td></tr><tr><td>29</td><td>dbf_线流转</td></tr><tr><td>27</td><td>yaojn_变更节点</td></tr><tr><td>25</td><td>linbq_0311_测试上报</td></tr><tr><td>25</td><td>dbf_表格输入</td></tr><tr><td>24</td><td>2022_01_13测试审批节点文案调整</td></tr><tr><td>24</td><td>投产系统变更</td></tr><tr><td>23</td><td>yaojn_测试表单</td></tr><tr><td>23</td><td>linbq_0121_测试多个连续处理节点</td></tr><tr><td>22</td><td>linbq_1122_测试sla</td></tr><tr><td>22</td><td>linbq_0507_测试开始按钮替换文案</td></tr><tr><td>21</td><td>dbf_联动</td></tr><tr><td>20</td><td>linbq_1231_1149</td></tr><tr><td>20</td><td>yaojn_测试所有节点</td></tr><tr><td>19</td><td>yaojn_测试转报</td></tr><tr><td>19</td><td>dbf_变更</td></tr><tr><td>18</td><td>linbq_1214_测试步骤子任务</td></tr><tr><td>18</td><td>yjn0423测试时效</td></tr><tr><td>18</td><td>dbf_回复</td></tr><tr><td>18</td><td>yjn_0321_测试表单转知识所有新表单组件</td></tr><tr><td>18</td><td>测试附件从剪切板获取</td></tr><tr><td>17</td><td>dbf_变更创建</td></tr><tr><td>17</td><td>upload</td></tr><tr><td>17</td><td>linbq_0211_测试定时节点</td></tr><tr><td>17</td><td>yjn_0516_测试附件上传问题</td></tr><tr><td>16</td><td>测试上报内容修改</td></tr><tr><td>15</td><td>linbq_0217_测试表单</td></tr><tr><td>15</td><td>统信-问题上报</td></tr><tr><td>15</td><td>虚拟桌面申请</td></tr><tr><td>14</td><td>lvzk_审批</td></tr><tr><td>14</td><td>dbf_eoa</td></tr><tr><td>14</td><td>dbf_定时skdljgs胜利大街上课红色故事发送到发送到方式方式顺风顺风刷卡机发顺丰</td></tr><tr><td>13</td><td>linbq_0414_测试结束节点回退</td></tr><tr><td>13</td><td>巡检问题服务</td></tr><tr><td>13</td><td>test111</td></tr><tr><td>13</td><td>小苏通测试_选择流转路径</td></tr><tr><td>12</td><td>测试事件流程</td></tr><tr><td>12</td><td>linbq_0311_测试工单详情页权限</td></tr><tr><td>11</td><td>自动化输出参数赋值测试</td></tr><tr><td>11</td><td>linbq_1220_测试第二节点回退到开始节点</td></tr><tr><td>11</td><td>dbf_子任务</td></tr><tr><td>11</td><td>linbq_0310_表格选择组件</td></tr><tr><td>11</td><td>时间组件自定义规则_无联动</td></tr><tr><td>11</td><td>dbf_矩阵</td></tr><tr><td>10</td><td>linbq_1206_1119</td></tr><tr><td>10</td><td>linbq_0525_测试回复列表</td></tr><tr><td>10</td><td>dbf_优先级测试</td></tr><tr><td>10</td><td>时间组件加小时服务目录</td></tr><tr><td>10</td><td>dbf_默认场景</td></tr><tr><td>10</td><td>时间组件自定义规则</td></tr><tr><td>10</td><td>linbq_1127_测试sla</td></tr><tr><td>9</td><td>测试指派范围</td></tr><tr><td>9</td><td>需求投产</td></tr><tr><td>9</td><td>linbq_1107_附件上传开关</td></tr><tr><td>9</td><td>linbq_1102_测试自动审批场景</td></tr><tr><td>9</td><td>zzm_表格输入组件校验</td></tr><tr><td>9</td><td>dbf_表格选择</td></tr><tr><td>9</td><td>linbq_0119_测试重审</td></tr><tr><td>9</td><td>yjn_测试日期组件</td></tr><tr><td>8</td><td>linbq_0331_测试自动化节点</td></tr><tr><td>8</td><td>创建基础架构变更_1_linbq</td></tr><tr><td>8</td><td>工单转知识</td></tr><tr><td>8</td><td>linbq0202测试修改工单流程图</td></tr><tr><td>8</td><td>linbq_0221_1021</td></tr><tr><td>8</td><td>dbf_newform_勿动</td></tr><tr><td>8</td><td>linbq_1014_测试分配处理人_表单值支持文本框</td></tr><tr><td>8</td><td>统信-事件上报</td></tr><tr><td>8</td><td>linbq_测试转报1</td></tr><tr><td>8</td><td>yjn__0303_测试表单自定义格式</td></tr><tr><td>8</td><td>linbq_1219_1111</td></tr><tr><td>8</td><td>下拉树默认值</td></tr><tr><td>8</td><td>linbq_0515_b</td></tr><tr><td>8</td><td>linbq_1023_测试评分模板</td></tr><tr><td>7</td><td>linbq_0223_测试时效有不会激活节点场景</td></tr><tr><td>7</td><td>linbq_0222_测试时效</td></tr><tr><td>7</td><td>linbq_1110_表格选择组件绑定矩阵</td></tr><tr><td>7</td><td>yjn_0428_表单禁用_服务目录</td></tr><tr><td>7</td><td>linbq_0414_测试自动处理节点</td></tr><tr><td>7</td><td>linbq_0201_1832</td></tr><tr><td>7</td><td>子服务</td></tr><tr><td>7</td><td>linbq_0621_新自动化组件_常量</td></tr><tr><td>7</td><td>dbf_表单09</td></tr><tr><td>6</td><td>linbq_0112_eoa</td></tr><tr><td>6</td><td>linbq_0120_测试重审</td></tr><tr><td>6</td><td>linbq_0329_测试撤回权限</td></tr><tr><td>6</td><td>linbq_0712_上报节点日志</td></tr><tr><td>6</td><td>linbq_0309_苏州银行对接小苏通_工单信息获取接口</td></tr><tr><td>6</td><td>linbq_0228_测试回退线</td></tr><tr><td>6</td><td>test</td></tr><tr><td>6</td><td>upload_test</td></tr><tr><td>6</td><td>20230506文案测试</td></tr><tr><td>5</td><td>0511表单联动必填设置</td></tr><tr><td>5</td><td>linbq_测试上报成功时触发步骤成功</td></tr><tr><td>5</td><td>yjn_0329_日期禁用</td></tr><tr><td>5</td><td>yjn_0530_隐藏清空值组件</td></tr><tr><td>5</td><td>test审批邮件</td></tr><tr><td>5</td><td>协议目标</td></tr><tr><td>5</td><td>父服务</td></tr><tr><td>5</td><td>yjn_日期组件</td></tr><tr><td>5</td><td>linbq_0112_测试工单步骤回退后再次处理报错问题</td></tr><tr><td>5</td><td>SLA观察测试</td></tr><tr><td>5</td><td>创建知识</td></tr><tr><td>5</td><td>工单表单修改必填测试</td></tr><tr><td>5</td><td>OA或UM门户维护申请</td></tr><tr><td>5</td><td>0428测试超时</td></tr><tr><td>5</td><td>linbq_0207_测试变更</td></tr><tr><td>4</td><td>linbq_1210_测试工单流转附件删除问题</td></tr><tr><td>4</td><td>测试工单导出</td></tr><tr><td>4</td><td>客户事件管理流程</td></tr><tr><td>4</td><td>linbq_0715_测试时效延迟_A</td></tr><tr><td>4</td><td>linbq_1206_1448</td></tr><tr><td>4</td><td>yjn_测试色板颜色值_1207_1835</td></tr><tr><td>4</td><td>linbq_1219_1605</td></tr><tr><td>4</td><td>linbq_0322_测试邮件发送</td></tr><tr><td>4</td><td>linbq_0428_测试修改下拉框活动日志</td></tr><tr><td>4</td><td>dbf_cmdb</td></tr><tr><td>4</td><td>yjn_1</td></tr><tr><td>4</td><td>linbq_0513_测试分配策略</td></tr><tr><td>4</td><td>linbq_0210_测试不自动开始</td></tr><tr><td>4</td><td>linbq_1222_测试根据工作量分配策略</td></tr><tr><td>3</td><td>linbq_0516_测试变更步骤</td></tr><tr><td>3</td><td>linbq_0426_测试表格输入组件与邮件模板</td></tr><tr><td>3</td><td>linbq_0512_测试时效关联多个步骤时各用一个时效</td></tr><tr><td>3</td><td>linbq_1107_1656</td></tr><tr><td>3</td><td>linbq_0906_测试表单分配处理人</td></tr><tr><td>3</td><td>数据采集申请_取数</td></tr><tr><td>3</td><td>linbq_0715_测试时效延迟_B</td></tr><tr><td>3</td><td>工单号规则测试</td></tr><tr><td>3</td><td>test_hide</td></tr><tr><td>3</td><td>linbq_1117_1541</td></tr><tr><td>3</td><td>linbq_0222_测试时效2</td></tr><tr><td>3</td><td>数据修改申请_改数</td></tr><tr><td>3</td><td>linbq_0113_堡垒机权限申请_非投产</td></tr><tr><td>3</td><td>linbq_0512_测试步骤别名</td></tr><tr><td>3</td><td>linbq_0209_1824</td></tr><tr><td>2</td><td>linbq_0114_测试步骤之间连线颜色</td></tr><tr><td>2</td><td>linbq_0224_测试工单耗时</td></tr><tr><td>2</td><td>yjn_测试表单日期对比问题</td></tr><tr><td>2</td><td>小苏通对接测试专用服务</td></tr><tr><td>2</td><td>临时管理员权限申请</td></tr><tr><td>2</td><td>linbq_测试文案</td></tr><tr><td>2</td><td>lvzk_test_autoexec勿动</td></tr><tr><td>2</td><td>linbq_0331_测试自动化节点异常信息显示</td></tr><tr><td>2</td><td>表单场景</td></tr><tr><td>2</td><td>linbq_0616_事件回退</td></tr><tr><td>2</td><td>数据实验室权限申请变更</td></tr><tr><td>2</td><td>linbq_0321_测试自动审批功能</td></tr><tr><td>2</td><td>linbq_1202_测试时效超时后显示</td></tr><tr><td>2</td><td>附件</td></tr><tr><td>2</td><td>基础架构问题管理_copy</td></tr><tr><td>2</td><td>linbq_0725_测试子任务</td></tr><tr><td>2</td><td>testupload</td></tr><tr><td>2</td><td>linbq_0530_定时节点</td></tr><tr><td>2</td><td>linbq_0621_新自动化组件_表单普通组件</td></tr><tr><td>2</td><td>linbq_1104_测试表格选择组件</td></tr><tr><td>2</td><td>linbq_0331_测试数据转换节点</td></tr><tr><td>2</td><td>linbq_测试工单中心通过表单属性过滤</td></tr><tr><td>2</td><td>linbq_0202_1019</td></tr><tr><td>2</td><td>yjn_0412_测试表单</td></tr><tr><td>2</td><td>dbf_事件上报</td></tr><tr><td>2</td><td>0601_所有的步骤信息</td></tr><tr><td>2</td><td>yjn_验证日期年月日格式</td></tr><tr><td>2</td><td>linbq_1126_测试sla的job</td></tr><tr><td>2</td><td>linbq_0523_文本框保存与获取时数据类型不一致</td></tr><tr><td>2</td><td>linbq_0711_eoa</td></tr><tr><td>2</td><td>linbq_0414_测试回复模板</td></tr><tr><td>1</td><td>linbq_1008_密码</td></tr><tr><td>1</td><td>创新中心无线网白名单开通申请</td></tr><tr><td>1</td><td>新系统录入</td></tr><tr><td>1</td><td>测试标题模板</td></tr><tr><td>1</td><td>upload_0518test</td></tr><tr><td>1</td><td>AD邮箱密码重置申请</td></tr><tr><td>1</td><td>行外人员AD账号申请</td></tr><tr><td>1</td><td>yjn_日期组件禁用_2222</td></tr><tr><td>1</td><td>yjn_0415_测试自定义矩阵引用</td></tr><tr><td>1</td><td>linbq_1024_测试时效</td></tr><tr><td>1</td><td>baron_test</td></tr><tr><td>1</td><td>yjn_0525_表单单选复选样式</td></tr><tr><td>1</td><td>机房设备报废_扩容_维修申请</td></tr><tr><td>1</td><td>流程节点动作触发测试</td></tr><tr><td>1</td><td>test分叉流程</td></tr><tr><td>1</td><td>测试分流</td></tr><tr><td>1</td><td>人员进出机房申请</td></tr><tr><td>1</td><td>linbq_0121_测试撤回</td></tr><tr><td>1</td><td>linbq_0420_测试对接小苏通工单处理统一接口</td></tr><tr><td>1</td><td>linbq_0621_新自动化组件_表单表格组件</td></tr><tr><td>1</td><td>yjn_测试流程问题</td></tr><tr><td>1</td><td>测试EOA</td></tr><tr><td>1</td><td>堡垒机用户申请流程</td></tr><tr><td>1</td><td>linbq_1216_测试条件节点日志显示</td></tr><tr><td>1</td><td>在线学习平台故障支持服务</td></tr><tr><td>1</td><td>linbq_0308_1407</td></tr><tr><td>1</td><td>表单色板</td></tr><tr><td>1</td><td>创建基础架构变更</td></tr><tr><td>1</td><td>linbq_0207_测试分配处理人</td></tr><tr><td>1</td><td>linbq_1117_测试新表单</td></tr><tr><td>1</td><td>test工单中心表单搜索</td></tr><tr><td>1</td><td>linbq0108测试定时节点</td></tr><tr><td>1</td><td>linbq_0308_1410</td></tr><tr><td>1</td><td>实验室用户申请变更</td></tr></tbody></table></div></div></div></div></td>
                                   <td valign="top"><div><div id="tableDataDay" class="ivu-card ivu-card-dis-hover ivu-card-shadow"><div class="ivu-card-head">按天统计</div><div class="ivu-card-body tstable-container tstable-normal border tstable-no-fixedHeader block-large"><div class="tstable-main bg-op"><table tableName="按天统计" class="table-main tstable-body"><thead><tr class="th-left"><th>天</th><th>工单数量</th></tr></thead><tbody class="tbody-main"><tr><td>2026-03-23</td><td>5</td></tr><tr><td>2026-03-18</td><td>2</td></tr><tr><td>2026-03-16</td><td>6</td></tr><tr><td>2026-03-13</td><td>8</td></tr><tr><td>2026-03-12</td><td>17</td></tr><tr><td>2026-03-11</td><td>6</td></tr><tr><td>2026-02-28</td><td>1</td></tr><tr><td>2026-02-26</td><td>1</td></tr><tr><td>2026-02-25</td><td>4</td></tr><tr><td>2026-02-24</td><td>2</td></tr><tr><td>2026-02-07</td><td>2</td></tr><tr><td>2026-02-06</td><td>2</td></tr><tr><td>2026-02-02</td><td>7</td></tr><tr><td>2026-01-27</td><td>1</td></tr><tr><td>2026-01-08</td><td>1</td></tr><tr><td>2026-01-07</td><td>1</td></tr><tr><td>2026-01-06</td><td>2</td></tr><tr><td>2026-01-04</td><td>1</td></tr><tr><td>2025-12-27</td><td>4</td></tr><tr><td>2025-12-25</td><td>1</td></tr><tr><td>2025-12-18</td><td>1</td></tr><tr><td>2025-12-11</td><td>3</td></tr><tr><td>2025-12-09</td><td>1</td></tr><tr><td>2025-12-03</td><td>1</td></tr><tr><td>2025-12-02</td><td>1</td></tr><tr><td>2025-11-28</td><td>1</td></tr><tr><td>2025-11-21</td><td>1</td></tr><tr><td>2025-11-19</td><td>1</td></tr><tr><td>2025-11-10</td><td>3</td></tr><tr><td>2025-11-06</td><td>5</td></tr><tr><td>2025-10-23</td><td>9</td></tr><tr><td>2025-10-15</td><td>5</td></tr><tr><td>2025-10-14</td><td>2</td></tr><tr><td>2025-10-10</td><td>1</td></tr><tr><td>2025-10-09</td><td>3</td></tr><tr><td>2025-09-22</td><td>3</td></tr><tr><td>2025-09-16</td><td>3</td></tr><tr><td>2025-09-04</td><td>3</td></tr><tr><td>2025-09-03</td><td>4</td></tr><tr><td>2025-07-25</td><td>1</td></tr><tr><td>2025-07-11</td><td>1</td></tr><tr><td>2025-06-27</td><td>2</td></tr><tr><td>2025-06-25</td><td>1</td></tr><tr><td>2025-06-20</td><td>1</td></tr><tr><td>2025-06-18</td><td>3</td></tr><tr><td>2025-06-12</td><td>4</td></tr><tr><td>2025-06-10</td><td>1</td></tr><tr><td>2025-06-06</td><td>1</td></tr><tr><td>2025-05-30</td><td>6</td></tr><tr><td>2025-05-21</td><td>5</td></tr><tr><td>2025-05-20</td><td>14</td></tr><tr><td>2025-05-19</td><td>7</td></tr><tr><td>2025-05-16</td><td>1</td></tr><tr><td>2025-05-12</td><td>10</td></tr><tr><td>2025-05-10</td><td>1</td></tr><tr><td>2025-05-09</td><td>2</td></tr><tr><td>2025-04-25</td><td>1</td></tr><tr><td>2025-04-23</td><td>2</td></tr><tr><td>2025-03-31</td><td>6</td></tr><tr><td>2025-03-21</td><td>2</td></tr><tr><td>2025-03-20</td><td>1</td></tr><tr><td>2025-03-17</td><td>3</td></tr><tr><td>2025-03-13</td><td>1</td></tr><tr><td>2025-03-10</td><td>4</td></tr><tr><td>2025-03-05</td><td>2</td></tr><tr><td>2025-02-25</td><td>2</td></tr><tr><td>2025-02-14</td><td>15</td></tr><tr><td>2025-02-12</td><td>10</td></tr><tr><td>2024-11-16</td><td>2</td></tr><tr><td>2024-08-02</td><td>1</td></tr><tr><td>2024-07-26</td><td>2</td></tr><tr><td>2024-07-23</td><td>5</td></tr><tr><td>2024-07-19</td><td>6</td></tr><tr><td>2024-07-17</td><td>4</td></tr><tr><td>2024-07-15</td><td>6</td></tr><tr><td>2024-07-13</td><td>2</td></tr><tr><td>2024-07-11</td><td>3</td></tr><tr><td>2024-07-03</td><td>1</td></tr><tr><td>2024-07-02</td><td>1</td></tr><tr><td>2024-07-01</td><td>1</td></tr><tr><td>2024-06-28</td><td>6</td></tr><tr><td>2024-06-26</td><td>1</td></tr><tr><td>2024-06-21</td><td>1</td></tr><tr><td>2024-06-15</td><td>1</td></tr><tr><td>2024-06-06</td><td>1</td></tr><tr><td>2024-06-05</td><td>3</td></tr><tr><td>2024-06-03</td><td>1</td></tr><tr><td>2024-05-31</td><td>1</td></tr><tr><td>2024-05-20</td><td>2</td></tr><tr><td>2024-05-17</td><td>1</td></tr><tr><td>2024-05-16</td><td>5</td></tr><tr><td>2024-05-15</td><td>9</td></tr><tr><td>2024-04-29</td><td>3</td></tr><tr><td>2024-04-26</td><td>2</td></tr><tr><td>2024-04-19</td><td>1</td></tr><tr><td>2024-04-16</td><td>1</td></tr><tr><td>2024-04-15</td><td>1</td></tr><tr><td>2024-04-13</td><td>2</td></tr><tr><td>2024-04-09</td><td>3</td></tr><tr><td>2024-04-08</td><td>2</td></tr><tr><td>2024-04-02</td><td>1</td></tr><tr><td>2024-03-29</td><td>2</td></tr><tr><td>2024-03-25</td><td>1</td></tr><tr><td>2024-03-22</td><td>3</td></tr><tr><td>2024-03-21</td><td>2</td></tr><tr><td>2024-03-20</td><td>7</td></tr><tr><td>2024-03-19</td><td>5</td></tr><tr><td>2024-03-18</td><td>9</td></tr><tr><td>2024-03-14</td><td>1</td></tr><tr><td>2024-03-13</td><td>2</td></tr><tr><td>2024-03-12</td><td>4</td></tr><tr><td>2024-03-11</td><td>9</td></tr><tr><td>2024-03-07</td><td>3</td></tr><tr><td>2024-03-05</td><td>4</td></tr><tr><td>2024-02-28</td><td>4</td></tr><tr><td>2024-02-25</td><td>1</td></tr><tr><td>2024-02-05</td><td>1</td></tr><tr><td>2024-02-04</td><td>2</td></tr><tr><td>2024-02-02</td><td>1</td></tr><tr><td>2024-02-01</td><td>4</td></tr><tr><td>2024-01-31</td><td>1</td></tr><tr><td>2024-01-24</td><td>1</td></tr><tr><td>2024-01-19</td><td>1</td></tr><tr><td>2024-01-16</td><td>4</td></tr><tr><td>2024-01-15</td><td>1</td></tr><tr><td>2024-01-12</td><td>3</td></tr><tr><td>2024-01-11</td><td>1</td></tr><tr><td>2024-01-08</td><td>1</td></tr><tr><td>2024-01-05</td><td>5</td></tr><tr><td>2023-12-16</td><td>1</td></tr><tr><td>2023-12-13</td><td>1</td></tr><tr><td>2023-12-06</td><td>6</td></tr><tr><td>2023-11-28</td><td>4</td></tr><tr><td>2023-11-24</td><td>2</td></tr><tr><td>2023-11-22</td><td>1</td></tr><tr><td>2023-11-17</td><td>3</td></tr><tr><td>2023-11-16</td><td>2</td></tr><tr><td>2023-11-02</td><td>1</td></tr><tr><td>2023-10-17</td><td>2</td></tr><tr><td>2023-10-09</td><td>2</td></tr><tr><td>2023-09-26</td><td>2</td></tr><tr><td>2023-09-25</td><td>2</td></tr><tr><td>2023-09-22</td><td>2</td></tr><tr><td>2023-09-21</td><td>3</td></tr><tr><td>2023-09-14</td><td>1</td></tr><tr><td>2023-08-22</td><td>1</td></tr><tr><td>2023-08-18</td><td>1</td></tr><tr><td>2023-06-28</td><td>1</td></tr><tr><td>2023-05-31</td><td>1</td></tr><tr><td>2023-05-26</td><td>1</td></tr><tr><td>2023-05-25</td><td>3</td></tr><tr><td>2023-05-23</td><td>1</td></tr><tr><td>2023-05-19</td><td>1</td></tr><tr><td>2023-05-17</td><td>2</td></tr><tr><td>2023-05-15</td><td>1</td></tr><tr><td>2023-05-12</td><td>3</td></tr><tr><td>2023-05-11</td><td>1</td></tr><tr><td>2023-05-09</td><td>1</td></tr><tr><td>2023-05-08</td><td>2</td></tr><tr><td>2023-05-06</td><td>5</td></tr><tr><td>2023-05-05</td><td>6</td></tr><tr><td>2023-05-04</td><td>2</td></tr><tr><td>2023-04-28</td><td>7</td></tr><tr><td>2023-04-26</td><td>10</td></tr><tr><td>2023-04-25</td><td>4</td></tr><tr><td>2023-04-24</td><td>12</td></tr><tr><td>2023-04-23</td><td>6</td></tr><tr><td>2023-04-21</td><td>1</td></tr><tr><td>2023-04-20</td><td>5</td></tr><tr><td>2023-04-19</td><td>17</td></tr><tr><td>2023-04-18</td><td>3</td></tr><tr><td>2023-04-17</td><td>4</td></tr><tr><td>2023-04-14</td><td>2</td></tr><tr><td>2023-04-11</td><td>1</td></tr><tr><td>2023-04-10</td><td>1</td></tr><tr><td>2023-04-07</td><td>1</td></tr><tr><td>2023-04-06</td><td>1</td></tr><tr><td>2023-03-27</td><td>1</td></tr><tr><td>2023-03-24</td><td>4</td></tr><tr><td>2023-03-23</td><td>2</td></tr><tr><td>2023-03-22</td><td>7</td></tr><tr><td>2023-03-21</td><td>14</td></tr><tr><td>2023-03-16</td><td>1</td></tr><tr><td>2023-03-15</td><td>1</td></tr><tr><td>2023-03-13</td><td>1</td></tr><tr><td>2023-03-09</td><td>5</td></tr><tr><td>2023-03-08</td><td>1</td></tr><tr><td>2023-03-03</td><td>2</td></tr><tr><td>2023-03-02</td><td>1</td></tr><tr><td>2023-02-27</td><td>1</td></tr><tr><td>2023-02-24</td><td>1</td></tr><tr><td>2023-02-22</td><td>5</td></tr><tr><td>2023-02-21</td><td>8</td></tr><tr><td>2023-02-20</td><td>17</td></tr><tr><td>2023-02-17</td><td>2</td></tr><tr><td>2023-02-16</td><td>30</td></tr><tr><td>2023-02-15</td><td>2</td></tr><tr><td>2023-02-10</td><td>2</td></tr><tr><td>2023-02-09</td><td>2</td></tr><tr><td>2023-02-07</td><td>2</td></tr><tr><td>2023-02-06</td><td>3</td></tr><tr><td>2023-02-03</td><td>4</td></tr><tr><td>2023-02-02</td><td>1</td></tr><tr><td>2023-02-01</td><td>4</td></tr><tr><td>2023-01-12</td><td>4</td></tr><tr><td>2023-01-10</td><td>1</td></tr><tr><td>2023-01-09</td><td>3</td></tr><tr><td>2023-01-06</td><td>1</td></tr><tr><td>2023-01-05</td><td>6</td></tr><tr><td>2023-01-04</td><td>3</td></tr><tr><td>2023-01-03</td><td>1</td></tr><tr><td>2022-12-29</td><td>5</td></tr><tr><td>2022-12-28</td><td>1</td></tr><tr><td>2022-12-22</td><td>4</td></tr><tr><td>2022-12-21</td><td>1</td></tr><tr><td>2022-12-19</td><td>13</td></tr><tr><td>2022-12-08</td><td>8</td></tr><tr><td>2022-12-06</td><td>3</td></tr><tr><td>2022-12-02</td><td>4</td></tr><tr><td>2022-12-01</td><td>1</td></tr><tr><td>2022-11-25</td><td>2</td></tr><tr><td>2022-11-23</td><td>7</td></tr><tr><td>2022-11-22</td><td>3</td></tr><tr><td>2022-11-18</td><td>3</td></tr><tr><td>2022-11-17</td><td>3</td></tr><tr><td>2022-11-16</td><td>1</td></tr><tr><td>2022-11-15</td><td>2</td></tr><tr><td>2022-11-14</td><td>4</td></tr><tr><td>2022-11-11</td><td>2</td></tr><tr><td>2022-11-07</td><td>8</td></tr><tr><td>2022-11-04</td><td>1</td></tr><tr><td>2022-11-03</td><td>1</td></tr><tr><td>2022-11-02</td><td>2</td></tr><tr><td>2022-10-28</td><td>1</td></tr><tr><td>2022-10-26</td><td>3</td></tr><tr><td>2022-10-25</td><td>1</td></tr><tr><td>2022-10-08</td><td>1</td></tr><tr><td>2022-09-29</td><td>1</td></tr><tr><td>2022-09-20</td><td>4</td></tr><tr><td>2022-09-07</td><td>1</td></tr><tr><td>2022-09-06</td><td>4</td></tr><tr><td>2022-09-05</td><td>1</td></tr><tr><td>2022-09-01</td><td>1</td></tr><tr><td>2022-08-31</td><td>2</td></tr><tr><td>2022-08-19</td><td>3</td></tr><tr><td>2022-08-16</td><td>1</td></tr><tr><td>2022-08-08</td><td>2</td></tr><tr><td>2022-08-03</td><td>3</td></tr><tr><td>2022-07-14</td><td>1</td></tr><tr><td>2022-07-13</td><td>2</td></tr><tr><td>2022-07-12</td><td>3</td></tr><tr><td>2022-07-08</td><td>1</td></tr><tr><td>2022-07-07</td><td>5</td></tr><tr><td>2022-07-06</td><td>1</td></tr><tr><td>2022-07-04</td><td>1</td></tr><tr><td>2022-06-29</td><td>1</td></tr><tr><td>2022-06-28</td><td>2</td></tr><tr><td>2022-06-27</td><td>1</td></tr><tr><td>2022-06-24</td><td>1</td></tr><tr><td>2022-06-21</td><td>1</td></tr><tr><td>2022-06-17</td><td>1</td></tr><tr><td>2022-06-16</td><td>1</td></tr><tr><td>2022-06-02</td><td>1</td></tr><tr><td>2022-06-01</td><td>4</td></tr><tr><td>2022-05-31</td><td>6</td></tr><tr><td>2022-05-30</td><td>20</td></tr><tr><td>2022-05-27</td><td>3</td></tr><tr><td>2022-05-26</td><td>12</td></tr><tr><td>2022-05-25</td><td>14</td></tr><tr><td>2022-05-24</td><td>1</td></tr><tr><td>2022-05-23</td><td>2</td></tr><tr><td>2022-05-20</td><td>2</td></tr><tr><td>2022-05-19</td><td>1</td></tr><tr><td>2022-05-18</td><td>1</td></tr><tr><td>2022-05-17</td><td>6</td></tr><tr><td>2022-05-16</td><td>18</td></tr><tr><td>2022-05-13</td><td>10</td></tr><tr><td>2022-05-12</td><td>11</td></tr><tr><td>2022-05-11</td><td>7</td></tr><tr><td>2022-05-10</td><td>7</td></tr><tr><td>2022-05-09</td><td>9</td></tr><tr><td>2022-05-07</td><td>9</td></tr><tr><td>2022-05-05</td><td>4</td></tr><tr><td>2022-04-29</td><td>11</td></tr><tr><td>2022-04-28</td><td>13</td></tr><tr><td>2022-04-27</td><td>1</td></tr><tr><td>2022-04-26</td><td>1</td></tr><tr><td>2022-04-20</td><td>1</td></tr><tr><td>2022-04-18</td><td>4</td></tr><tr><td>2022-04-15</td><td>26</td></tr><tr><td>2022-04-14</td><td>17</td></tr><tr><td>2022-04-13</td><td>7</td></tr><tr><td>2022-04-12</td><td>9</td></tr><tr><td>2022-04-11</td><td>2</td></tr><tr><td>2022-04-08</td><td>2</td></tr><tr><td>2022-04-07</td><td>1</td></tr><tr><td>2022-03-31</td><td>2</td></tr><tr><td>2022-03-30</td><td>3</td></tr><tr><td>2022-03-29</td><td>7</td></tr><tr><td>2022-03-28</td><td>6</td></tr><tr><td>2022-03-25</td><td>6</td></tr><tr><td>2022-03-22</td><td>1</td></tr><tr><td>2022-03-21</td><td>13</td></tr><tr><td>2022-03-18</td><td>1</td></tr><tr><td>2022-03-17</td><td>4</td></tr><tr><td>2022-03-15</td><td>5</td></tr><tr><td>2022-03-14</td><td>5</td></tr><tr><td>2022-03-11</td><td>7</td></tr><tr><td>2022-03-10</td><td>8</td></tr><tr><td>2022-03-09</td><td>11</td></tr><tr><td>2022-03-08</td><td>6</td></tr><tr><td>2022-03-07</td><td>11</td></tr><tr><td>2022-03-04</td><td>5</td></tr><tr><td>2022-03-03</td><td>7</td></tr><tr><td>2022-03-02</td><td>4</td></tr><tr><td>2022-03-01</td><td>6</td></tr><tr><td>2022-02-28</td><td>1</td></tr><tr><td>2022-02-25</td><td>6</td></tr><tr><td>2022-02-24</td><td>5</td></tr><tr><td>2022-02-23</td><td>5</td></tr><tr><td>2022-02-22</td><td>13</td></tr><tr><td>2022-02-18</td><td>5</td></tr><tr><td>2022-02-17</td><td>9</td></tr><tr><td>2022-02-16</td><td>4</td></tr><tr><td>2022-02-15</td><td>3</td></tr><tr><td>2022-02-14</td><td>7</td></tr><tr><td>2022-02-13</td><td>7</td></tr><tr><td>2022-02-11</td><td>10</td></tr><tr><td>2022-02-10</td><td>3</td></tr><tr><td>2022-02-08</td><td>1</td></tr><tr><td>2022-02-07</td><td>4</td></tr><tr><td>2022-01-27</td><td>1</td></tr><tr><td>2022-01-25</td><td>2</td></tr><tr><td>2022-01-24</td><td>24</td></tr><tr><td>2022-01-22</td><td>2</td></tr><tr><td>2022-01-21</td><td>2</td></tr><tr><td>2022-01-20</td><td>13</td></tr><tr><td>2022-01-19</td><td>13</td></tr><tr><td>2022-01-18</td><td>13</td></tr><tr><td>2022-01-17</td><td>12</td></tr><tr><td>2022-01-16</td><td>2</td></tr><tr><td>2022-01-14</td><td>10</td></tr><tr><td>2022-01-13</td><td>28</td></tr><tr><td>2022-01-12</td><td>13</td></tr><tr><td>2022-01-11</td><td>3</td></tr><tr><td>2022-01-10</td><td>1</td></tr><tr><td>2022-01-07</td><td>1</td></tr><tr><td>2022-01-06</td><td>6</td></tr><tr><td>2022-01-05</td><td>6</td></tr><tr><td>2022-01-04</td><td>17</td></tr><tr><td>2021-12-31</td><td>29</td></tr><tr><td>2021-12-30</td><td>2</td></tr><tr><td>2021-12-28</td><td>2</td></tr><tr><td>2021-12-27</td><td>1</td></tr><tr><td>2021-12-24</td><td>3</td></tr><tr><td>2021-12-23</td><td>2</td></tr><tr><td>2021-12-22</td><td>3</td></tr><tr><td>2021-12-20</td><td>16</td></tr><tr><td>2021-12-17</td><td>26</td></tr><tr><td>2021-12-16</td><td>3</td></tr><tr><td>2021-12-15</td><td>3</td></tr><tr><td>2021-12-14</td><td>4</td></tr><tr><td>2021-12-13</td><td>1</td></tr><tr><td>2021-12-10</td><td>3</td></tr><tr><td>2021-12-08</td><td>6</td></tr><tr><td>2021-12-07</td><td>5</td></tr><tr><td>2021-12-06</td><td>14</td></tr><tr><td>2021-12-02</td><td>1</td></tr><tr><td>2021-12-01</td><td>2</td></tr><tr><td>2021-11-29</td><td>3</td></tr><tr><td>2021-11-28</td><td>4</td></tr><tr><td>2021-11-27</td><td>3</td></tr><tr><td>2021-11-26</td><td>8</td></tr><tr><td>2021-11-25</td><td>8</td></tr><tr><td>2021-11-24</td><td>3</td></tr><tr><td>2021-11-22</td><td>1</td></tr><tr><td>2021-11-18</td><td>1</td></tr><tr><td>2021-11-17</td><td>3</td></tr><tr><td>2021-11-16</td><td>4</td></tr><tr><td>2021-11-15</td><td>3</td></tr><tr><td>2021-11-12</td><td>4</td></tr><tr><td>2021-11-10</td><td>3</td></tr><tr><td>2021-11-08</td><td>2</td></tr><tr><td>2021-11-05</td><td>8</td></tr><tr><td>2021-11-04</td><td>11</td></tr><tr><td>2021-11-03</td><td>2</td></tr><tr><td>2021-11-02</td><td>1</td></tr><tr><td>2021-10-29</td><td>1</td></tr><tr><td>2021-10-22</td><td>5</td></tr><tr><td>2021-10-21</td><td>10</td></tr><tr><td>2021-10-19</td><td>1</td></tr><tr><td>2021-10-18</td><td>1</td></tr><tr><td>2021-10-14</td><td>10</td></tr><tr><td>2021-10-13</td><td>1</td></tr><tr><td>2021-09-18</td><td>2</td></tr><tr><td>2021-09-17</td><td>8</td></tr></tbody></table></div></div></div></div></td>
                                   <td valign="top"><div><div id="tableDataWeek" class="ivu-card ivu-card-dis-hover ivu-card-shadow"><div class="ivu-card-head">按周统计</div><div class="ivu-card-body tstable-container tstable-normal border tstable-no-fixedHeader block-large"><div class="tstable-main bg-op"><table tableName="按周统计" class="table-main tstable-body"><thead><tr class="th-left"><th>周</th><th>工单数量</th></tr></thead><tbody class="tbody-main"><tr><td>2026-13</td><td>5</td></tr><tr><td>2026-12</td><td>8</td></tr><tr><td>2026-11</td><td>31</td></tr><tr><td>2026-09</td><td>8</td></tr><tr><td>2026-06</td><td>11</td></tr><tr><td>2026-05</td><td>1</td></tr><tr><td>2026-02</td><td>4</td></tr><tr><td>2026-01</td><td>1</td></tr><tr><td>2025-52</td><td>5</td></tr><tr><td>2025-51</td><td>1</td></tr><tr><td>2025-50</td><td>4</td></tr><tr><td>2025-49</td><td>2</td></tr><tr><td>2025-48</td><td>1</td></tr><tr><td>2025-47</td><td>2</td></tr><tr><td>2025-46</td><td>3</td></tr><tr><td>2025-45</td><td>5</td></tr><tr><td>2025-43</td><td>9</td></tr><tr><td>2025-42</td><td>7</td></tr><tr><td>2025-41</td><td>4</td></tr><tr><td>2025-39</td><td>3</td></tr><tr><td>2025-38</td><td>3</td></tr><tr><td>2025-36</td><td>7</td></tr><tr><td>2025-30</td><td>1</td></tr><tr><td>2025-28</td><td>1</td></tr><tr><td>2025-26</td><td>3</td></tr><tr><td>2025-25</td><td>4</td></tr><tr><td>2025-24</td><td>5</td></tr><tr><td>2025-23</td><td>1</td></tr><tr><td>2025-22</td><td>6</td></tr><tr><td>2025-21</td><td>26</td></tr><tr><td>2025-20</td><td>11</td></tr><tr><td>2025-19</td><td>3</td></tr><tr><td>2025-17</td><td>3</td></tr><tr><td>2025-14</td><td>6</td></tr><tr><td>2025-12</td><td>6</td></tr><tr><td>2025-11</td><td>5</td></tr><tr><td>2025-10</td><td>2</td></tr><tr><td>2025-09</td><td>2</td></tr><tr><td>2025-07</td><td>25</td></tr><tr><td>2024-46</td><td>2</td></tr><tr><td>2024-31</td><td>1</td></tr><tr><td>2024-30</td><td>7</td></tr><tr><td>2024-29</td><td>16</td></tr><tr><td>2024-28</td><td>5</td></tr><tr><td>2024-27</td><td>3</td></tr><tr><td>2024-26</td><td>7</td></tr><tr><td>2024-25</td><td>1</td></tr><tr><td>2024-24</td><td>1</td></tr><tr><td>2024-23</td><td>5</td></tr><tr><td>2024-22</td><td>1</td></tr><tr><td>2024-21</td><td>2</td></tr><tr><td>2024-20</td><td>15</td></tr><tr><td>2024-18</td><td>3</td></tr><tr><td>2024-17</td><td>2</td></tr><tr><td>2024-16</td><td>3</td></tr><tr><td>2024-15</td><td>7</td></tr><tr><td>2024-14</td><td>1</td></tr><tr><td>2024-13</td><td>3</td></tr><tr><td>2024-12</td><td>26</td></tr><tr><td>2024-11</td><td>16</td></tr><tr><td>2024-10</td><td>7</td></tr><tr><td>2024-09</td><td>4</td></tr><tr><td>2024-08</td><td>1</td></tr><tr><td>2024-06</td><td>1</td></tr><tr><td>2024-05</td><td>8</td></tr><tr><td>2024-04</td><td>1</td></tr><tr><td>2024-03</td><td>6</td></tr><tr><td>2024-02</td><td>5</td></tr><tr><td>2024-01</td><td>5</td></tr><tr><td>2023-50</td><td>2</td></tr><tr><td>2023-49</td><td>6</td></tr><tr><td>2023-48</td><td>4</td></tr><tr><td>2023-47</td><td>3</td></tr><tr><td>2023-46</td><td>5</td></tr><tr><td>2023-44</td><td>1</td></tr><tr><td>2023-42</td><td>2</td></tr><tr><td>2023-41</td><td>2</td></tr><tr><td>2023-39</td><td>4</td></tr><tr><td>2023-38</td><td>5</td></tr><tr><td>2023-37</td><td>1</td></tr><tr><td>2023-34</td><td>1</td></tr><tr><td>2023-33</td><td>1</td></tr><tr><td>2023-26</td><td>1</td></tr><tr><td>2023-22</td><td>1</td></tr><tr><td>2023-21</td><td>5</td></tr><tr><td>2023-20</td><td>4</td></tr><tr><td>2023-19</td><td>7</td></tr><tr><td>2023-18</td><td>13</td></tr><tr><td>2023-17</td><td>33</td></tr><tr><td>2023-16</td><td>36</td></tr><tr><td>2023-15</td><td>4</td></tr><tr><td>2023-14</td><td>2</td></tr><tr><td>2023-13</td><td>1</td></tr><tr><td>2023-12</td><td>27</td></tr><tr><td>2023-11</td><td>3</td></tr><tr><td>2023-10</td><td>6</td></tr><tr><td>2023-09</td><td>4</td></tr><tr><td>2023-08</td><td>31</td></tr><tr><td>2023-07</td><td>34</td></tr><tr><td>2023-06</td><td>9</td></tr><tr><td>2023-05</td><td>9</td></tr><tr><td>2023-02</td><td>8</td></tr><tr><td>2023-01</td><td>11</td></tr><tr><td>2022-52</td><td>6</td></tr><tr><td>2022-51</td><td>18</td></tr><tr><td>2022-49</td><td>11</td></tr><tr><td>2022-48</td><td>5</td></tr><tr><td>2022-47</td><td>12</td></tr><tr><td>2022-46</td><td>13</td></tr><tr><td>2022-45</td><td>10</td></tr><tr><td>2022-44</td><td>4</td></tr><tr><td>2022-43</td><td>5</td></tr><tr><td>2022-40</td><td>1</td></tr><tr><td>2022-39</td><td>1</td></tr><tr><td>2022-38</td><td>4</td></tr><tr><td>2022-36</td><td>6</td></tr><tr><td>2022-35</td><td>3</td></tr><tr><td>2022-33</td><td>4</td></tr><tr><td>2022-32</td><td>2</td></tr><tr><td>2022-31</td><td>3</td></tr><tr><td>2022-28</td><td>6</td></tr><tr><td>2022-27</td><td>8</td></tr><tr><td>2022-26</td><td>4</td></tr><tr><td>2022-25</td><td>2</td></tr><tr><td>2022-24</td><td>2</td></tr><tr><td>2022-22</td><td>31</td></tr><tr><td>2022-21</td><td>32</td></tr><tr><td>2022-20</td><td>28</td></tr><tr><td>2022-19</td><td>44</td></tr><tr><td>2022-18</td><td>13</td></tr><tr><td>2022-17</td><td>26</td></tr><tr><td>2022-16</td><td>5</td></tr><tr><td>2022-15</td><td>61</td></tr><tr><td>2022-14</td><td>3</td></tr><tr><td>2022-13</td><td>18</td></tr><tr><td>2022-12</td><td>20</td></tr><tr><td>2022-11</td><td>15</td></tr><tr><td>2022-10</td><td>43</td></tr><tr><td>2022-09</td><td>23</td></tr><tr><td>2022-08</td><td>29</td></tr><tr><td>2022-07</td><td>28</td></tr><tr><td>2022-06</td><td>25</td></tr><tr><td>2022-04</td><td>27</td></tr><tr><td>2022-03</td><td>55</td></tr><tr><td>2022-02</td><td>57</td></tr><tr><td>2022-01</td><td>30</td></tr><tr><td>2021-52</td><td>34</td></tr><tr><td>2021-51</td><td>24</td></tr><tr><td>2021-50</td><td>37</td></tr><tr><td>2021-49</td><td>28</td></tr><tr><td>2021-48</td><td>6</td></tr><tr><td>2021-47</td><td>27</td></tr><tr><td>2021-46</td><td>11</td></tr><tr><td>2021-45</td><td>9</td></tr><tr><td>2021-44</td><td>22</td></tr><tr><td>2021-43</td><td>1</td></tr><tr><td>2021-42</td><td>17</td></tr><tr><td>2021-41</td><td>11</td></tr><tr><td>2021-37</td><td>10</td></tr></tbody></table></div></div></div></div></td>
                                   <td valign="top"><div><div id="tableDataMonth" class="ivu-card ivu-card-dis-hover ivu-card-shadow"><div class="ivu-card-head">按月统计</div><div class="ivu-card-body tstable-container tstable-normal border tstable-no-fixedHeader block-large"><div class="tstable-main bg-op"><table tableName="按月统计" class="table-main tstable-body"><thead><tr class="th-left"><th>月</th><th>工单数量</th></tr></thead><tbody class="tbody-main"><tr><td>2026-03</td><td>44</td></tr><tr><td>2026-02</td><td>19</td></tr><tr><td>2026-01</td><td>6</td></tr><tr><td>2025-12</td><td>12</td></tr><tr><td>2025-11</td><td>11</td></tr><tr><td>2025-10</td><td>20</td></tr><tr><td>2025-09</td><td>13</td></tr><tr><td>2025-07</td><td>2</td></tr><tr><td>2025-06</td><td>13</td></tr><tr><td>2025-05</td><td>46</td></tr><tr><td>2025-04</td><td>3</td></tr><tr><td>2025-03</td><td>19</td></tr><tr><td>2025-02</td><td>27</td></tr><tr><td>2024-11</td><td>2</td></tr><tr><td>2024-08</td><td>1</td></tr><tr><td>2024-07</td><td>31</td></tr><tr><td>2024-06</td><td>14</td></tr><tr><td>2024-05</td><td>18</td></tr><tr><td>2024-04</td><td>16</td></tr><tr><td>2024-03</td><td>52</td></tr><tr><td>2024-02</td><td>13</td></tr><tr><td>2024-01</td><td>18</td></tr><tr><td>2023-12</td><td>8</td></tr><tr><td>2023-11</td><td>13</td></tr><tr><td>2023-10</td><td>4</td></tr><tr><td>2023-09</td><td>10</td></tr><tr><td>2023-08</td><td>2</td></tr><tr><td>2023-06</td><td>1</td></tr><tr><td>2023-05</td><td>30</td></tr><tr><td>2023-04</td><td>75</td></tr><tr><td>2023-03</td><td>40</td></tr><tr><td>2023-02</td><td>84</td></tr><tr><td>2023-01</td><td>19</td></tr><tr><td>2022-12</td><td>40</td></tr><tr><td>2022-11</td><td>39</td></tr><tr><td>2022-10</td><td>6</td></tr><tr><td>2022-09</td><td>12</td></tr><tr><td>2022-08</td><td>11</td></tr><tr><td>2022-07</td><td>14</td></tr><tr><td>2022-06</td><td>13</td></tr><tr><td>2022-05</td><td>143</td></tr><tr><td>2022-04</td><td>95</td></tr><tr><td>2022-03</td><td>118</td></tr><tr><td>2022-02</td><td>83</td></tr><tr><td>2022-01</td><td>169</td></tr><tr><td>2021-12</td><td>126</td></tr><tr><td>2021-11</td><td>72</td></tr><tr><td>2021-10</td><td>29</td></tr><tr><td>2021-09</td><td>10</td></tr></tbody></table></div></div></div></div></td>
                                </tr>
                              </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                </body>
                </html>
                """;

        String html2 = """
                <!DOCTYPE html>
                <html>
                <head lang="en">
                    <meta charset="UTF-8"/>
                    <style>
                                body {
                            font-family: "SimSun";
                        }
                                
                        .innerTable {
                            width: 100%;
                            text-align: center;
                            border-collapse: collapse;
                        }
                                
                        .innerTable th, .innerTable td {
                            border: 1px solid #000;
                            font-weight: normal;
                        }
                                
                        .lineTd {
                            text-align: left;
                            word-break: break-all;
                            word-wrap: break-word;
                        }
                                
                        .text-success {
                            color: #25b865;
                        }
                                
                        .text-warning {
                            color: #f9a825;
                        }
                                
                        .text-error {
                            color: #f71010;
                        }
                                
                        .bg-error {
                            color: #f71010;
                        }
                                
                        .title {
                            font-size: 20px;
                            text-align: center;
                            font-weight: bold;
                        }
                                
                        .userAndTime {
                            text-align: center;
                        }
                    </style>
                </head>
                                
                <body>
                    <p class="title">1067868140453893_巡检报告</p>
                    <p class="userAndTime"><span>管理员</span>&nbsp;<span></span></p>
                <table class="innerTable">
                    <tbody>
                            <tr>
                                <td>告警</td>
                                <td>
                                    <table class="innerTable">
                                        <thead>
                                        <tr>
                                                <th>告警级别</th>
                                                <th>告警字段</th>
                                                <th>告警提示</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                            <tr class="text-error">
                                                    <td>
                                                        严重
                                                    </td>
                                                    <td>
                                                        可用性
                                                    </td>
                                                    <td>
                                                        可用性告警
                                                    </td>
                                            </tr>
                                            <tr class="text-error">
                                                    <td>
                                                        严重
                                                    </td>
                                                    <td>
                                                        错误信息
                                                    </td>
                                                    <td>
                                                        错误信息非空
                                                    </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                                
                            <tr>
                                <td>管理IP</td>
                                <td class="lineTd ">192.168.0.101</td>
                            </tr>
                            <tr>
                                <td>可用性</td>
                                <td class="lineTd  text-error ">false</td>
                            </tr>
                            <tr>
                                <td>响应时间</td>
                                <td class="lineTd ">0.027</td>
                            </tr>
                            <tr>
                                <td>错误信息</td>
                                <td class="lineTd  text-error ">'ERROR: Connect to 192.168.0.101:3939 failed, [Errno 111] Connection refused.'</td>
                            </tr>
                            <tr>
                                <td>监听端口</td>
                                <td class="lineTd ">暂无数据</td>
                            </tr>
                            <tr>
                                <td>自定义巡检结果</td>
                                <td class="lineTd ">暂无数据</td>
                            </tr>
                                
                    </tbody>
                </table>
                </body>
                </html>
                                
                                
                                
                """;
        {
            ITextRenderer renderer = new ITextRenderer();
            ChineseFont[] fonts = ChineseFont.values();
            for (ChineseFont font : fonts) {
                renderer.getFontResolver().addFont(font.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
            renderer.setDocumentFromString(html1);
            renderer.layout();
            File file = new File("测试导出PDF1.pdf");
            if (file.exists()) {
                file.delete();
            }
            boolean newFile = file.createNewFile();
            if (newFile) {
                System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
                ;
            }
            FileOutputStream fos = new FileOutputStream(file);
            renderer.createPDF(fos);
        }
        {
            ITextRenderer renderer = new ITextRenderer();
            ChineseFont[] fonts = ChineseFont.values();
            for (ChineseFont font : fonts) {
                renderer.getFontResolver().addFont(font.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
            renderer.setDocumentFromString(html2);
            renderer.layout();
            File file = new File("测试导出PDF2.pdf");
            if (file.exists()) {
                file.delete();
            }
            boolean newFile = file.createNewFile();
            if (newFile) {
                System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
                ;
            }
            FileOutputStream fos = new FileOutputStream(file);
            renderer.createPDF(fos);
        }
    }
}
