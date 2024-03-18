/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.report.startup.handler;

import neatlogic.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import neatlogic.framework.startup.StartupBase;
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
    public int executeForCurrentTenant() {
        reportDataSourceMapper.resetReportDataSourceStatus();
        return 0;
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
