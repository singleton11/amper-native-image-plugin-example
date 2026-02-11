import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import kotlinx.coroutines.runBlocking
import org.jetbrains.amper.core.AmperUserCacheInitializationFailure
import org.jetbrains.amper.core.AmperUserCacheRoot
import org.jetbrains.amper.frontend.schema.JvmDistribution
import org.jetbrains.amper.incrementalcache.IncrementalCache
import org.jetbrains.amper.jdk.provisioning.JdkProvider
import org.jetbrains.amper.jdk.provisioning.JdkProvisioningCriteria
import org.jetbrains.amper.jdk.provisioning.orThrow
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.div
import kotlin.io.path.exists

@TaskAction
fun provision(
    graalVmVersion: String,
    graalVmDistribution: GraalVmDistribution,
    acknowledgeLicense: Boolean,
    @Output output: Path,
) {
    require(graalVmVersion.isNotBlank()) {
        "graalVmVersion must be set when GRAALVM_HOME/JAVA_HOME are not configured"
    }
    val graalVmHome = provisionGraalVmHome(
        distribution = graalVmDistribution,
        version = graalVmVersion,
        acknowledgeLicense = acknowledgeLicense,
    )
    output.createParentDirectories()
    output.toFile().writeText(graalVmHome)
}


private fun provisionGraalVmHome(
    distribution: GraalVmDistribution,
    version: String,
    acknowledgeLicense: Boolean = false,
): String {
    val majorVersion = parseJavaMajorVersion(version)
    val userCacheRoot = amperUserCacheRoot()
    val incrementalCache = IncrementalCache(
        stateRoot = userCacheRoot.path / "incremental.state" / "native-image-plugin",
        codeVersion = "native-image-plugin",
    )
    val jdkProvider = JdkProvider(
        userCacheRoot = userCacheRoot,
        incrementalCache = incrementalCache,
    )

    val jdk = runBlocking {
        jdkProvider.provisionJdk(
            criteria = JdkProvisioningCriteria(
                majorVersion = majorVersion,
                distributions = listOf(distribution.toJvmDistribution()),
                acknowledgedLicenses = if (acknowledgeLicense) listOf(JvmDistribution.OracleGraalVM) else emptyList(),
            )
        )
    }.orThrow()

    val nativeImagePath = jdk.homeDir / "bin" / nativeImageExecutableName()
    require(nativeImagePath.exists()) {
        "Provisioned GraalVM installation doesn't contain `${nativeImageExecutableName()}` at $nativeImagePath"
    }
    return jdk.homeDir.toString()
}

private fun amperUserCacheRoot(): AmperUserCacheRoot {
    return when (val result = AmperUserCacheRoot.fromCurrentUserResult()) {
        is AmperUserCacheRoot -> result
        is AmperUserCacheInitializationFailure -> error(result.defaultMessage)
    }
}

private fun parseJavaMajorVersion(raw: String): Int {
    val trimmed = raw.trim()
    val majorPrefix = trimmed.takeWhile { it.isDigit() }
    return majorPrefix.toIntOrNull()
        ?: error("graalVmVersion must start with a Java major version number (e.g. 21), but was: '$raw'")
}

private fun GraalVmDistribution.toJvmDistribution(): JvmDistribution = when (this) {
    GraalVmDistribution.ORACLE -> JvmDistribution.OracleGraalVM
    GraalVmDistribution.COMMUNITY -> JvmDistribution.GraalVMCommunityEdition
}
