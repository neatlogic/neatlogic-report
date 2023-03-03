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
