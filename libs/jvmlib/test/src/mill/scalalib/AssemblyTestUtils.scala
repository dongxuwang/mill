package mill.scalalib

import mill.*
import mill.api.Discover
import mill.util.Jvm
import mill.testkit.TestRootModule

trait AssemblyTestUtils {

  object TestCase extends TestRootModule {
    trait Setup extends ScalaModule {
      def scalaVersion = "2.13.11"

      def sources = Task.Sources(mill.api.BuildCtx.workspaceRoot / "src")

      def mvnDeps = super.mvnDeps() ++ Seq(
        mvn"com.lihaoyi::scalatags:0.8.2",
        mvn"com.lihaoyi::mainargs:0.4.0",
        mvn"org.apache.avro:avro:1.11.1"
      )
    }

    trait ExtraDeps extends ScalaModule {
      def mvnDeps = super.mvnDeps() ++ Seq(
        mvn"dev.zio::zio:2.0.15",
        mvn"org.typelevel::cats-core:2.9.0",
        mvn"org.apache.spark::spark-core:3.4.0",
        mvn"dev.zio::zio-metrics-connectors:2.0.8",
        mvn"dev.zio::zio-http:3.0.0-RC2"
      )
    }

    object noExe extends Module {
      object small extends Setup {
        override def prependShellScript: T[String] = ""
      }

      object large extends Setup with ExtraDeps {
        override def prependShellScript: T[String] = ""
      }
    }

    object exe extends Module {
      object small extends Setup

      object large extends Setup with ExtraDeps
    }

    lazy val millDiscover = Discover[this.type]
  }

  val sources = os.Path(sys.env("MILL_TEST_RESOURCE_DIR")) / "assembly"
  def runAssembly(file: os.Path, wd: os.Path, checkExe: Boolean = false): Unit = {
    println(s"File size: ${os.stat(file).size}")
    os.call(
      cmd = Seq(Jvm.javaExe, "-jar", file.toString(), "--text", "tutu"),
      env = Map.empty[String, String],
      cwd = wd,
      stdin = os.Inherit,
      stdout = os.Inherit
    )

    if (checkExe) {
      os.call(
        cmd = Seq(file.toString(), "--text", "tutu"),
        env = Map.empty[String, String],
        cwd = wd,
        stdin = os.Inherit,
        stdout = os.Inherit
      )
    }
  }
}
