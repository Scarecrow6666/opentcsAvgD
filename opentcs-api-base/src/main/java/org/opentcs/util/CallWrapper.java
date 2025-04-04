// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util;

import java.util.concurrent.Callable;

/**
 * Provides methods for wrapping other method calls.
 * This can be useful to ensure preparations have been done for a larger set of various method
 * calls, for example.
 */
public interface CallWrapper {

  /**
   * Calls a method that has a return value.
   *
   * @param <R> The return value's type.
   * @param callable The method wrapped in a {@link Callable} instance.
   * @return The result of the method's call.
   * @throws Exception If there was an exception calling method.
   */
  <R> R call(Callable<R> callable)
      throws Exception;

  /**
   * Calls a mehtod that has no return value.
   *
   * @param runnable The method wrapped in a {@link Runnable} instance.
   * @throws Exception If there was an exception calling method.
   */
  void call(Runnable runnable)
      throws Exception;
}
