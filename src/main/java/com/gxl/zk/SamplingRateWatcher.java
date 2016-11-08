package com.gxl.zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gxl.core.tracing.TraceFilter;

/**
 * 采样率信息的watcher
 * 
 * @author gaoxianglong
 */
public class SamplingRateWatcher implements Watcher {
	private ZooKeeper zk_client;
	private String nodePath;
	private Logger logger = LoggerFactory.getLogger(SamplingRateWatcher.class);

	protected SamplingRateWatcher(ZooKeeper zk_client, String nodePath) {
		this.zk_client = zk_client;
		this.nodePath = nodePath;
	}

	@Override
	public void process(WatchedEvent event) {
		if (null == zk_client)
			return;
		try {
			/* 重新注册节点 */
			zk_client.exists(nodePath, this);
			EventType eventType = event.getType();
			switch (eventType) {
			case NodeCreated:
				break;
			case NodeDataChanged:
				final String SAMPLINGRATE = new String(zk_client.getData("/tracing/samplingRate", false, null));
				/* 设置采样率规则 */
				TraceFilter.samplingNum = Integer.parseInt(SAMPLINGRATE);
				logger.info("change node data-->" + event.getPath());
				break;
			case NodeChildrenChanged:
				break;
			case NodeDeleted:
			default:
				break;
			}
		} catch (Exception e) {
			logger.error("error", e);
		}
	}
}