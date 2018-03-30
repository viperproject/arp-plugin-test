import sbt._
import Keys._
import sbtassembly.AssemblyPlugin.autoImport._
import de.oakgrove.SbtBrand.{BrandKeys, brandSettings, Val}
import de.oakgrove.SbtHgId.{HgIdKeys, hgIdSettings}
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

object ARPTestBuild extends Build {

  /* Base settings */

  lazy val baseSettings = (
       hgIdSettings
    ++ brandSettings
    ++ Seq(
          organization := "viper",
          version := "0.1-SNAPSHOT",
          scalaVersion := "2.11.8",
          scalacOptions in Compile ++= Seq(
            "-deprecation",
            "-unchecked",
            "-feature"
            /*"-Xfatal-warnings"*/),
          resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
          traceLevel := 10,
          maxErrors := 6))

  /* Projects */

  lazy val arpTest = {
    var p = Project(
      id = "arpTest",
      base = file("."),
      settings = (
           baseSettings
        ++ Seq(
              name := "ARPTest",
              mainClass in Compile := Some("viper.silicon.SiliconRunner"),
              mainClass in assembly := Some("viper.silicon.SiliconRunner"),
              jarName in assembly := "arp-plugin-test.jar",
              assemblyMergeStrategy in assembly := {
                case "logback.xml" => MergeStrategy.first
                case x =>
                    val oldStrategy = (assemblyMergeStrategy in assembly).value
                    oldStrategy(x)
              },
              test in assembly := {},
              fork := true,
              javaOptions in run ++= Seq("-Xss128M", "-Dfile.encoding=UTF-8"),
              javaOptions in Test += "-Xss128M",
              testOptions in Test += Tests.Argument("-oGK"),
              libraryDependencies ++= externalDep,
              BrandKeys.dataPackage := "viper.silver.plugin.arpTest",
              BrandKeys.dataObject := "brandingData",
              BrandKeys.data += Val("buildDate", new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new java.util.Date)),
              BrandKeys.data <+= scalaVersion(Val("scalaVersion", _)),
              BrandKeys.data <+= sbtBinaryVersion(Val("sbtBinaryVersion", _)),
              BrandKeys.data <+= sbtVersion(Val("sbtVersion", _)),
              BrandKeys.data <+= name(Val("sbtProjectName", _)),
              BrandKeys.data <+= version(Val("sbtProjectVersion", _)),
              BrandKeys.data <++= HgIdKeys.projectId(idOrException => {
                val id =
                  idOrException.fold(Predef.identity,
                                     _ => de.oakgrove.SbtHgId.Id("<unknown>", "<unknown>", "<unknown>", "<unknown>"))

                Seq(Val("hgid_version", id.version),
                    Val("hgid_id", id.id),
                    Val("hgid_branch", id.branch),
                    Val("hgid_tags", id.tags))
              }),
              sourceGenerators in Compile <+= BrandKeys.generateDataFile)
        ++ addCommandAlias("tn", "test-only -- -n "))
    )

    for (dep <- internalDep) {
      p = p.dependsOn(dep)
    }

    p.enablePlugins(JavaAppPackaging)
  }


  /* On the build-server, we cannot have all project in the same directory, and
   * thus we use the publish-local mechanism for dependencies.
   */
  def isBuildServer = sys.env.contains("BUILD_TAG") /* Should only be defined on the build server */

  def internalDep = if (isBuildServer) Nil else Seq(
    (dependencies.silSrc % "compile->compile;test->test"),
    (dependencies.arpSrc % "compile->compile;test->test"),
    (dependencies.siliconSrc % "compile->compile;test->test"),
    (dependencies.carbonSrc % "compile->compile;test->test")
  )

  def externalDep = (if (isBuildServer) Seq(
    (dependencies.sil % "compile->compile;test->test"),
    (dependencies.arp % "compile->compile;test->test"),
    (dependencies.silicon % "compile->compile;test->test"),
    (dependencies.carbon % "compile->compile;test->test")
    ) else Nil)

  /* Dependencies */

  object dependencies {
    lazy val sil = "viper" %% "silver" %  "0.1-SNAPSHOT"
    lazy val silSrc = RootProject(new java.io.File("../silver"))

    lazy val arp = "viper" %% "arp" %  "1.0-SNAPSHOT"
    lazy val arpSrc = RootProject(new java.io.File("../arp-plugin"))

    lazy val silicon = "viper" %% "silicon" %  "1.1-SNAPSHOT"
    lazy val siliconSrc = RootProject(new java.io.File("../silicon"))

    lazy val carbon = "viper" %% "carbon" %  "1.0-SNAPSHOT"
    lazy val carbonSrc = RootProject(new java.io.File("../carbon"))
  }
}
