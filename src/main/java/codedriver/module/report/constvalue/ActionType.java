package codedriver.module.report.constvalue;

public enum ActionType {

	VIEW("view","查看"),
	EXPORT("export","导出");
    
    
	private String value;
	private String text;
	
	private ActionType(String value,String text) {
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
		for(ActionType type : values()) {
			if(type.getValue().equals(_value)) {
				return type.getText();
			}
		}
		return null;
	}
	
}
