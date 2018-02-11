/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package viper.carbon.tests

import java.nio.file.Path

import viper.silver.frontend.Frontend
import viper.silver.reporter.{NoopReporter, Reporter, StdIOReporter}
import viper.silver.testing.SilSuite
import viper.silver.verifier.Verifier
import viper.carbon.{CarbonFrontend, CarbonVerifier}

class CarbonArpTests extends SilSuite {
  private val arpTestDirectories = Seq("arp")
  private val siliconTestDirectories = Seq("consistency")
  private val silTestDirectories = Seq("all", "quantifiedpermissions", "wands", "examples", "quantifiedpredicates" ,"quantifiedcombinations")

  val testDirectories = arpTestDirectories ++ siliconTestDirectories ++ silTestDirectories

  override def frontend(verifier: Verifier, files: Seq[Path]): Frontend = {
    require(files.length == 1, "tests should consist of exactly one file")

    val fe = new CarbonFrontend(NoopReporter)
    fe.init(verifier)
    fe.reset(files.head)
    fe
  }

  override def projectInfo: ProjectInfo = super.projectInfo.update("ARP")

  lazy val verifiers = List(createCarbonInstance())

  val commandLineArguments: Seq[String] = Seq("--plugin", "viper.silver.plugin.ARPPlugin")

  private def createCarbonInstance() = {
    val args = commandLineArguments
    val debugInfo = ("startedBy" -> "viper.carbon.tests.CarbonArpTests") :: Nil
    val carbon = CarbonVerifier(debugInfo)
    carbon.parseCommandLine(args :+ "dummy-file-to-prevent-cli-parser-from-complaining-about-missing-file-name.silver")
    println(s"config: ${carbon.config.plugin}")

    carbon
  }

}
