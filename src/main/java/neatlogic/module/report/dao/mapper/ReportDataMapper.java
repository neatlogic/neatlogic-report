/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.report.dao.mapper;

import neatlogic.framework.report.dto.data.DistrictVo;

import java.util.List;

public interface ReportDataMapper {
    DistrictVo getDistrictDataById(Integer adcode);

    List<DistrictVo> searchDistrictData(DistrictVo districtVo);
}
