package com.yanan.test.junit;

/**
 * 报告类型
 * ALL,SUCCESS,FAILED,ERROR
 * 分别对应 所有 成功 失败  错误
 * @author yanan
 *
 */
public enum ReportType {
	ALL,SUCCESS,FAILED,ERROR;
	/**
	 * 将字符串转化为类型
	 * @param type 类型
	 * @return ReportType
	 */
	public static ReportType getType(String type) {
		type = type.toLowerCase();
		switch(type) {
		case "all":
			return ALL;
		case "success":
			return SUCCESS;
		case "failed":
			return FAILED;
		case "error":
			return ERROR;
		}
		throw new RuntimeException("could not parse the type for ["+type+"]");
	}
}