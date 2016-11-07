package com.gxl.core.tracing;

import com.gxl.utils.CreateTraceID;

import redis.clients.jedis.JedisCluster;

public class TraceHandler {
	public ThreadLocal<TraceBean> local;

	public TraceHandler() {
		local = new ThreadLocal<TraceBean>();
	}

	public ThreadLocal<TraceBean> getTrace() {
		return local;
	}

	/**
	 * 根调用,创建TraceID和其他相关的调用链上下文信息
	 * 
	 * @author gaoxianglong
	 */
	public TraceBean createTracer(String host, Integer port, String serviceName, String methodName) {
		TraceBean tracer = new TraceBean();
		tracer.setRootSpan(true);
		tracer.setHost(host);
		tracer.setPort(port);
		tracer.setServiceName(serviceName);
		tracer.setMethodName(methodName);
		tracer.setTraceId(CreateTraceID.getTraceID());
		/* 根服务调用没有parentSpanId */
		tracer.setParentSpanId(-1);
		/* 缺省为0 */
		tracer.setSpanId(0);
		TraceFilter.jedisCluster.set(String.valueOf(tracer.getTraceId()), String.valueOf(tracer.getSpanId()));
		return tracer;
	}

	/**
	 * 服务调用方接受传递过来的调用链上下文信息
	 * 
	 * @author gaoxianglong
	 */
	public TraceBean createTracer(Long traceID, Integer spanID, Integer parentSpanID, String host, Integer port,
			String serviceName, String methodName) {
		TraceBean tracer = new TraceBean();
		tracer.setTraceId(traceID);
		tracer.setSpanId(spanID);
		tracer.setParentSpanId(parentSpanID);
		tracer.setHost(host);
		tracer.setPort(port);
		tracer.setServiceName(serviceName);
		tracer.setMethodName(methodName);
		return tracer;
	}

	public TraceBean createTracer() {
		return new TraceBean();
	}

	/**
	 * 从Redis中获取当前调用链的SpanID
	 *
	 * @author gaoxianglong
	 */
	public Integer getSpanID(Long traceID, JedisCluster jedisCluster) {
		Integer spanID = Integer.parseInt(jedisCluster.get(String.valueOf(traceID))) + 1;
		jedisCluster.set(String.valueOf(traceID), String.valueOf(spanID));
		return spanID;
	}
}