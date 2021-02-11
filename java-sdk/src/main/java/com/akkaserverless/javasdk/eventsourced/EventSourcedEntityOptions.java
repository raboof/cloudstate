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

package com.akkaserverless.javasdk.eventsourced;

import com.akkaserverless.javasdk.EntityOptions;
import com.akkaserverless.javasdk.PassivationStrategy;
import com.akkaserverless.javasdk.impl.eventsourced.EventSourcedEntityOptionsImpl;

/** Root entity options for all event sourcing entities. */
public interface EventSourcedEntityOptions extends EntityOptions {

  EventSourcedEntityOptions withPassivationStrategy(PassivationStrategy strategy);

  /**
   * Create a default entity option for an event sourced entity.
   *
   * @return the entity option
   */
  static EventSourcedEntityOptions defaults() {
    return new EventSourcedEntityOptionsImpl(PassivationStrategy.defaultTimeout());
  }
}
