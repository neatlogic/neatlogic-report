/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.report.util;

import neatlogic.framework.datawarehouse.dto.DataSourceDataVo;
import neatlogic.framework.datawarehouse.dto.DataSourceFieldVo;
import neatlogic.framework.datawarehouse.dto.DataSourceVo;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

public class ReportDataBuilder {
    private final List<HashMap<String, Object>> resultList;
    private final DataSourceVo reportDataSourceVo;


    private ReportDataBuilder(Builder builder) {
        resultList = builder.resultList;
        reportDataSourceVo = builder.reportDataSourceVo;
    }

    public List<DataSourceDataVo> getDataList() {
        Map<Long, DataSourceDataVo> dataMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(resultList)) {
            for (Map<String, Object> result : resultList) {
                DataSourceDataVo dataVo;

                Long id = result.get("id") != null ? Long.valueOf(String.valueOf(result.get("id"))) : null;
                Date insertTime = result.get("insertTime") != null ? new Date(Long.parseLong(String.valueOf(result.get("insertTime")))) : null;
                Date expireTime = result.get("expireTime") != null ? new Date(Long.parseLong(String.valueOf(result.get("expireTime")))) : null;

                if (!dataMap.containsKey(id)) {
                    dataVo = new DataSourceDataVo(reportDataSourceVo.getId());
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
                        DataSourceFieldVo fieldVo = reportDataSourceVo.getFieldById(fieldId);
                        if (fieldVo != null && !dataVo.containField(fieldId)) {
                            DataSourceFieldVo fieldValueVo = new DataSourceFieldVo(fieldVo);
                            fieldValueVo.setValue(value);
                            dataVo.addField(fieldValueVo);
                        }
                    }
                }
            }
        }
        List<DataSourceDataVo> dataList = new ArrayList<>();
        for (Long key : dataMap.keySet()) {
            DataSourceDataVo dataVo = dataMap.get(key);
            dataList.add(dataVo);
        }
        return dataList;
    }

    public DataSourceDataVo getData() {
        DataSourceDataVo dataVo = new DataSourceDataVo(reportDataSourceVo.getId());
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
                        DataSourceFieldVo fieldVo = reportDataSourceVo.getFieldById(fieldId);
                        if (fieldVo != null && !dataVo.containField(fieldId)) {
                            DataSourceFieldVo fieldValueVo = new DataSourceFieldVo(fieldVo);
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
        private final DataSourceVo reportDataSourceVo;

        public Builder(DataSourceVo _reportDataSourceVo, List<HashMap<String, Object>> _resultList) {
            resultList = _resultList;
            reportDataSourceVo = _reportDataSourceVo;
        }


        public ReportDataBuilder build() {
            return new ReportDataBuilder(this);
        }
    }

}
