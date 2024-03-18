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

package neatlogic.module.report.auth.label;

import neatlogic.framework.auth.core.AuthBase;

import java.util.Arrays;
import java.util.List;

public class REPORT_TEMPLATE_MODIFY extends AuthBase {

    @Override
    public String getAuthDisplayName() {
        return "报表模板管理员权限";
    }

    @Override
    public String getAuthIntroduction() {
        return "可以查看和修改报表模板";
    }

    @Override
    public String getAuthGroup() {
        return "report";
    }

    @Override
    public Integer getSort() {
        return 3;
    }

    @Override
    public List<Class<? extends AuthBase>> getIncludeAuths() {
        return Arrays.asList(REPORT_BASE.class);
    }
}
