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

package neatlogic.module.report.dto;

import org.apache.commons.lang3.StringUtils;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.EntityField;

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
