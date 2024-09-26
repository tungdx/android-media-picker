import model.GitInfo
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import javax.inject.Inject

abstract class GitInfoSource : ValueSource<GitInfo, ValueSourceParameters.None> {

    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): GitInfo? {
        val branch = getOutput("git rev-parse --abbrev-ref HEAD").trim()
        val rev = getOutput("git rev-parse HEAD").trim()
        return GitInfo(branch, rev)
    }

    private fun getOutput(cmd: String): String {
        val output = ByteArrayOutputStream()
        execOperations.exec {
            commandLine(cmd.split(" "))
            standardOutput = output
        }
        return String(output.toByteArray(), Charset.defaultCharset())
    }
}