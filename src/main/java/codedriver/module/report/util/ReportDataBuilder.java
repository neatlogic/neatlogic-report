/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.util;

import codedriver.framework.report.dto.ReportDataSourceDataVo;
import codedriver.framework.report.dto.ReportDataSourceFieldVo;
import codedriver.framework.report.dto.ReportDataSourceVo;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

public class ReportDataBuilder {
    private final List<HashMap<String, Object>> resultList;
    private final ReportDataSourceVo reportDataSourceVo;


    private ReportDataBuilder(Builder builder) {
        resultList = builder.resultList;
        reportDataSourceVo = builder.reportDataSourceVo;
    }

    public List<ReportDataSourceDataVo> getDataList() {
        Map<Long, ReportDataSourceDataVo> dataMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(resultList)) {
            for (Map<String, Object> result : resultList) {
                ReportDataSourceDataVo dataVo;

                Long id = result.get("id") != null ? Long.valueOf(String.valueOf(result.get("id"))) : null;
                Date insertTime = result.get("insertTime") != null ? new Date(Long.parseLong(String.valueOf(result.get("insertTime")))) : null;
                Date expireTime = result.get("expireTime") != null ? new Date(Long.parseLong(String.valueOf(result.get("expireTime")))) : null;

                if (!dataMap.containsKey(id)) {
                    dataVo = new ReportDataSourceDataVo(reportDataSourceVo.getId());
                    dataVo.setId(id);
                    dataVo.setInsertTime(insertTime);
                    dataVo.setExpireTime(expireTime);
                    dataMap.put(id, dataVo);
                } else {
                    dataVo = dataMap.get(id);
                }

                for (String key : result.keySet()) {
                    if (key.startsWith("field_")) {
                        Long fieldId = Long.parseLong(key.substring(6));
                        Object value = result.get(key);
                        ReportDataSourceFieldVo fieldVo = reportDataSourceVo.getFieldById(fieldId);
                        if (fieldVo != null && !dataVo.containField(fieldId)) {
                            ReportDataSourceFieldVo fieldValueVo = new ReportDataSourceFieldVo(fieldVo);
                            fieldValueVo.setValue(value);
                            dataVo.addField(fieldValueVo);
                        }
                    }
                }
            }
        }
        List<ReportDataSourceDataVo> dataList = new ArrayList<>();
        for (Long key : dataMap.keySet()) {
            ReportDataSourceDataVo dataVo = dataMap.get(key);
            dataList.add(dataVo);
        }
        return dataList;
    }

    public ReportDataSourceDataVo getData() {
        ReportDataSourceDataVo dataVo = new ReportDataSourceDataVo(reportDataSourceVo.getId());
        if (CollectionUtils.isNotEmpty(resultList)) {
            for (Map<String, Object> result : resultList) {
                Long id = result.get("id") != null ? Long.valueOf(String.valueOf(result.get("id"))) : null;
                Date insertTime = result.get("insertTime") != null ? new Date(Long.parseLong(String.valueOf(result.get("insertTime")))) : null;
                Date expireTime = result.get("expireTime") != null ? new Date(Long.parseLong(String.valueOf(result.get("expireTime")))) : null;
                dataVo.setId(id);
                dataVo.setInsertTime(insertTime);
                dataVo.setExpireTime(expireTime);
                for (String key : result.keySet()) {
                    if (key.startsWith("field_")) {
                        Long fieldId = Long.parseLong(key.substring(6));
                        Object value = result.get(key);
                        ReportDataSourceFieldVo fieldVo = reportDataSourceVo.getFieldById(fieldId);
                        if (fieldVo != null && !dataVo.containField(fieldId)) {
                            ReportDataSourceFieldVo fieldValueVo = new ReportDataSourceFieldVo(fieldVo);
                            fieldValueVo.setValue(value);
                            dataVo.addField(fieldValueVo);
                        }
                    }
                }
            }
        }
        return dataVo;
    }


    public static class Builder {
        private final List<HashMap<String, Object>> resultList;
        private final ReportDataSourceVo reportDataSourceVo;

        public Builder(ReportDataSourceVo _reportDataSourceVo, List<HashMap<String, Object>> _resultList) {
            resultList = _resultList;
            reportDataSourceVo = _reportDataSourceVo;
        }


        public ReportDataBuilder build() {
            return new ReportDataBuilder(this);
        }
    }

}
