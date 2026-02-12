import org.jetbrains.amper.plugins.Classpath
import org.jetbrains.amper.plugins.CompilationArtifact
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.div
import kotlin.io.path.exists

@TaskAction
fun buildNativeImage(
    @Input graalVmHomePath: Path,
    @Input classpath: Classpath,
    mainClass: String,
    @Output output: Path,
) {
    val graalVmHome = graalVmHomePath.toFile().readText().trim()
    val nativeImageExe = Path.of(graalVmHome) / "bin" / nativeImageExecutableName()

    require(nativeImageExe.exists()) {
        "native-image executable not found at $nativeImageExe"
    }

    output.createParentDirectories()

    val fullClasspath = classpath.resolvedFiles.joinToString(File.pathSeparator)

    val process = ProcessBuilder(
        nativeImageExe.toString(),
        "-cp", fullClasspath,
        mainClass,
        "-o", output.toString(),
        "--no-fallback",
    ).inheritIO().start()

    val exitCode = process.waitFor()
    require(exitCode == 0) {
        "native-image build failed with exit code $exitCode"
    }
}
