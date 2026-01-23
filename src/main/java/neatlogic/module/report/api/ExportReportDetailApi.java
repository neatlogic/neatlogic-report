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

package neatlogic.module.report.api;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.MimeType;
import neatlogic.framework.common.constvalue.ResponseCode;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.report.exception.ReportNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.userexportfile.constvalue.FrameworkUserExportFileType;
import neatlogic.framework.userexportfile.core.ExportFileManager;
import neatlogic.framework.util.DocType;
import neatlogic.framework.util.ExportUtil;
import neatlogic.framework.util.FileUtil;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.constvalue.ActionType;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dto.ReportVo;
import neatlogic.module.report.service.ReportService;
import neatlogic.module.report.util.ReportFreemarkerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    @Description(desc = "导出报表")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        JSONObject filter = new JSONObject();
        filter.putAll(paramObj);
        filter.remove("type");
        Long reportId = paramObj.getLong("id");
        String type = paramObj.getString("type");
        Long reportInstanceId = paramObj.getLong("reportInstanceId");
        ReportVo reportVo = reportService.getReportDetailById(reportId);
        if (reportVo == null) {
            throw new ReportNotFoundException(reportId);
        }
        // 统计使用次数
        reportMapper.updateReportVisitCount(reportId);
        ExportFileManager exportFileManager = new ExportFileManager(FrameworkUserExportFileType.MATRIX_DATA)
                .withAwait(5, TimeUnit.SECONDS);
        if (DocType.PDF.getValue().equals(type)) {
            exportFileManager.withName(reportVo.getName() + ".pdf").withMimeType(MimeType.PDF);
        } else if (DocType.WORD.getValue().equals(type)) {
            exportFileManager.withName(reportVo.getName() + ".docx").withMimeType(MimeType.DOCX);
        } else if (DocType.EXCEL.getValue().equals(type)) {
            exportFileManager.withName(reportVo.getName() + ".xlsx").withMimeType(MimeType.XLS);
        }
        exportFileManager.generateData((outputStream) -> {
        /* 获取表格显示列配置 */
        Map<String, List<String>> showColumnsMap = reportService.getShowColumnsMap(reportInstanceId);

        try {
            Map<String, Object> returnMap = reportService.getQuerySqlResult(reportVo, paramObj, showColumnsMap);
            Map<String, Object> tmpMap = new HashMap<>();
            Map<String, Object> commonMap = new HashMap<>();
            tmpMap.put("report", returnMap);
            tmpMap.put("param", paramObj);
            tmpMap.put("common", commonMap);

            String content = ReportFreemarkerUtil.getFreemarkerExportContent(tmpMap, returnMap, filter, reportVo.getContent(), ActionType.EXPORT.getValue());
            if (DocType.PDF.getValue().equals(type)) {
                ExportUtil.getPdfFileByHtml(content, outputStream, true, true);
            } else if (DocType.WORD.getValue().equals(type)) {
                ExportUtil.getWordFileByHtml(content, outputStream, true, true);
            } else if (DocType.EXCEL.getValue().equals(type)) {
                Workbook workbook = reportService.getReportWorkbook(content);
                workbook.write(outputStream);
            }
        } catch (ApiRuntimeException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        });
        try (OutputStream os = response.getOutputStream()) {
            response.setContentType(exportFileManager.getMimeType().getValue());
            response.setHeader("Content-Disposition", " attachment; filename=\"" + FileUtil.getEncodedFileName(exportFileManager.getName()) + "\"");
            if (!exportFileManager.exportTo(os)) {
                response.setStatus(ResponseCode.EXPORT_TIMEOUT.getCode());
            }
        }
        return null;
    }

}
