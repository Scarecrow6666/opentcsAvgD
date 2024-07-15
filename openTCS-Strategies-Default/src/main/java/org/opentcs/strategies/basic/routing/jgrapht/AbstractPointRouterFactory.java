/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.Iterator;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.routing.PointRouter;
import org.opentcs.strategies.basic.routing.PointRouterFactory;
import org.opentcs.strategies.basic.routing.jgrapht.GraphProvider.GraphResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates {@link PointRouter} instances with algorithm implementations created by subclasses.
 */
public abstract class AbstractPointRouterFactory
    implements
      PointRouterFactory {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractPointRouterFactory.class);
  private final GraphProvider graphProvider;

  /**
   * Creates a new instance.
   *
   * @param graphProvider Provides routing graphs for vehicles.
   */
  public AbstractPointRouterFactory(
      @Nonnull
      GraphProvider graphProvider
  ) {
    this.graphProvider = requireNonNull(graphProvider, "graphProvider");
  }

  @Override
  public PointRouter createPointRouter(
      @Nonnull
      Vehicle vehicle,
      @Nonnull
      Set<Point> pointsToExclude,
      @Nonnull
      Set<Path> pathsToExclude
  ) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(pointsToExclude, "pointsToExclude");
    requireNonNull(pathsToExclude, "pathsToExclude");

    long timeStampBefore = System.currentTimeMillis();

    GraphProvider.GraphResult graphResult;
    if (pointsToExclude.isEmpty() && pathsToExclude.isEmpty()) {
      graphResult = graphProvider.getGraphResult(vehicle);
    }
    else {
      graphResult = graphProvider.getDerivedGraphResult(vehicle, pointsToExclude, pathsToExclude);
    }

    PointRouter router = createPointRouter(graphResult);

    LOG.debug(
        "Created point router for {} in {} milliseconds.",
        vehicle.getName(),
        System.currentTimeMillis() - timeStampBefore
    );

    return router;
  }

  @Override
  public PointRouter createGeneralPointRouter(
      @Nonnull
      Set<Point> pointsToExclude,
      @Nonnull
      Set<Path> pathsToExclude
  ) {
    requireNonNull(pointsToExclude, "pointsToExclude");
    requireNonNull(pathsToExclude, "pathsToExclude");

    long timeStampBefore = System.currentTimeMillis();

    GraphProvider.GraphResult graphResult;
    if (pointsToExclude.isEmpty() && pathsToExclude.isEmpty()) {
      graphResult = graphProvider.getGeneralGraphResult();
    }
    else {
      graphResult = graphProvider.getDerivedGeneralGraphResult(pointsToExclude, pathsToExclude);
    }

    PointRouter router = createPointRouter(graphResult);

    LOG.debug(
        "Created a general point router in {} milliseconds.",
        System.currentTimeMillis() - timeStampBefore
    );

    return router;
  }

  /**
   * Returns a shortest path algorithm implementation working on the given graph.
   *
   * @param graph The graph.
   * @return A shortest path algorithm implementation working on the given graph.
   */
  protected abstract ShortestPathAlgorithm<String, Edge> createShortestPathAlgorithm(
      Graph<String, Edge> graph
  );

  private PointRouter createPointRouter(GraphResult graphResult) {
    Set<Point> points = graphResult.getPointBase();

    PointRouter router = new ShortestPathPointRouter(
        createShortestPathAlgorithm(graphResult.getGraph()),
        points
    );
    // Make a single request for a route from one point to a different one to make sure the
    // point router is primed. (Some implementations are initialized lazily.)
    if (points.size() >= 2) {
      Iterator<Point> pointIter = points.iterator();
      router.getRouteSteps(pointIter.next(), pointIter.next());
    }

    return router;
  }
}
