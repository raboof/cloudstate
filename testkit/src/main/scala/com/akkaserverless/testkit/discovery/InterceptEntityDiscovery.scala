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

package com.akkaserverless.testkit.discovery

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.testkit.{TestKit, TestProbe}
import com.akkaserverless.protocol.action.ActionProtocol
import com.akkaserverless.protocol.crdt.Crdt
import com.akkaserverless.protocol.entity._
import com.akkaserverless.protocol.event_sourced.EventSourced
import com.akkaserverless.protocol.value_entity.ValueEntity
import com.akkaserverless.testkit.BuildInfo
import com.akkaserverless.testkit.InterceptService.InterceptorContext
import com.google.protobuf.empty.{Empty => ScalaPbEmpty}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

final class InterceptEntityDiscovery(context: InterceptorContext) {
  import InterceptEntityDiscovery._

  private val interceptor = new EntityDiscoveryInterceptor(context)

  def expectOnline(timeout: FiniteDuration): Unit = InterceptEntityDiscovery.expectOnline(context, timeout)

  def expectDiscovery(): Discovery = context.probe.expectMsgType[Discovery]

  def handler: PartialFunction[HttpRequest, Future[HttpResponse]] =
    EntityDiscoveryHandler.partial(interceptor)(context.system)

  def terminate(): Unit = interceptor.terminate()
}

object InterceptEntityDiscovery {
  final class EntityDiscoveryInterceptor(context: InterceptorContext) extends EntityDiscovery {
    private val client = EntityDiscoveryClient(context.clientSettings)(context.system)
    private val discovery = new Discovery(context)

    override def discover(info: ProxyInfo): Future[EntitySpec] = {
      context.probe.ref ! discovery
      discovery.in.ref ! info
      client
        .discover(info)
        .andThen {
          case Success(spec) => discovery.out.ref ! spec
          case Failure(error) => discovery.out.ref ! error
        }(context.system.dispatcher)
    }

    override def reportError(error: UserFunctionError): Future[ScalaPbEmpty] = {
      discovery.in.ref ! error
      client.reportError(error)
    }

    def terminate(): Unit = client.close()
  }

  final class Discovery(context: InterceptorContext) {
    private[testkit] val in = TestProbe("DiscoveryInProbe")(context.system)
    private[testkit] val out = TestProbe("DiscoveryOutProbe")(context.system)

    def expectProxyInfo(): ProxyInfo = in.expectMsgType[ProxyInfo]
    def expectEntitySpec(): EntitySpec = out.expectMsgType[EntitySpec]
  }

  val testProxyInfo: ProxyInfo = ProxyInfo(
    protocolMajorVersion = BuildInfo.protocolMajorVersion,
    protocolMinorVersion = BuildInfo.protocolMinorVersion,
    proxyName = BuildInfo.name,
    proxyVersion = BuildInfo.version,
    supportedEntityTypes = Seq(ActionProtocol.name, Crdt.name, EventSourced.name, ValueEntity.name)
  )

  def expectOnline(context: InterceptorContext, timeout: FiniteDuration): Unit = {
    val client = EntityDiscoveryClient(context.clientSettings)(context.system)
    try {
      var discovery: Try[EntitySpec] = null
      if (!TestKit.awaitCond({
            discovery = Await.ready(client.discover(testProxyInfo), timeout).value.get
            discovery.isSuccess
          }, timeout, noThrow = true))
        throw new AssertionError(s"No discovery after $timeout, last error: ${discovery.failed.get.getMessage}")
    } finally client.close()
  }
}
