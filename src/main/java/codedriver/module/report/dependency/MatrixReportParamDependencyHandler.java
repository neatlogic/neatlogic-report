/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.dependency;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.dependency.constvalue.FromType;
import codedriver.framework.dependency.core.CustomTableDependencyHandlerBase;
import codedriver.framework.dependency.core.IFromType;
import codedriver.framework.dependency.dto.DependencyInfoVo;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportParamVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 报表引用矩阵处理器
 *
 * @author: laiwt
 * @since: 2021/11/3 11:42
 **/
@Service
public class MatrixReportParamDependencyHandler extends CustomTableDependencyHandlerBase {

    @Resource
    private ReportMapper reportMapper;

    /**
     * 表名
     *
     * @return
     */
    @Override
    protected String getTableName() {
        return null;
    }

    /**
     * 被引用者（上游）字段
     *
     * @return
     */
    @Override
    protected String getFromField() {
        return null;
    }

    /**
     * 引用者（下游）字段
     *
     * @return
     */
    @Override
    protected String getToField() {
        return null;
    }

    @Override
    protected List<String> getToFieldList() {
        return null;
    }

    /**
     * 报表参数与矩阵之间的引用关系有自己的添加和删除方式
     *
     * @param from 被引用者（上游）值（如：服务时间窗口uuid）
     * @param to 引用者（下游）值（如：服务uuid）
     * @return
     */
    @Override
    public int insert(Object from, Object to) {
        return 0;
    }

    /**
     * 报表参数与矩阵之间的引用关系有自己的添加和删除方式
     *
     * @param to 引用者（下游）值（如：服务uuid）
     * @return
     */
    @Override
    public int delete(Object to) {
        return 0;
    }

    /**
     * 解析数据，拼装跳转url，返回引用下拉列表一个选项数据结构
     *
     * @param dependencyObj 引用关系数据
     * @return
     */
    @Override
    protected DependencyInfoVo parse(Object dependencyObj) {
        if (dependencyObj == null) {
            return null;
        }
        if (dependencyObj instanceof ReportParamVo) {
            ReportParamVo reportParamVo = (ReportParamVo) dependencyObj;
            JSONObject dependencyInfoConfig = new JSONObject();
            dependencyInfoConfig.put("reportId", reportParamVo.getReportId());
            dependencyInfoConfig.put("reportName", reportParamVo.getReportName());
            dependencyInfoConfig.put("paramName", reportParamVo.getName());
            String pathFormat = "报表-${DATA.reportName}-${DATA.paramName}";
            String urlFormat = "/" + TenantContext.get().getTenantUuid() + "/report.html#/report-manage";
            return new DependencyInfoVo(reportParamVo.getReportId(), dependencyInfoConfig, pathFormat, urlFormat);
//            DependencyInfoVo dependencyInfoVo = new DependencyInfoVo();
//            dependencyInfoVo.setValue(reportParamVo.getReportId());
//            String text = String.format("<a href=\"/%s/report.html#/report-manage\" target=\"_blank\">%s</a>",
//                    TenantContext.get().getTenantUuid(), reportParamVo.getReportName() + "-" + reportParamVo.getName());
//            dependencyInfoVo.setText(text);
//            return dependencyInfoVo;
        }
        return null;
    }

    /**
     * 被引用者（上游）类型
     *
     * @return
     */
    @Override
    public IFromType getFromType() {
        return FromType.MATRIX;
    }

    /**
     * 查询引用列表数据
     *
     * @param from   被引用者（上游）值（如：服务时间窗口uuid）
     * @param startNum 开始行号
     * @param pageSize 每页条数
     * @return
     */
    @Override
    public List<DependencyInfoVo> getDependencyList(Object from, int startNum, int pageSize) {
        List<DependencyInfoVo> resultList = new ArrayList<>();
        List<ReportParamVo> callerList = reportMapper.getReportParamByMatrixUuid((String) from, startNum, pageSize);
        for (ReportParamVo caller : callerList) {
            DependencyInfoVo dependencyInfoVo = parse(caller);
            if (dependencyInfoVo != null) {
                resultList.add(dependencyInfoVo);
            }
        }
        return resultList;
    }

    @Override
    public int getDependencyCount(Object from) {
        return reportMapper.getReportParamCountByMatrixUuid((String) from);
    }
}
