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
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.report.exception.ReportNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.DocType;
import neatlogic.framework.util.ExportUtil;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.constvalue.ActionType;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dto.ReportVo;
import neatlogic.module.report.service.ReportService;
import neatlogic.module.report.util.ReportFreemarkerUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class ExportReportDetailApi extends PrivateBinaryStreamApiComponentBase {
    private static final Log logger = LogFactory.getLog(ExportReportDetailApi.class);

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private ReportService reportService;

    @Override
    public String getToken() {
        return "report/detail/export/{id}/{type}";
    }

    @Override
    public String getName() {
        return "导出报表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", desc = "报表id", type = ApiParamType.LONG, isRequired = true),
            @Param(name = "reportInstanceId", desc = "报表实例id", type = ApiParamType.LONG),
            @Param(name = "type", desc = "文件类型", type = ApiParamType.ENUM, rule = "pdf,word,excel", isRequired = true)})
    @Description(desc = "导出报表接口")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        JSONObject filter = new JSONObject();
        filter.putAll(paramObj);
        filter.remove("type");
        Long reportId = paramObj.getLong("id");
        String type = paramObj.getString("type");
        Long reportInstanceId = paramObj.getLong("reportInstanceId");
        // 统计使用次数
        reportMapper.updateReportVisitCount(reportId);
        /* 获取表格显示列配置 */
        Map<String, List<String>> showColumnsMap = reportService.getShowColumnsMap(reportInstanceId);

        OutputStream os = null;
        try {
            ReportVo reportVo = reportService.getReportDetailById(reportId);
            if (reportVo == null) {
                throw new ReportNotFoundException(reportId);
            }
            Map<String, Object> returnMap = reportService.getQuerySqlResult(reportVo, paramObj, showColumnsMap);
            Map<String, Map<String, Object>> pageMap = (Map<String, Map<String, Object>>) returnMap.remove("page");
            Map<String, Object> tmpMap = new HashMap<>();
            Map<String, Object> commonMap = new HashMap<>();
            tmpMap.put("report", returnMap);
            tmpMap.put("param", paramObj);
            tmpMap.put("common", commonMap);

            String content = ReportFreemarkerUtil.getFreemarkerExportContent(tmpMap, returnMap, pageMap, filter, reportVo.getContent(), ActionType.EXPORT.getValue());
            if (DocType.PDF.getValue().equals(type)) {
                os = response.getOutputStream();
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition",
                        " attachment; filename=\"" + URLEncoder.encode(reportVo.getName(), "utf-8") + ".pdf\"");
                ExportUtil.getPdfFileByHtml(content, os, true, true);
            } else if (DocType.WORD.getValue().equals(type)) {
                os = response.getOutputStream();
                response.setContentType("application/x-download");
                response.setHeader("Content-Disposition",
                        " attachment; filename=\"" + URLEncoder.encode(reportVo.getName(), "utf-8") + ".docx\"");
                ExportUtil.getWordFileByHtml(content, os, true, true);
            } else if (DocType.EXCEL.getValue().equals(type)) {
                Workbook workbook = reportService.getReportWorkbook(content);
                String fileNameEncode = reportVo.getName() + ".xlsx";
                Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
                if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
                    fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
                } else {
                    fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
                }
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                response.setHeader("Content-Disposition", " attachment; filename=\"" + fileNameEncode + "\"");
                os = response.getOutputStream();
                workbook.write(os);
            }
        } catch (ApiRuntimeException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (os != null) {
                os.flush();
                os.close();
            }
        }
        return null;
    }

}
