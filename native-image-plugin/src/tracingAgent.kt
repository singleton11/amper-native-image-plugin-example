import org.jetbrains.amper.plugins.Classpath
import org.jetbrains.amper.plugins.CompilationArtifact
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.ModuleSources
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.exists

@TaskAction
fun tracingAgent(
    @Input graalVmHomePath: Path,
    @Input jarPath: CompilationArtifact,
    @Input classpath: Classpath,
    mainClass: String,
    @Output resources: ModuleSources,
) {
    val graalVmHome = graalVmHomePath.toFile().readText().trim()
    val javaExe = Path.of(graalVmHome) / "bin" / javaExecutableName()

    require(javaExe.exists()) { "java executable not found at $javaExe" }

    val outputDir = resources.sourceDirectories.first() / "META-INF/native-image/"
    outputDir.createDirectories()

    val allFiles = listOf(jarPath.artifact) + classpath.resolvedFiles
    val fullClasspath = allFiles.joinToString(File.pathSeparator)

    val process = ProcessBuilder(
        javaExe.toString(),
        "-agentlib:native-image-agent=config-output-dir=${outputDir}",
        "-cp", fullClasspath,
        mainClass,
    ).inheritIO().start()

    val exitCode = process.waitFor()
    require(exitCode == 0) {
        "Tracing agent failed with exit code $exitCode"
    }
}
