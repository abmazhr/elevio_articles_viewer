package application.utils

object MenuUtils {
  def getMenuWireFrame(menuLines: List[String], trimLastEndLine: Boolean = false): String =
    if (trimLastEndLine) {
      menuLines.foldLeft("")(_.concat(_).concat("\n")).trim
    } else {
      menuLines.foldLeft("")(_.concat(_).concat("\n"))
    }

  def clearScreen() {
    System.out.print("\033[H\033[2J")
    System.out.flush()
  }
}
