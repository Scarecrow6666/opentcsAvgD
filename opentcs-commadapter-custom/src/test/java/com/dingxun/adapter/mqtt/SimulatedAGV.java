package com.dingxun.adapter.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SimulatedAGV {
  private static final String BROKER = "tcp://localhost:1883";
  private static final String CLIENT_ID = "Simulated-AGV";
  private static final String COMMAND_TOPIC = "opentcs/commands";
  private static final String STATUS_TOPIC = "agv/status";

  public static void main(String[] args) {
    try (MqttClient client = new MqttClient(BROKER, CLIENT_ID)) {
      MqttConnectOptions options = new MqttConnectOptions();
      options.setAutomaticReconnect(true);
      client.connect(options);

      // 订阅命令主题
      client.subscribe(COMMAND_TOPIC, (topic, message) -> {
        String payload = new String(message.getPayload());
        System.out.println("[AGV] 收到命令: " + payload);
        // 解析命令并执行动作（例如移动到目标点）
      });

      // 模拟发送状态信息
      while (true) {
        String status = "{\"vehicleId\":\"AGV-001\", \"status\":\"IDLE\"}";
        client.publish(STATUS_TOPIC, new MqttMessage(status.getBytes()));
        Thread.sleep(5000); // 每5秒发送一次状态
      }
    } catch (MqttException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
