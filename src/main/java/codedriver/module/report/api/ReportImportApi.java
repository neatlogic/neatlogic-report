/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.exception.file.FileExtNotAllowedException;
import codedriver.framework.exception.file.FileNotUploadException;
import codedriver.framework.exception.type.ParamNotExistsException;
import codedriver.framework.exception.type.ParamRepeatsException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.SnowflakeUtil;
import codedriver.module.report.auth.label.REPORT_BASE;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportAuthVo;
import codedriver.module.report.dto.ReportParamVo;
import codedriver.module.report.dto.ReportVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipInputStream;

@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class ReportImportApi extends PrivateBinaryStreamApiComponentBase {
    private static final Logger logger = LoggerFactory.getLogger(ReportImportApi.class);

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "report/import";
    }

    @Override
    public String getName() {
        return "导入报表模版";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({
            @Param(name = "successCount", type = ApiParamType.INTEGER, desc = "导入成功数量"),
            @Param(name = "failureCount", type = ApiParamType.INTEGER, desc = "导入失败数量"),
            @Param(name = "failureReasonList", type = ApiParamType.JSONARRAY, desc = "失败原因")
    })
    @Description(desc = "导入报表模版")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        JSONObject resultObj = new JSONObject();
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
        if (multipartFileMap.isEmpty()) {
            throw new FileNotUploadException();
        }
        JSONArray resultList = new JSONArray();
        byte[] buf = new byte[1024];
        int successCount = 0;
        int failureCount = 0;
        for (Map.Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
            MultipartFile multipartFile = entry.getValue();
            try (ZipInputStream zis = new ZipInputStream(multipartFile.getInputStream());
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                while (zis.getNextEntry() != null) {
                    int len;
                    while ((len = zis.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                    ReportVo reportVo = JSONObject.parseObject(new String(out.toByteArray(), StandardCharsets.UTF_8), new TypeReference<ReportVo>() {
                    });
                    JSONObject result = save(reportVo);
                    if (MapUtils.isNotEmpty(result)) {
                        resultList.add(result);
                        failureCount++;
                    } else {
                        successCount++;
                    }
                    out.reset();
                }
            } catch (IOException e) {
                throw new FileExtNotAllowedException(multipartFile.getOriginalFilename());
            }
        }
        resultObj.put("successCount", successCount);
        resultObj.put("failureCount", failureCount);
        if (CollectionUtils.isNotEmpty(resultList)) {
            resultObj.put("failureReasonList", resultList);
        }
        return null;
    }

    private JSONObject save(ReportVo reportVo) {
        List<String> failReasonList = new ArrayList<>();
        String name = reportVo.getName();
        ReportVo oldReport = reportMapper.getReportByName(name);
        List<ReportParamVo> paramList = reportVo.getParamList();
        List<ReportAuthVo> reportAuthList = reportVo.getReportAuthList();
        if (oldReport != null) {
            reportVo.setId(oldReport.getId());
        } else {
            reportVo.setId(SnowflakeUtil.uniqueLong());
        }
        if (CollectionUtils.isNotEmpty(paramList)) {
            Set<String> keySet = new HashSet<>();
            try {
                for (int i = 0; i < paramList.size(); i++) {
                    ReportParamVo paramVo = paramList.get(i);
                    paramVo.setReportId(reportVo.getId());
                    String key = paramVo.getName();
                    if (StringUtils.isBlank(key)) {
                        throw new ParamNotExistsException("paramList.[" + i + "].key");
                    }
                    if (keySet.contains(key)) {
                        throw new ParamRepeatsException("paramList.[" + i + "].key");
                    } else {
                        keySet.add(key);
                    }
                }
            } catch (Exception ex) {
                failReasonList.add("报表【" + name + "】：" + ex.getMessage());
            }
        }
        if (CollectionUtils.isNotEmpty(reportAuthList)) {
            Iterator<ReportAuthVo> iterator = reportAuthList.iterator();
            while (iterator.hasNext()) {
                ReportAuthVo next = iterator.next();
                String authType = next.getAuthType();
                String authUuid = next.getAuthUuid();
                next.setReportId(reportVo.getId());
                boolean authTargetExist = true;
                if (GroupSearch.USER.getValue().equals(authType)) {
                    if (userMapper.checkUserIsExists(authUuid) == 0) {
                        authTargetExist = false;
                    }
                } else if (GroupSearch.TEAM.getValue().equals(authType)) {
                    if (teamMapper.checkTeamIsExists(authUuid) == 0) {
                        authTargetExist = false;
                    }
                } else if (GroupSearch.ROLE.getValue().equals(authType)) {
                    if (roleMapper.checkRoleIsExists(authUuid) == 0) {
                        authTargetExist = false;
                    }
                }
                if (!authTargetExist) {
                    iterator.remove();
                }
            }
        }
        if (CollectionUtils.isEmpty(failReasonList)) {
            if (oldReport != null) {
                reportMapper.getReportByIdForUpudate(reportVo.getId());
                reportMapper.updateReport(reportVo);
                reportMapper.deleteReportParamByReportId(oldReport.getId());
                reportMapper.deleteReportAuthByReportId(oldReport.getId());
            } else {
                reportMapper.insertReport(reportVo);
            }
            if (CollectionUtils.isNotEmpty(paramList)) {
                for (int i = 0; i < paramList.size(); i++) {
                    paramList.get(i).setSort(i);
                }
                reportMapper.batchInsertReportParam(paramList);
            }
            if (CollectionUtils.isNotEmpty(reportAuthList)) {
                reportMapper.batchInsertReportAuth(reportAuthList);
            }
        }
        if (CollectionUtils.isNotEmpty(failReasonList)) {
            JSONObject result = new JSONObject();
            result.put("item", "导入：" + name + "时出现如下问题：");
            result.put("list", failReasonList);
            return result;
        }
        return null;
    }

}
