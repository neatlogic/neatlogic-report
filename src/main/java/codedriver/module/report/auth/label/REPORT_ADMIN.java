/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.auth.label;

import codedriver.framework.auth.core.AuthBase;

import java.util.Arrays;
import java.util.List;

public class REPORT_ADMIN extends AuthBase {

    @Override
    public String getAuthDisplayName() {
        return "报表模板管理权限";
    }

    @Override
    public String getAuthIntroduction() {
        return "拥有包括配置报表可用对象等所有权限";
    }

    @Override
    public String getAuthGroup() {
        return "report";
    }

    @Override
    public Integer getSort() {
        return 4;
    }

    @Override
    public List<Class<? extends AuthBase>> getIncludeAuths() {
        return Arrays.asList(REPORT_BASE.class, REPORT_MODIFY.class, REPORT_TEMPLATE_MODIFY.class);
    }

}
