package com.gxl.core.tracing;

public interface TraceDao {
	/**
	 * 消费者before落盘数据
	 * 
	 * @author gaoxianglong
	 */
	public void insertClientSendTime(TraceBean trace, long clientSendTime);

	/**
	 * 消费者after落盘数据
	 * 
	 * @author gaoxianglong
	 */
	public void insertClientReceiveTime(TraceBean trace, long clientReceiveTime);

	/**
	 * 提供者before落盘数据
	 * 
	 * @author gaoxianglong
	 */
	public void insertServerReceiveTime(TraceBean trace, long serverReceiveTime);

	/**
	 * 提供者after落盘数据
	 * 
	 * @author gaoxianglong
	 */
	public void insertServerSendTime(TraceBean trace, long serverSendTime);
}