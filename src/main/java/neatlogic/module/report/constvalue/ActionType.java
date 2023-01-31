/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.report.constvalue;

public enum ActionType {

    VIEW("view", "查看"),
    EXPORT("export", "导出");


    private final String value;
    private final String text;

    private ActionType(String value, String text) {
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
        for (ActionType type : values()) {
            if (type.getValue().equals(_value)) {
                return type.getText();
            }
        }
        return null;
    }

}
