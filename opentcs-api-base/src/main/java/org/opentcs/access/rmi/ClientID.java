// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.rmi;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.util.UUID;

/**
 * Identifies a remote client unambiguously.
 */
public class ClientID
    implements
      Serializable {
  /**
   * The client's name.
   */
  private final String clientName;
  /**
   * The client's UUID.
   */
  private final UUID uuid;

  /**
   * Creates a new ClientID.
   *
   * @param clientName The client's name.
   */
  public ClientID(
      @Nonnull
      String clientName
  ) {
    this.clientName = requireNonNull(clientName, "clientName");
    uuid = UUID.randomUUID();
  }

  /**
   * Return the client's name.
   *
   * @return The client's name.
   */
  @Nonnull
  public String getClientName() {
    return clientName;
  }

  /**
   * Checks if this object equals another one.
   *
   * @param otherObject The other object to be compared with this object.
   * @return <code>true</code> if, and only if, the given object is also a
   * <code>ClientID</code> and contains the same name and the same UUID as this
   * one.
   */
  @Override
  public boolean equals(Object otherObject) {
    if (otherObject instanceof ClientID) {
      ClientID otherID = (ClientID) otherObject;
      return clientName.equals(otherID.clientName) && uuid.equals(otherID.uuid);
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return uuid.hashCode();
  }

  @Override
  public String toString() {
    return clientName + ":" + uuid;
  }
}
