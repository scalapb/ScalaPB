import sbt.Keys._

object ConsoleHelper {
  def logo(text: String): String = s"${Console.CYAN}$text${Console.RESET}"

  def item(cmd: String, helptext: String): String =
    s"${Console.RED}> ${Console.GREEN}${cmd.padTo(18, ' ')}${Console.RESET} - $helptext"

  def header =
    Seq(
      raw"""   _____             _       _____  ____  """,
      raw"""  / ____|           | |     |  __ \|  _ \ """,
      raw""" | (___   ___   __ _| | __ _| |__) | |_) |""",
      raw"""  \___ \ / __| / _` | |/ _` |  ___/|  _ < """,
      raw"""  ____) | (__ | (_| | | (_| | |    | |_) |""",
      raw""" |_____/ \___| \__,_|_|\__,_|_|    |____/  VERSION"""
    ).map(logo) ++ Seq(
      "",
      "Useful sbt tasks:",
      item("fmt", "Run scalafmt on the entire project."),
      item("e2eJVM2_12/test", "Run end-to-end for Scala 2.12"),
      item("e2eJVM2_13/test", "Run end-to-end for Scala 2.13"),
      item("docs/mdoc --watch", "Generate docs markdown through mdoc. See docs/README.md")
    )

  def welcomeMessage =
    onLoadMessage :=
      header
        .mkString("\n")
        .replace("VERSION", version.value)
}
