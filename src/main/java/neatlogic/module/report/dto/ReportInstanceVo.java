/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.report.dto;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BaseEditorVo;
import neatlogic.framework.restful.annotation.EntityField;
import neatlogic.framework.util.SnowflakeUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReportInstanceVo extends BaseEditorVo {
    @EntityField(name = "id", type = ApiParamType.LONG)
    private Long id;
    @EntityField(name = "报表定义id", type = ApiParamType.LONG)
    private Long reportId;
    @EntityField(name = "报表定义名称", type = ApiParamType.STRING)
    private String reportName;
    @EntityField(name = "名称", type = ApiParamType.STRING)
    private String name;
    @EntityField(name = "是否激活", type = ApiParamType.INTEGER)
    private Integer isActive;
    @EntityField(name = "访问次数", type = ApiParamType.INTEGER)
    private Integer visitCount;
    @EntityField(name = "配置", type = ApiParamType.JSONOBJECT)
    private JSONObject config;
    @EntityField(name = "授权列表", type = ApiParamType.JSONARRAY)
    private List<ReportInstanceAuthVo> reportInstanceAuthList;
    @EntityField(name = "授权字符串列表", type = ApiParamType.JSONARRAY)
    private List<String> authList;
    @JSONField(serialize = false) // 搜索模式，默认是按用户搜索，管理员页面无需检查用户权限
    private String searchMode = "user";
    @EntityField(name = "报表参数列表", type = ApiParamType.JSONARRAY)
    private List<ReportParamVo> paramList;
    @EntityField(name = "表格列表", type = ApiParamType.JSONARRAY)
    private Map<String, List<String>> tableColumnsMap;

    @JSONField(serialize = false)
    private Integer searchByFcu = 0;

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

    public List<ReportInstanceAuthVo> getReportInstanceAuthList() {
        return reportInstanceAuthList;
    }

    public void setReportInstanceAuthList(List<ReportInstanceAuthVo> reportInstanceAuthList) {
        this.reportInstanceAuthList = reportInstanceAuthList;
    }

    public List<String> getAuthList() {
        if (CollectionUtils.isEmpty(authList) && CollectionUtils.isNotEmpty(reportInstanceAuthList)) {
            this.authList = new ArrayList<>();
            for (ReportInstanceAuthVo reportAuthVo : reportInstanceAuthList) {
                this.authList.add(reportAuthVo.getAuthType() + "#" + reportAuthVo.getAuthUuid());
            }
        }
        return authList;
    }

    public void setAuthList(List<String> authList) {
        this.authList = authList;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public JSONObject getConfig() {
        return config;
    }

    public void setConfig(String config) {
        try {
            this.config = JSONObject.parseObject(config);
        } catch (Exception ex) {

        }
    }

    @JSONField(serialize = false)
    public String getConfigStr() {
        if (config != null) {
            return config.toJSONString();
        }
        return null;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getSearchMode() {
        return searchMode;
    }

    public void setSearchMode(String searchMode) {
        this.searchMode = searchMode;
    }

    public List<ReportParamVo> getParamList() {
        if (CollectionUtils.isNotEmpty(paramList) && config != null) {
            JSONObject paramObj = config.getJSONObject("param");
            if (paramObj != null) {
                for (ReportParamVo reportParamVo : paramList) {
                    if (paramObj.containsKey(reportParamVo.getName())) {
                        JSONObject tmpObj = reportParamVo.getConfig();
                        if (tmpObj == null) {
                            tmpObj = new JSONObject();
                        }
                        tmpObj.put("defaultValue", paramObj.getJSONObject(reportParamVo.getName()).get("defaultValue"));
                        reportParamVo.setConfig(tmpObj.toJSONString());
                    }
                }
            }
        }
        return paramList;
    }

    public void setParamList(List<ReportParamVo> paramList) {
        this.paramList = paramList;
    }

    public Map<String, List<String>> getTableColumnsMap() {
        return tableColumnsMap;
    }

    public void setTableColumnsMap(Map<String, List<String>> tableColumnsMap) {
        this.tableColumnsMap = tableColumnsMap;
    }

    public Integer getSearchByFcu() {
        return searchByFcu;
    }

    public void setSearchByFcu(Integer searchByFcu) {
        this.searchByFcu = searchByFcu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof ReportInstanceVo)) {
            return false;
        }
        final ReportInstanceVo other = (ReportInstanceVo) o;
        return Objects.equals(this.getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
