/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.dto;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.restful.annotation.EntityField;
import codedriver.framework.util.SnowflakeUtil;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ReportVo extends BasePageVo {
    @EntityField(name = "id", type = ApiParamType.LONG)
    private Long id;
    @EntityField(name = "名称", type = ApiParamType.STRING)
    private String name;
    @EntityField(name = "类型", type = ApiParamType.STRING)
    private String type;
    @EntityField(name = "sql配置内容", type = ApiParamType.STRING)
    private String sql;
    @EntityField(name = "条件配置内容", type = ApiParamType.STRING)
    private String condition;
    @EntityField(name = "主体配置内容", type = ApiParamType.STRING)
    private String content;
    @EntityField(name = "是否激活", type = ApiParamType.INTEGER)
    private Integer isActive;
    @EntityField(name = "访问次数", type = ApiParamType.INTEGER)
    private Integer visitCount;
    @EntityField(name = "创建用户", type = ApiParamType.STRING)
    private String fcu;
    @EntityField(name = "最后修改人", type = ApiParamType.STRING)
    private String lcu;
    @EntityField(name = "创建日期", type = ApiParamType.LONG)
    private Date fcd;
    @EntityField(name = "修改日期", type = ApiParamType.LONG)
    private Date lcd;
    @EntityField(name = "授权列表", type = ApiParamType.JSONARRAY)
    private List<ReportAuthVo> reportAuthList;
    @EntityField(name = "授权字符串列表", type = ApiParamType.JSONARRAY)
    private List<String> authList;
    @EntityField(name = "参数列表", type = ApiParamType.JSONARRAY)
    private List<ReportParamVo> paramList;
    @EntityField(name = "表格列表", type = ApiParamType.JSONARRAY)
    List<Map<String, Object>> tableList;
    @JSONField(serialize = false)//搜索模式，默认是按用户搜索，管理员页面无需检查用户权限
    private String searchMode = "user";
    @JSONField(serialize = false)
    private Integer sort;

    public Long getId() {
        if (id == null) {
            id = SnowflakeUtil.uniqueLong();
        }
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public Integer getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(Integer visitCount) {
        this.visitCount = visitCount;
    }

    public String getFcu() {
        return fcu;
    }

    public void setFcu(String fcu) {
        this.fcu = fcu;
    }

    public String getLcu() {
        return lcu;
    }

    public void setLcu(String lcu) {
        this.lcu = lcu;
    }

    public Date getFcd() {
        return fcd;
    }

    public void setFcd(Date fcd) {
        this.fcd = fcd;
    }

    public Date getLcd() {
        return lcd;
    }

    public void setLcd(Date lcd) {
        this.lcd = lcd;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public List<ReportAuthVo> getReportAuthList() {
        return reportAuthList;
    }

    public void setReportAuthList(List<ReportAuthVo> reportAuthList) {
        this.reportAuthList = reportAuthList;
    }

    public List<String> getAuthList() {
        if (CollectionUtils.isEmpty(authList) && CollectionUtils.isNotEmpty(reportAuthList)) {
            this.authList = new ArrayList<>();
            for (ReportAuthVo reportAuthVo : reportAuthList) {
                this.authList.add(reportAuthVo.getAuthType() + "#" + reportAuthVo.getAuthUuid());
            }
        }
        return authList;
    }

    public void setAuthList(List<String> authList) {
        this.authList = authList;
    }

    public List<ReportParamVo> getParamList() {
        return paramList;
    }

    public void setParamList(List<ReportParamVo> paramList) {
        this.paramList = paramList;
    }

    public List<Map<String, Object>> getTableList() {
        return tableList;
    }

    public void setTableList(List<Map<String, Object>> tableList) {
        this.tableList = tableList;
    }

    public String getSearchMode() {
        return searchMode;
    }

    public void setSearchMode(String searchMode) {
        this.searchMode = searchMode;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
