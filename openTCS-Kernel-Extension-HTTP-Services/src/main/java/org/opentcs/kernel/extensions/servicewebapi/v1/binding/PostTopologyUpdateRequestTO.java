/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * A list of paths that are to be updated when the routing topology gets updated.
 */
public class PostTopologyUpdateRequestTO {

  @Nonnull
  private List<String> paths;

  @JsonCreator
  public PostTopologyUpdateRequestTO(
      @Nonnull
      @JsonProperty(value = "paths", required = true)
      List<String> paths
  ) {
    this.paths = requireNonNull(paths, "paths");
  }

  @Nonnull
  public List<String> getPaths() {
    return paths;
  }

  public PostTopologyUpdateRequestTO setPaths(
      @Nonnull
      List<String> paths
  ) {
    this.paths = requireNonNull(paths, "paths");
    return this;
  }
}
