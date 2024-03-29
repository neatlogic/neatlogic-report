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
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.file.FileExtNotAllowedException;
import neatlogic.framework.exception.file.FileNotUploadException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.SnowflakeUtil;
import neatlogic.module.report.auth.label.REPORT_TEMPLATE_MODIFY;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dto.ReportAuthVo;
import neatlogic.module.report.dto.ReportParamVo;
import neatlogic.module.report.dto.ReportVo;
import neatlogic.module.report.service.ReportService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

@Transactional
@AuthAction(action = REPORT_TEMPLATE_MODIFY.class)
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

    @Resource
    private ReportService reportService;

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
        return resultObj;
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
            try {
                reportService.validateReportParamList(paramList);
            } catch (Exception ex) {
                failReasonList.add("报表【" + name + "】：" + ex.getMessage());
            }
        }
        if (CollectionUtils.isNotEmpty(reportAuthList)) {
            List<String> userUuidList = reportAuthList.stream().filter(o -> GroupSearch.USER.getValue().equals(o.getAuthType())).map(ReportAuthVo::getAuthUuid).collect(Collectors.toList());
            List<String> teamUuidList = reportAuthList.stream().filter(o -> GroupSearch.TEAM.getValue().equals(o.getAuthType())).map(ReportAuthVo::getAuthUuid).collect(Collectors.toList());
            List<String> roleUuidList = reportAuthList.stream().filter(o -> GroupSearch.ROLE.getValue().equals(o.getAuthType())).map(ReportAuthVo::getAuthUuid).collect(Collectors.toList());
            reportAuthList.clear();
            if (CollectionUtils.isNotEmpty(userUuidList)) {
                List<UserVo> userList = userMapper.getUserByUserUuidList(userUuidList);
                for (UserVo user : userList) {
                    reportAuthList.add(new ReportAuthVo(reportVo.getId(), GroupSearch.USER.getValue(), user.getUuid()));
                }
            }
            if (CollectionUtils.isNotEmpty(teamUuidList)) {
                List<TeamVo> teamList = teamMapper.getTeamByUuidList(teamUuidList);
                for (TeamVo team : teamList) {
                    reportAuthList.add(new ReportAuthVo(reportVo.getId(), GroupSearch.TEAM.getValue(), team.getUuid()));
                }
            }
            if (CollectionUtils.isNotEmpty(roleUuidList)) {
                List<RoleVo> roleList = roleMapper.getRoleByUuidList(roleUuidList);
                for (RoleVo role : roleList) {
                    reportAuthList.add(new ReportAuthVo(reportVo.getId(), GroupSearch.ROLE.getValue(), role.getUuid()));
                }
            }
        }
        if (CollectionUtils.isEmpty(failReasonList)) {
            if (oldReport != null) {
                reportMapper.getReportByIdForUpudate(reportVo.getId());
                reportMapper.updateReport(reportVo);
                reportMapper.deleteReportParamByReportId(reportVo.getId());
                reportMapper.deleteReportAuthByReportId(reportVo.getId());
            } else {
                reportMapper.insertReport(reportVo);
            }
            if (CollectionUtils.isNotEmpty(paramList)) {
                for (int i = 0; i < paramList.size(); i++) {
                    paramList.get(i).setReportId(reportVo.getId());
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
            result.put("item", "导入【" + name + "】时出现如下问题：");
            result.put("list", failReasonList);
            return result;
        }
        return null;
    }

}
