package com.gxl.utils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import com.sharksharding.util.sequence.SequenceIDManger;

/**
 * 生成全局唯一的TraceID
 * 
 * @author gaoxianglong
 */
public class CreateTraceID {
	@Resource
	private DataSource dataSource;

	public void init() {
		/* 初始化数据源信息 */
		SequenceIDManger.init(dataSource);
	}

	/**
	 * 获取 全局唯一的TraceId
	 *
	 * @author gaoxianglong
	 */
	public static Long getTraceID() {
		return SequenceIDManger.getSequenceId(100, 10, 100);
	}
}