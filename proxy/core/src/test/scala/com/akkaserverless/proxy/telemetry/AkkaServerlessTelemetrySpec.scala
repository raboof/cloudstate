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

package com.akkaserverless.proxy.telemetry

import akka.testkit.EventFilter
import io.prometheus.client.CollectorRegistry

class AkkaServerlessTelemetrySpec extends AbstractTelemetrySpec {

  "AkkaServerlessTelemetry" should {

    "be enabled in production mode by default" in withTestRegistry() { testKit =>
      AkkaServerlessTelemetry(testKit.system).settings.enabled shouldBe true
    }

    "be disabled in development mode by default" in withTestKit("""
      | akkaserverless.proxy.dev-mode-enabled = true
      """) { testKit =>
      AkkaServerlessTelemetry(testKit.system).settings.disabled shouldBe true
    }

    "allow telemetry to be enabled in development mode" in withTestRegistry(
      """
      | akkaserverless.proxy.dev-mode-enabled = true
      | akkaserverless.proxy.telemetry.disabled = false
      """
    ) { testKit =>
      AkkaServerlessTelemetry(testKit.system).settings.enabled shouldBe true
    }

    "use Prometheus default registry by default" in withTestKit(
      """
      | # disable telemetry so we don't register any metrics with the global default registry
      | akkaserverless.proxy.telemetry.disabled = true
      """
    ) { testKit =>
      AkkaServerlessTelemetry(testKit.system).prometheusRegistry should be theSameInstanceAs CollectorRegistry.defaultRegistry
    }

    "create separate Prometheus registry if configured" in withTestKit(
      """
      | akkaserverless.proxy.telemetry.prometheus.use-default-registry = false
      """
    ) { testKit =>
      AkkaServerlessTelemetry(testKit.system).prometheusRegistry shouldNot be theSameInstanceAs CollectorRegistry.defaultRegistry
    }

    "start Prometheus exporter on default port (9090)" in withTestRegistry(
      """
      | akka.loggers = ["akka.testkit.TestEventListener"]
      """
    ) { testKit =>
      import testKit.system
      EventFilter.info(start = "Prometheus exporter started", occurrences = 1).intercept {
        AkkaServerlessTelemetry(system).start()
      }
      val metrics = scrape("http://localhost:9090")(_.mkString)
      metrics should include("# TYPE akkaserverless_eventsourced")
    }

    "allow Prometheus metrics port to be configured" in withTestRegistry(
      """
      | akkaserverless.proxy.telemetry.prometheus.port = 9999
      | akka.loggers = ["akka.testkit.TestEventListener"]
      """
    ) { testKit =>
      import testKit.system
      EventFilter.info(start = "Prometheus exporter started", occurrences = 1).intercept {
        AkkaServerlessTelemetry(system).start()
      }
      val metrics = scrape("http://localhost:9999")(_.mkString)
      metrics should include("# TYPE akkaserverless_eventsourced")
    }

    "bind event-sourced instrumentation to Prometheus by default" in withTestRegistry() { testKit =>
      AkkaServerlessTelemetry(testKit.system).eventSourcedInstrumentation shouldBe a[
        PrometheusEventSourcedInstrumentation
      ]
    }

    "use noop event-sourced instrumentation when disabled" in withTestKit(
      """
      | akkaserverless.proxy.telemetry.disabled = true
      """
    ) { testKit =>
      AkkaServerlessTelemetry(testKit.system).eventSourcedInstrumentation should be theSameInstanceAs NoEventSourcedInstrumentation
    }

    "bind active event-sourced entity instrumentation by default" in withTestRegistry() { testKit =>
      AkkaServerlessTelemetry(testKit.system)
        .eventSourcedEntityInstrumentation("name") shouldBe an[ActiveEventSourcedEntityInstrumentation]
    }

    "use noop event-sourced entity instrumentation when disabled" in withTestKit(
      """
      | akkaserverless.proxy.telemetry.disabled = true
      """
    ) { testKit =>
      AkkaServerlessTelemetry(testKit.system)
        .eventSourcedEntityInstrumentation("name") should be theSameInstanceAs NoEventSourcedEntityInstrumentation
    }
  }
}
