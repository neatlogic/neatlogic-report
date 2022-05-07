/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.startup.handler;

import codedriver.framework.startup.StartupBase;
import codedriver.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ResetDatasourceJobStatusStartupHandler extends StartupBase {

    @Resource
    private DataWarehouseDataSourceMapper reportDataSourceMapper;

    /**
     * 作业名称
     *
     * @return 字符串
     */
    @Override
    public String getName() {
        return "创建资源中心视图";
    }

    /**
     * 每个租户分别执行
     */
    @Override
    public void executeForCurrentTenant() {
        reportDataSourceMapper.resetReportDataSourceStatus();
    }

    @Override
    public void executeForAllTenant() {

    }

    /**
     * 排序
     *
     * @return 顺序
     */
    @Override
    public int sort() {
        return 4;
    }
}
