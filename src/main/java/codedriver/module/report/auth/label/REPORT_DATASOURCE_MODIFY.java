/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.auth.label;

import codedriver.framework.auth.core.AuthBase;

import java.util.Collections;
import java.util.List;

public class REPORT_DATASOURCE_MODIFY extends AuthBase {

    @Override
    public String getAuthDisplayName() {
        return "报表数据源管理权限";
    }

    @Override
    public String getAuthIntroduction() {
        return "拥有修改数据源同步策略的权限";
    }

    @Override
    public String getAuthGroup() {
        return "report";
    }

    @Override
    public Integer getSort() {
        return 2;
    }

    @Override
    public List<Class<? extends AuthBase>> getIncludeAuths() {
        return Collections.singletonList(REPORT_BASE.class);
    }

}
