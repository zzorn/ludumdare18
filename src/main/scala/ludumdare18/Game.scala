package ludumdare18

/**
 * 
 */
object Game {

  var quit = false

  def main(args: Array[String]) {
    val screen = new Screen()

    screen.start()

    mainLoop()
  }

  def mainLoop() {

    while (!quit) {
      update()
      
      Thread.sleep(10)
    }

  }

  def update() {

  }

}