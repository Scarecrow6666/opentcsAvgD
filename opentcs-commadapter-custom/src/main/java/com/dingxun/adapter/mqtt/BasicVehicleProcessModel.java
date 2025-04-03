package com.dingxun.adapter.mqtt;

import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleProcessModel;

/**
 * 车辆过程模型，管理车辆状态和外部参数：
 * - 跟踪车辆状态（空闲、执行中、错误等）
 * - 合并基础路径与外部动态参数
 * 继承自VehicleProcessModel，支持属性变更事件通知
 */
public class BasicVehicleProcessModel
    extends VehicleProcessModel {

  // 状态属性常量定义
  public static final String VEHICLE_STATE = "VEHICLE_STATE"; // 状态属性名
  public static final String EXTERNAL_PARAMS = "EXTERNAL_PARAMS"; // 参数属性名

  private final Vehicle vehicle; // 关联的车辆对象
  private Vehicle.State currentState = Vehicle.State.IDLE; // 当前状态
  private String externalParams = ""; // 外部参数（如路径附加条件）

  /**
   * 构造函数
   * @param vehicle 车辆对象，包含基础属性
   */
  public BasicVehicleProcessModel(Vehicle vehicle) {
    super(vehicle);
    this.vehicle = vehicle;
  }

  /** 获取车辆对象 */
  public Vehicle getVehicle() {
    return vehicle;
  }

  /** 获取当前状态（线程安全） */
  public synchronized Vehicle.State getVehicleState() {
    return currentState;
  }

  /**
   * 更新车辆状态并触发事件
   * @param newState 新状态（如Vehicle.State.EXECUTING）
   */
  public synchronized void setVehicleState(Vehicle.State newState) {
    Vehicle.State oldState = this.currentState;
    this.currentState = newState;
    getPropertyChangeSupport().firePropertyChange(
        VEHICLE_STATE, oldState, newState
    );
  }

  /** 设置外部参数并触发事件 */
  public synchronized void setExternalParams(String params) {
    String oldParams = this.externalParams;
    this.externalParams = params;
    getPropertyChangeSupport().firePropertyChange(
        EXTERNAL_PARAMS, oldParams, params
    );
  }

  /** 获取融合后的路径参数 */
  public String getMergedPathParams() {
    return vehicle.getProperties().getOrDefault("basePath", "")
        + "&" + externalParams;
  }

  @Override
  public String toString() {
    return "BasicVehicleProcessModel{"
        + "name=" + vehicle.getName()
        + ", state=" + currentState
        + ", params=" + externalParams
        + "}";
  }
}
