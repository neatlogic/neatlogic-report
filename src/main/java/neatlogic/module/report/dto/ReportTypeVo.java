/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.report.dto;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.EntityField;
import org.apache.commons.lang3.StringUtils;

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
