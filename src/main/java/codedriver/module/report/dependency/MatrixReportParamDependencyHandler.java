/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.dependency;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dependency.constvalue.CalleeType;
import codedriver.framework.dependency.core.DependencyHandlerBase;
import codedriver.framework.dependency.core.ICalleeType;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportParamVo;
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
public class MatrixReportParamDependencyHandler extends DependencyHandlerBase {

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
     * 被调用者字段
     *
     * @return
     */
    @Override
    protected String getCalleeField() {
        return null;
    }

    /**
     * 调用者字段
     *
     * @return
     */
    @Override
    protected String getCallerField() {
        return null;
    }

    @Override
    protected List<String> getCallerFieldList() {
        return null;
    }

    /**
     * 报表参数与矩阵之间的引用关系有自己的添加和删除方式
     *
     * @param callee 被调用者值（如：服务时间窗口uuid）
     * @param caller 调用者值（如：服务uuid）
     * @return
     */
    @Override
    public int insert(Object callee, Object caller) {
        return 0;
    }

    /**
     * 报表参数与矩阵之间的引用关系有自己的添加和删除方式
     *
     * @param caller 调用者值（如：服务uuid）
     * @return
     */
    @Override
    public int delete(Object caller) {
        return 0;
    }

    /**
     * 解析数据，拼装跳转url，返回引用下拉列表一个选项数据结构
     *
     * @param caller 调用者值
     * @return
     */
    @Override
    protected ValueTextVo parse(Object caller) {
        if (caller == null) {
            return null;
        }
        if (caller instanceof ReportParamVo) {
            ReportParamVo reportParamVo = (ReportParamVo) caller;
            ValueTextVo valueTextVo = new ValueTextVo();
            valueTextVo.setValue(reportParamVo.getReportId());
            String text = String.format("<a href=\"/%s/report.html#/report-manage\" target=\"_blank\">%s</a>",
                    TenantContext.get().getTenantUuid(), reportParamVo.getReportName() + "-" + reportParamVo.getName());
            valueTextVo.setText(text);
            return valueTextVo;
        }
        return null;
    }

    /**
     * 被调用方名
     *
     * @return
     */
    @Override
    public ICalleeType getCalleeType() {
        return CalleeType.MATRIX;
    }

    /**
     * 查询引用列表数据
     *
     * @param callee   被调用者值（如：服务时间窗口uuid）
     * @param startNum 开始行号
     * @param pageSize 每页条数
     * @return
     */
    @Override
    public List<ValueTextVo> getCallerList(Object callee, int startNum, int pageSize) {
        List<ValueTextVo> resultList = new ArrayList<>();
        List<ReportParamVo> callerList = reportMapper.getReportParamByMatrixUuid((String) callee, startNum, pageSize);
        for (ReportParamVo caller : callerList) {
            ValueTextVo valueTextVo = parse(caller);
            if (valueTextVo != null) {
                resultList.add(valueTextVo);
            }
        }
        return resultList;
    }

    @Override
    public int getCallerCount(Object callee) {
        return reportMapper.getReportParamCountByMatrixUuid((String) callee);
    }
}
