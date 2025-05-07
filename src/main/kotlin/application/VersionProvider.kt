package se.gu.application

import picocli.CommandLine
import se.gu.neo4jud.BuildConfig
import java.text.SimpleDateFormat

class VersionProvider : CommandLine.IVersionProvider {

    val version: String
        get() = BuildConfig.APP_VERSION

    val buildTime: String
        get() {
            val simpleDateFormat = SimpleDateFormat("yyyy-MMM-dd HH:mm:ss")
            return simpleDateFormat.format(BuildConfig.BUILD_TIME)
        }

    override fun getVersion(): Array<out String> =
        arrayOf(
            "neo4jud version $version, build time: $buildTime"
        )
}