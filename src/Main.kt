import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import java.io.IOException

fun main() = try {
    // Create a terminal
    val terminal: Terminal = TerminalBuilder.builder().system(true).build()

    // Create a line reader
    val reader: LineReader = LineReaderBuilder.builder().terminal(terminal).build()

    // Read lines from the user
    while (true) {
        val line = reader.readLine("prompt> ")

        // Exit if requested
        if ("exit".equals(line, ignoreCase = true)) {
            break
        }

        // Echo the line back to the user
        terminal.writer().println("You entered: $line")
        terminal.flush()
    }

    terminal.writer().println("Goodbye!")
    terminal.close()
} catch (e: IOException) {
    System.err.println("Error creating terminal: " + e.message)
}
