package sbt

object compat {
  implicit class ConfigTypeRef(private val CR: ConfigRef.type) extends AnyVal {
    def wrap(cr: ConfigRef): ConfigRef = cr
  }
}

package librarymanagement {
package compat {

object exclude {
  def apply(org: String,
            name: String,
            crossVersion: CrossVersion): ExclusionRule =
    sbt.librarymanagement.InclExclRule(
      org,
      name,
      "*",
      Vector(),
      crossVersion
    )
}

}
}
