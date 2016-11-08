package com.gxl.core.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;

import redis.clients.jedis.JedisCluster;

/**
 * 拦截RPC请求进行调用追踪和收集数据
 * 
 * @author gaoxianglong
 */
@SuppressWarnings("resource")
@Activate(group = { Constants.PROVIDER, Constants.CONSUMER })
public class TraceFilter implements Filter {
	private TraceHandler traceHandler;
	private static TraceDao traceDao;
	public static JedisCluster jedisCluster;
	/* 采样率计数器 */
	private Integer samplingCounter = 1;
	/* 采样率 */
	public static Integer samplingNum;
	private static Logger logger = LoggerFactory.getLogger(TraceFilter.class);

	public TraceFilter() {
		traceHandler = new TraceHandler();
	}

	static {
		try {
			ApplicationContext context = new ClassPathXmlApplicationContext("classpath:tracing-context.xml");
			traceDao = (TraceDao) context.getBean("traceDao");
			jedisCluster = (JedisCluster) context.getBean("jedisCluster");
		} catch (Exception e) {
			logger.error("error", e);
		}
	}

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		logger.info("hashCode-->" + this.hashCode() + "\tthreadID-->" + Thread.currentThread().getId());
		final long BEFORE_TIME = System.currentTimeMillis();
		RpcInvocation rpcInvocation = (RpcInvocation) invocation;
		/* 获取当前Span过程的状态记录信息 */
		RpcContext context = RpcContext.getContext();
		final boolean IS_CONSUMER = context.isConsumerSide();
		final boolean IS_PROVIDER = context.isProviderSide();
		final String HOST = context.getLocalHost();
		final Integer PORT = context.getLocalPort();
		final String SERVICE_NAME = context.getUrl().getServiceInterface();
		String METHOD_NAME = context.getMethodName();
		/* 从ThreadLocal中获取当前线程的Trace上下文信息 */
		TraceBean traceBean = traceHandler.getTrace().get();
		/* 判断是服务提供方还是调用方 */
		if (IS_CONSUMER) {
			/* 如果ThreadLocal中不包含当前线程的Trace上下文信息则意味着是根调用,需要创建TraceID */
			if (null == traceBean) {
				synchronized (samplingCounter) {
					if (samplingCounter != samplingNum) {
						logger.info("serviceName-->" + SERVICE_NAME + "不需要采样");
						rpcInvocation.setAttachment("isSampling", "N");
						/* 根据请求数递增采样率计数器 */
						samplingCounter++;
						return invoker.invoke(rpcInvocation);
					}
					/* 重置采样率计数器 */
					samplingCounter = 1;
					logger.info("serviceName-->" + SERVICE_NAME + "需要采样");
					rpcInvocation.setAttachment("isSampling", "Y");
					traceBean = traceHandler.createTracer(HOST, PORT, SERVICE_NAME, METHOD_NAME);
				}
			} else {
				/* 检测是否需要采样 */
				if (!consumerIsSampling(traceBean, rpcInvocation))
					return invoker.invoke(rpcInvocation);
				final Long TRACE_ID = traceBean.getTraceId();
				/**
				 * 重设当前线程的调用链上下文信息
				 * 
				 * 一次请求的Trace通过TraceID串联起来,服务之间的依赖关系、调用顺序
				 * 通过parentSpanID和SpanID保证,
				 * 服务提供方才会将Trace上下文信息存储在当前线程的ThreadLocal中,SpanID每次都是递增1的,
				 * 而parentSpanID则是取它的上一个Span过程的SpanID,这样就可以明确服务调用的调用顺序和依赖关系
				 */
				traceBean = traceHandler.createTracer(TRACE_ID, traceHandler.getSpanID(TRACE_ID, jedisCluster),
						traceBean.getSpanId(), HOST, PORT, SERVICE_NAME, METHOD_NAME);
			}
			traceBean.setEventType(0);
			/* 将Trace上下文信息设置到Invocation中 */
			setAnnotation(rpcInvocation, traceBean);
		}
		if (IS_PROVIDER) {
			/* 检测是否需要采样 */
			if (!providerIsSampling(rpcInvocation))
				return invoker.invoke(rpcInvocation);
			/* 从Invocation中获取调用方传递过来的调用链上下文信息 */
			final Long TRACE_ID = Long.parseLong(rpcInvocation.getAttachment("traceID"));
			final Integer SPAN_ID = Integer.parseInt(rpcInvocation.getAttachment("spanID"));
			final Integer PARENT_SPAN_ID = Integer.parseInt(rpcInvocation.getAttachment("parentSpanID"));
			traceBean = traceHandler.createTracer(TRACE_ID, SPAN_ID, PARENT_SPAN_ID, HOST, PORT, SERVICE_NAME,
					METHOD_NAME);
			traceBean.setEventType(1);
		}
		/* 前置数据收集 */
		beforeDataCollect(traceBean, BEFORE_TIME);
		Result result = invoker.invoke(rpcInvocation);
		/* 后置数据收集 */
		afterDataCollect(traceBean, System.currentTimeMillis());
		return result;
	}

	/**
	 * 设置需要传递给服务提供方的调用链上下文信息
	 *
	 * @author gaoxianglong
	 */
	public void setAnnotation(RpcInvocation invocation, TraceBean trace) {
		invocation.setAttachment("traceID", String.valueOf(trace.getTraceId()));
		invocation.setAttachment("spanID", String.valueOf(trace.getSpanId()));
		invocation.setAttachment("parentSpanID", String.valueOf(trace.getParentSpanId()));
	}

	/**
	 * 前置数据收集
	 *
	 * @author gaoxianglong
	 */
	public void beforeDataCollect(TraceBean trace, long time) {
		/* 事件类型,0为调用方,1为提供方 */
		if (0 == trace.getEventType()) {
			traceDao.insertClientSendTime(trace, time);
		} else {
			traceDao.insertServerReceiveTime(trace, time);
			/* 将Trace上下文信息设置在ThreadLocal中 */
			traceHandler.getTrace().set(trace);
		}
	}

	/**
	 * 后置数据收集
	 *
	 * @author gaoxianglong
	 */
	public void afterDataCollect(TraceBean trace, long time) {
		if (0 == trace.getEventType()) {
			traceDao.insertClientReceiveTime(trace, time);
		} else {
			traceDao.insertServerSendTime(trace, time);
			traceHandler.getTrace().remove();
		}
		/* 如果是根服务调用则删除存放在Redis中的一次请求的Span记录 */
		if (trace.isRootSpan()) {
			jedisCluster.del(String.valueOf(trace.getTraceId()));
		}
	}

	/**
	 * 服务调用方是否采样检测
	 * 
	 * @author gaoxianglong
	 */
	public boolean consumerIsSampling(TraceBean traceBean, RpcInvocation rpcInvocation) {
		boolean result = false;
		if ("N".equals(traceBean.getIsSampling())) {
			rpcInvocation.setAttachment("isSampling", "N");
		} else {
			rpcInvocation.setAttachment("isSampling", "Y");
			result = true;
		}
		return result;
	}

	/**
	 * 服务提供方是否采样检测
	 * 
	 * @author gaoxianglong
	 */
	public boolean providerIsSampling(RpcInvocation rpcInvocation) {
		boolean result = false;
		TraceBean traceBean = traceHandler.createTracer();
		if ("N".equals(rpcInvocation.getAttachment("isSampling"))) {
			traceBean.setIsSampling("N");
		} else {
			traceBean.setIsSampling("Y");
			result = true;
		}
		traceHandler.getTrace().set(traceBean);
		return result;
	}
}