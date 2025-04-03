package com.dingxun.adapter.mqtt;

import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

/**
 * MQTT通信适配器描述类：
 * - 提供适配器的元数据（名称、类型）
 * - 标记为物理设备适配器（非模拟器）
 */
public class MQTTCommAdapterDescription
    extends VehicleCommAdapterDescription {

  @Override
  public String getDescription() {
    return "MQTT Communication Adapter";
  }

  @Override
  public boolean isSimVehicleCommAdapter() {
    return false; // 真实物理设备适配器
  }
}
