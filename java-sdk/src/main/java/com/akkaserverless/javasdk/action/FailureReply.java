/*
 * Copyright 2019 Lightbend Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.akkaserverless.javasdk.action;

import java.util.Collection;

/** A failure reply. */
public interface FailureReply<T> extends ActionReply<T> {

  /**
   * The description of the failure.
   *
   * @return The failure description.
   */
  String description();

  FailureReply<T> withEffects(Collection<Effect> effects);

  FailureReply<T> withEffects(Effect... effects);
}