import sbt._
import sbt.Keys._

object NoPublish extends AutoPlugin {
  override def projectSettings = Seq(
    publish / skip := true
  )
}
