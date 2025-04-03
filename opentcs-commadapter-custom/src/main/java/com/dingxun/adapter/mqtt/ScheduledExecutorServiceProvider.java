package com.dingxun.adapter.mqtt;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 提供调度执行器服务：
 * - 创建守护线程池，用于处理MQTT适配器的异步任务
 * - 线程池大小为2，线程名称为"mqtt-adapter-executor"
 */
public class ScheduledExecutorServiceProvider {

  private static final int THREAD_POOL_SIZE = 2;

  /**
   * 创建并返回调度执行器实例
   */
  public ScheduledExecutorService get() {
    return Executors.newScheduledThreadPool(
        THREAD_POOL_SIZE,
        r -> {
          Thread t = new Thread(r, "mqtt-adapter-executor");
          t.setDaemon(true); // 设置为守护线程
          return t;
        }
    );
  }
}
