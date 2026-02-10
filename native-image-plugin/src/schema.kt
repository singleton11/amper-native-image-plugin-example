import org.jetbrains.amper.plugins.Configurable


@Configurable
interface Schema {
    val version: String get() = "25"
    val distribution: GraalVmDistribution get() = GraalVmDistribution.ORACLE
    val mainClass: String
}

enum class GraalVmDistribution {
    ORACLE,
    COMMUNITY,
}
