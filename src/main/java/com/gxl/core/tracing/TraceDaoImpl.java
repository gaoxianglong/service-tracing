package com.gxl.core.tracing;

import javax.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.gxl.core.tracing.TraceBean;
import com.gxl.core.tracing.TraceDao;

@Repository("traceDao")
public class TraceDaoImpl implements TraceDao {
	@Resource
	private JdbcTemplate jdbcTemplate;

	@Override
	public void insertClientSendTime(TraceBean trace, long clientSendTime) {
		final String SQL = "INSERT INTO traceTab(traceId,parentId,spanId,host,port,serviceName,"
				+ "methodName,eventType,resultState,clientSendTime) VALUES(?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(SQL,
				new Object[] { trace.getTraceId(), trace.getParentSpanId(), trace.getSpanId(), trace.getHost(),
						trace.getPort(), trace.getServiceName(), trace.getMethodName(), trace.getEventType(),
						trace.getResultState(), clientSendTime });
	}

	@Override
	public void insertClientReceiveTime(TraceBean trace, long clientReceiveTime) {
		final String SQL = "INSERT INTO traceTab(traceId,parentId,spanId,host,port,serviceName,"
				+ "methodName,eventType,resultState,clientReceiveTime) VALUES(?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(SQL,
				new Object[] { trace.getTraceId(), trace.getParentSpanId(), trace.getSpanId(), trace.getHost(),
						trace.getPort(), trace.getServiceName(), trace.getMethodName(), trace.getEventType(),
						trace.getResultState(), clientReceiveTime });
	}

	@Override
	public void insertServerReceiveTime(TraceBean trace, long serverReceiveTime) {
		final String SQL = "INSERT INTO traceTab(traceId,parentId,spanId,host,port,serviceName,"
				+ "methodName,eventType,resultState,serverReceiveTime) VALUES(?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(SQL,
				new Object[] { trace.getTraceId(), trace.getParentSpanId(), trace.getSpanId(), trace.getHost(),
						trace.getPort(), trace.getServiceName(), trace.getMethodName(), trace.getEventType(),
						trace.getResultState(), serverReceiveTime });
	}

	@Override
	public void insertServerSendTime(TraceBean trace, long serverSendTime) {
		final String SQL = "INSERT INTO traceTab(traceId,parentId,spanId,host,port,serviceName,"
				+ "methodName,eventType,resultState,serverSendTime) VALUES(?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(SQL,
				new Object[] { trace.getTraceId(), trace.getParentSpanId(), trace.getSpanId(), trace.getHost(),
						trace.getPort(), trace.getServiceName(), trace.getMethodName(), trace.getEventType(),
						trace.getResultState(), serverSendTime });
	}
}