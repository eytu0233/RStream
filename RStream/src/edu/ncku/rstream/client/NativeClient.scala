package edu.ncku.uscc

import java.net._
import java.io._
import scala.io._

object NativeClient {

  def main(args: Array[String]) {
    val socket = new Socket(InetAddress.getByName("localhost"), 9999)
    val input = new BufferedInputStream(socket.getInputStream())
    val out = new BufferedOutputStream(new FileOutputStream("result.txt"))

    var b = 0
    b = input.read()

    while (b != -1) {
      out.write(b)
      out.flush()
      b = input.read()
    }

    out.flush()
    out.close()

    socket.close()
  }

}