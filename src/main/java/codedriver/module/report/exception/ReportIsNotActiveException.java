/*
 * Copyright(c) 2021. TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ReportIsNotActiveException extends ApiRuntimeException {

	private static final long serialVersionUID = 1373950211961149950L;

	public ReportIsNotActiveException(String name) {
		super("报表：" + name + "未激活");
	}
}
