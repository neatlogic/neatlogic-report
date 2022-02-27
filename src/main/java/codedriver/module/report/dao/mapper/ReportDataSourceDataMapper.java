/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.dao.mapper;

import codedriver.framework.report.dto.ReportDataSourceDataVo;
import codedriver.framework.report.dto.ReportDataSourceVo;

public interface ReportDataSourceDataMapper {
    int getDataSourceDataCount(ReportDataSourceVo reportDataSourceVo);

    void insertDataSourceData(ReportDataSourceDataVo reportDataSourceDataVo);

    void truncateTable(ReportDataSourceVo reportDataSourceVo);
}
