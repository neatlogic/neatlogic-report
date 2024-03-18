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

package neatlogic.module.report.api;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.FileUtil;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dto.ReportVo;
import neatlogic.module.report.service.ReportService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class ReportExportApi extends PrivateBinaryStreamApiComponentBase {
    private static final Logger logger = LoggerFactory.getLogger(ReportExportApi.class);

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private ReportService reportService;

    @Override
    public String getToken() {
        return "report/export";
    }

    @Override
    public String getName() {
        return "导出报表模版";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "idList", desc = "报表id列表", type = ApiParamType.JSONARRAY, isRequired = true)})
    @Description(desc = "导出报表模版")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        List<Long> idList = paramObj.getJSONArray("idList").toJavaList(Long.class);
        List<Long> existedIdList = reportMapper.checkReportIdListExists(idList);
        idList.removeAll(existedIdList);
        if (CollectionUtils.isNotEmpty(idList)) {
            logger.error("报表：{}不存在", idList.stream().map(Object::toString).collect(Collectors.joining(",")));
        }
        String fileName = FileUtil.getEncodedFileName("报表模版." + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".pak");
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", " attachment; filename=\"" + fileName + "\"");
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (Long id : existedIdList) {
                ReportVo reportVo = reportService.getReportDetailById(id);
                zos.putNextEntry(new ZipEntry(reportVo.getName() + ".json"));
                zos.write(JSONObject.toJSONBytes(reportVo));
                zos.closeEntry();
            }
        }
        return null;
    }

}
