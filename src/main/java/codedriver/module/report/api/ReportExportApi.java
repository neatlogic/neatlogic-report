/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.FileUtil;
import codedriver.module.report.auth.label.REPORT_BASE;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportVo;
import codedriver.module.report.service.ReportService;
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
