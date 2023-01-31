/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.report.constvalue;

public enum BlackWhiteType {

    BLACK("black", "黑名单"), WHITE("white", "白名单");


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
        return text;
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
