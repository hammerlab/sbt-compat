package sbt

// sbt 1 -> sbt 0.13 compatibility
// Exposes (a minimal part) of the sbt 1 API using sbt 0.13 API
// Inspired by macro-compat

object compat {
  val scalaModuleInfo = SettingKey[Option[librarymanagement.ScalaModuleInfo]]("ivyScala")

  implicit class ModuleIdOps(val _m: ModuleID) extends AnyVal {
    def withName(n: String): ModuleID = _m copy (name = n)
  }
}

package io {
  object `package` {
    type Using[Source, T] = sbt.Using[Source, T]
    val Using = sbt.Using
  }
}

package librarymanagement {
  object `package` {
    final val UpdateLogging = sbt.UpdateLogging
    type ModuleDescriptor = IvySbt#Module
    type UpdateLogging = UpdateLogging.Value
    type UpdateConfiguration = sbt.UpdateConfiguration
    type ScalaModuleInfo = IvyScala

    implicit class UpdateConfigurationOps(val _uc: UpdateConfiguration) extends AnyVal {
      def withLogging(ul: UpdateLogging): UpdateConfiguration = _uc copy (logging = ul)
    }

    implicit class RichUpdateReport(val _ur: UpdateReport) extends AnyVal {
      def configuration(c: ConfigRef) = _ur configuration c.name
    }

    type Disabled = sbt.CrossVersion.Disabled.type
    def Disabled() = sbt.CrossVersion.Disabled

    type ModuleID = sbt.ModuleID
    val ModuleID = sbt.ModuleID

    val Developer = sbt.Developer
    val ScmInfo = sbt.ScmInfo

    object syntax {
      type ExclusionRule = sbt.SbtExclusionRule
      val ExclusionRule = sbt.SbtExclusionRule
    }

    object compat {
      /**
       * This function is implemented here and for 1.0  to go from un-crossversioned Maven coordinates to a
       * (cross-versioned) Ivy-style exclusion-rule.
       */
      def exclude(org: String,
                  name: String,
                  crossVersion: CrossVersion)(
          implicit
          crossVersionFn: (CrossVersion, String) ⇒ String
      ): sbt.ExclusionRule =
        sbt.ExclusionRule(
          org,
          crossVersionFn(crossVersion, name)
        )
    }

  }

  final case class ConfigRef(name: String)

  object ConfigRef {
    def wrap(s: String): ConfigRef = ConfigRef(s)
  }

  object UpdateConfiguration {
    def apply() = new UpdateConfiguration(None, false, UpdateLogging.Default)
  }

  class DependencyResolution(ivy: IvySbt) {
    def wrapDependencyInModule(m: ModuleID): ModuleDescriptor = {
      val moduleSettings = InlineConfiguration(
        "dummy" % "test" % "version",
        ModuleInfo("dummy-test-project-for-resolving"),
        dependencies = Seq(m)
      )
      new ivy.Module(moduleSettings)
    }

    def update(
        module: ModuleDescriptor,
        configuration: UpdateConfiguration,
        uwconfig: UnresolvedWarningConfiguration,
        log: Logger
    ): Either[UnresolvedWarning, UpdateReport] =
      IvyActions.updateEither(
        module,
        new UpdateConfiguration(
          retrieve = None,
          missingOk = false,
          logging = UpdateLogging.DownloadOnly
        ),
        UnresolvedWarningConfiguration(),
        LogicalClock.unknown,
        None,
        log
      )
  }

  package ivy {
    object IvyDependencyResolution {
      def apply(configuration: IvyConfiguration): DependencyResolution =
        new DependencyResolution(new IvySbt(configuration))
    }
  }
}

package internal {
  package librarymanagement {
    object `package`
  }

  package util {
    object JLine {
      def usingTerminal[T](f: jline.Terminal => T): T =
        sbt.JLine.usingTerminal(f)
    }
  }

  object `package` {
    type BuildStructure = sbt.BuildStructure
    type LoadedBuildUnit = sbt.LoadedBuildUnit
  }
}
