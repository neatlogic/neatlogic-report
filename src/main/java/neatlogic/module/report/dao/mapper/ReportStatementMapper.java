/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.report.dao.mapper;

import neatlogic.framework.report.dto.ReportStatementVo;

import java.util.List;

public interface ReportStatementMapper {
    int searchReportStatementCount(ReportStatementVo reportStatementVo);

    List<ReportStatementVo> searchReportStatement(ReportStatementVo reportStatementVo);

    ReportStatementVo getReportStatementById(Long id);

    void insertReportStatement(ReportStatementVo reportStatementVo);

    void updateReportStatement(ReportStatementVo reportStatementVo);

    void updateReportStatementActive(ReportStatementVo reportStatementVo);

    void deleteStatementById(Long id);
}
