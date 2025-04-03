package com.dingxun.adapter.mqtt;

import com.google.inject.Inject;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.*;

/**
 * MQTT适配器工厂类：- 根据车辆配置创建MQTTCommAdapter实例 - 检查车辆协议是否为"MQTT"
 * 实现了VehicleCommAdapterFactory接口，
 * 负责创建MQTT通信适配器实例（MQTTCommAdapter）。
 * 它根据车辆的协议属性判断是否为MQTT协议，从而决定是否提供适配器。
 */
public class MQTTAdapterFactory
    implements VehicleCommAdapterFactory {

  private static final String PROTOCOL_MQTT = "MQTT";
  private final MQTTConfig config; // 注入的配置
  private final ScheduledExecutorServiceProvider executorProvider; // 执行器提供者
  private final VehicleService vehicleService; // 新增依赖
  private final RoutePlanner routePlanner;    // 新增依赖


  @Inject
  public MQTTAdapterFactory(
      MQTTConfig config,
      VehicleService vehicleService,
      RoutePlanner routePlanner,
      ScheduledExecutorServiceProvider executorProvider
  ) {
    this.config = config;
    this.executorProvider = executorProvider;
    this.vehicleService = vehicleService;
    this.routePlanner = routePlanner;
  }

  /**
   * 创建适配器实例
   *
   * @param vehicle 车辆对象
   */
  @Override
  public VehicleCommAdapter getAdapterFor(Vehicle vehicle) {
    return new MQTTCommAdapter(
        vehicleService,
        routePlanner,
        vehicle,
        config,
        new BasicVehicleProcessModel(vehicle), // 创建车辆状态模型
        executorProvider.get() // 获取执行器
    );
  }

  @Override
  public VehicleCommAdapterDescription getDescription() {
    return new MQTTCommAdapterDescription();
  }

  @Override
  public boolean providesAdapterFor(Vehicle vehicle) {
    return PROTOCOL_MQTT.equals(vehicle.getProperty("protocol"));
  }

  @Override
  public void initialize() {
    // 初始化逻辑（如有需要）
  }

  @Override
  public boolean isInitialized() {
    return true;
  }

  @Override
  public void terminate() {
    // 清理逻辑（如有需要）
  }
}
