/*
 * Copyright 2015-2101 gaoxianglong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gxl.zk;

import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gxl.core.tracing.TraceFilter;
import com.sharksharding.exception.ConnectionException;

/**
 * 客户端连接管理器,与zookeeper服务器建立session会话
 * 
 * @author gaoxianglong
 */
public class ZKConnectionManager {
	private String address;
	private int zk_session_timeout;
	private String samplingRate_path;
	private CountDownLatch countDownLatch;
	private ZooKeeper zk_client;
	private Logger logger = LoggerFactory.getLogger(ZKConnectionManager.class);

	/**
	 * 初始化方法
	 *
	 * @author gaoxianglong
	 */
	public void init() {
		connection();
	}

	/**
	 * 连接zookeeper
	 * 
	 * @author gaoxianglong
	 * 
	 * @throws ConnectionException
	 * 
	 * @return void
	 */
	private void connection() {
		countDownLatch = new CountDownLatch(1);
		try {
			zk_client = new ZooKeeper(address, zk_session_timeout, new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					final KeeperState STATE = event.getState();
					switch (STATE) {
					case SyncConnected:
						countDownLatch.countDown();
						logger.info("connection zookeeper success");
						break;
					case Disconnected:
						logger.warn("zookeeper connection is disconnected");
						break;
					case Expired:
						logger.error("zookeeper session expired");
						break;
					case AuthFailed:
						logger.error("authentication failure");
					default:
						break;
					}
				}
			});
			countDownLatch.await();
			String samplingRate = null;
			/* 验证根目录是否存在 */
			if (null == zk_client.exists("/tracing", false)) {
				zk_client.create("/tracing", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			/* 验证采样率znode是否存在 */
			if (null != zk_client.exists(samplingRate_path, false)) {
				samplingRate = new String(zk_client.getData(samplingRate_path,
						new SamplingRateWatcher(zk_client, samplingRate_path), null));
			} else {
				logger.warn("节点-->" + samplingRate_path + "不存在,准备创建");
				/* 缺省1/1024的采样率 */
				zk_client.create(samplingRate_path, "1024".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				samplingRate = new String(zk_client.getData(samplingRate_path,
						new SamplingRateWatcher(zk_client, samplingRate_path), null));
			}
			/* 设置采样率规则 */
			TraceFilter.samplingNum = Integer.parseInt(samplingRate);
		} catch (Exception e) {
			logger.error("error", e);
		}
	}

	public int getZk_session_timeout() {
		return zk_session_timeout;
	}

	public void setZk_session_timeout(int zk_session_timeout) {
		this.zk_session_timeout = zk_session_timeout;
	}

	public String getSamplingRate_path() {
		return samplingRate_path;
	}

	public void setSamplingRate_path(String samplingRate_path) {
		this.samplingRate_path = samplingRate_path;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}