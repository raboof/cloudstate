import sbt.Keys._
import sbt._

object NoPublish extends AutoPlugin {
  override def projectSettings = Seq(
    publish / skip := true
  )
}
