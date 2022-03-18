/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.dto;

import org.apache.commons.lang3.StringUtils;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class ReportTypeVo {
    @EntityField(name = "分类唯一标识", type = ApiParamType.STRING)
    private String name;
    @EntityField(name = "分类名称", type = ApiParamType.STRING)
    private String label;
    @EntityField(name = "报表数量", type = ApiParamType.INTEGER)
    private int reportCount;

    public String getName() {
        //name不能为null，主要是为了前端能正确回选数据
        if (name == null) {
            name = "";
        }
        return name;
    }

    public String getLabel() {
        if (StringUtils.isBlank(label)) {
            label = "未分类";
        }
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getReportCount() {
        return reportCount;
    }

    public void setReportCount(int reportCount) {
        this.reportCount = reportCount;
    }
}
