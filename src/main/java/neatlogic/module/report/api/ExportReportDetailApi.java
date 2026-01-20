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
import neatlogic.framework.dao.mapper.UserExportFileMapper;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.report.enums.ReportUserExportFileType;
import neatlogic.framework.report.exception.ReportNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.userexportfile.dto.UserExportFileVo;
import neatlogic.framework.util.DocType;
import neatlogic.framework.util.ExportUtil;
import neatlogic.framework.util.UserExportFileUtil;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.constvalue.ActionType;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dto.ReportVo;
import neatlogic.module.report.service.ReportService;
import neatlogic.module.report.util.ReportFreemarkerUtil;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @Resource
    private UserExportFileMapper userExportFileMapper;

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
        // 统计使用次数
        reportMapper.updateReportVisitCount(reportId);
        /* 获取表格显示列配置 */
        Map<String, List<String>> showColumnsMap = reportService.getShowColumnsMap(reportInstanceId);

        try {
            ReportVo reportVo = reportService.getReportDetailById(reportId);
            if (reportVo == null) {
                throw new ReportNotFoundException(reportId);
            }
            Map<String, Object> returnMap = reportService.getQuerySqlResult(reportVo, paramObj, showColumnsMap);
            Map<String, Object> tmpMap = new HashMap<>();
            Map<String, Object> commonMap = new HashMap<>();
            tmpMap.put("report", returnMap);
            tmpMap.put("param", paramObj);
            tmpMap.put("common", commonMap);

            String content = ReportFreemarkerUtil.getFreemarkerExportContent(tmpMap, returnMap, filter, reportVo.getContent(), ActionType.EXPORT.getValue());
            if (DocType.PDF.getValue().equals(type)) {
                UserExportFileVo userExportFileVo = new UserExportFileVo(ReportUserExportFileType.REPORT_DATA, reportVo.getName(), ".pdf", "application/pdf");
                userExportFileMapper.insertUserExportFile(userExportFileVo);
                DeferredFileOutputStream deferredFileOutputStream = UserExportFileUtil.getDeferredFileOutputStream(reportVo.getName(), ".pdf");
                ExportUtil.getPdfFileByHtml(content, deferredFileOutputStream, true, true);
                UserExportFileUtil.saveDeferredFileOutputStream(deferredFileOutputStream, userExportFileVo, response);
            } else if (DocType.WORD.getValue().equals(type)) {
                UserExportFileVo userExportFileVo = new UserExportFileVo(ReportUserExportFileType.REPORT_DATA, reportVo.getName(), ".docx", "application/x-download");
                userExportFileMapper.insertUserExportFile(userExportFileVo);
                DeferredFileOutputStream deferredFileOutputStream = UserExportFileUtil.getDeferredFileOutputStream(reportVo.getName(), ".docx");
                ExportUtil.getWordFileByHtml(content, deferredFileOutputStream, true, true);
                UserExportFileUtil.saveDeferredFileOutputStream(deferredFileOutputStream, userExportFileVo, response);
            } else if (DocType.EXCEL.getValue().equals(type)) {
                UserExportFileVo userExportFileVo = new UserExportFileVo(ReportUserExportFileType.REPORT_DATA, reportVo.getName(), ".xlsx", "application/vnd.ms-excel;charset=utf-8");
                userExportFileMapper.insertUserExportFile(userExportFileVo);
                Workbook workbook = reportService.getReportWorkbook(content);
                UserExportFileUtil.saveWorkbook(workbook, userExportFileVo, response);
            }
        } catch (ApiRuntimeException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }

}
