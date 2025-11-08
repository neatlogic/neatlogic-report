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

package neatlogic.module.report.constvalue;

import neatlogic.framework.util.$;

public enum BlackWhiteType {

    BLACK("black", "黑名单"),
    WHITE("white", "白名单");


    private final String value;
    private final String text;

    BlackWhiteType(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }


    public String getText() {
        return $.t(text);
    }

    public static String getText(String _value) {
        for (BlackWhiteType type : values()) {
            if (type.getValue().equals(_value)) {
                return type.getText();
            }
        }
        return null;
    }

}
