/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.report.dao.mapper;

import neatlogic.framework.report.dto.ReportBlackWhiteListVo;

import java.util.List;

public interface ReportConfigMapper {
    int checkItemIsExists(ReportBlackWhiteListVo reportBlackWhiteListVo);

    ReportBlackWhiteListVo getBlackWhiteById(Long id);

    List<ReportBlackWhiteListVo> searchBlackWhiteList(ReportBlackWhiteListVo reportBlackWhiteListVo);

    int searchBlackWhiteListCount(ReportBlackWhiteListVo reportBlackWhiteListVo);

    int insertBlackWhiteList(ReportBlackWhiteListVo reportBlackWhiteListVo);

    int updateBlackWhiteList(ReportBlackWhiteListVo reportBlackWhiteListVo);
}
