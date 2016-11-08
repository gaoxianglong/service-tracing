package com.gxl.core.tracing;

/**
 * 用于记录一次请求的调用链上下文信息
 * 
 * @author gaoxianglong
 */
public class TraceBean {
	private Long traceId = -1L;
	private Integer parentSpanId = -1;
	private Integer spanId = 0;
	private String host;
	private Integer port;
	private String serviceName;
	private String methodName;
	/* 事件类型,0为调用方,1为提供方 */
	private Integer eventType;
	/* 服务调用结果状态,0成功,1异常 */
	private Integer resultState;
	/* 根调用标记 */
	private boolean isRootSpan = false;
	private String isSampling;

	public String getIsSampling() {
		return isSampling;
	}

	public void setIsSampling(String isSampling) {
		this.isSampling = isSampling;
	}

	public boolean isRootSpan() {
		return isRootSpan;
	}

	public void setRootSpan(boolean isRootSpan) {
		this.isRootSpan = isRootSpan;
	}

	public Integer getParentSpanId() {
		return parentSpanId;
	}

	public void setParentSpanId(Integer parentSpanId) {
		this.parentSpanId = parentSpanId;
	}

	public Long getTraceId() {
		return traceId;
	}

	public void setTraceId(Long traceId) {
		this.traceId = traceId;
	}

	public Integer getSpanId() {
		return spanId;
	}

	public void setSpanId(Integer spanId) {
		this.spanId = spanId;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Integer getEventType() {
		return eventType;
	}

	public void setEventType(Integer eventType) {
		this.eventType = eventType;
	}

	public Integer getResultState() {
		return resultState;
	}

	public void setResultState(Integer resultState) {
		this.resultState = resultState;
	}
}