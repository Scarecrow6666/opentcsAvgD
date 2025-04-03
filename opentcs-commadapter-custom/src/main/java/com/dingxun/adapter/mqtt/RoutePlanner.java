package com.dingxun.adapter.mqtt;

import org.opentcs.data.model.Point;
import java.util.List;

/**
 * 路径规划器接口（伪实现）
 */
public interface RoutePlanner {
  List<Point> generatePath(String fromPoint, String toPoint);
}
