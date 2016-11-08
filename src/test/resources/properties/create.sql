CREATE TABLE `tracetab` (
  `tid` int(10) NOT NULL AUTO_INCREMENT,
  `traceId` bigint(20) DEFAULT NULL,
  `parentId` int(11) DEFAULT NULL,
  `spanId` varchar(80) DEFAULT NULL,
  `host` varchar(80) DEFAULT NULL,
  `port` int(11) DEFAULT NULL,
  `serviceName` varchar(80) DEFAULT NULL,
  `methodName` varchar(80) DEFAULT NULL,
  `eventType` int(11) DEFAULT NULL,
  `resultState` int(11) DEFAULT NULL,
  `clientSendTime` bigint(20) DEFAULT NULL,
  `clientReceiveTime` bigint(20) DEFAULT NULL,
  `ServerReceiveTime` bigint(20) DEFAULT NULL,
  `ServerSendTime` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`)
) ENGINE=InnoDB AUTO_INCREMENT=323 DEFAULT CHARSET=utf8;