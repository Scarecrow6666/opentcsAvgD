package com.dingxun.adapter.mqtt;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * MQTT配置接口：
 * - 绑定到adapter-custom.properties文件
 * - 定义代理地址、QoS级别、命令主题等配置项
 */
@ConfigurationPrefix("com.dingxun.adapter.mqtt.MQTTConfig")
public interface MQTTConfig {

  @ConfigurationEntry(
      type = "String",
      description = "MQTT Broker连接地址 (格式: tcp://host:port)",
      orderKey = "0_connection"
  )
  String brokerUrl();// 对应配置文件中的mqtt.adapter.broker-url

  @ConfigurationEntry(
      type = "Integer",
      description = "MQTT QoS级别 (0-2)",
      orderKey = "1_quality"
  )
  int qosLevel();

  @ConfigurationEntry(
      type = "String",
      description = "命令订阅主题",
      orderKey = "2_topics"
  )
  String commandTopic();
}
