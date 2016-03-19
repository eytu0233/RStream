package edu.ncku.rstream.server

import java.net.ServerSocket
import scala.io.BufferedSource
import java.io.PrintStream
import java.io.File
import scala.io.Source
import java.io.FileInputStream
import java.io.BufferedInputStream
import java.io.BufferedOutputStream


object NativeServer {
  
  def main(args: Array[String]) {
    
    if(args.length < 1){
      System.out.println("Usage: NativeServer <fileName>")
      System.exit(1)
    }
    
    val buffer = new Array[Byte](1024)
    
    val serverSocket = new ServerSocket(9999)
    
    while (true) {
      val linkIns = serverSocket.accept()
      val out = new BufferedOutputStream(linkIns.getOutputStream())
      
      val bis = new BufferedInputStream(new FileInputStream(args(0)))
      
      val start = System.currentTimeMillis
      
      var b = 0
      b = bis.read()
      
      while(b != -1){  
        out.write(b)
        out.flush()
        b = bis.read()
      }
         
      bis.close()
      out.close()   
      linkIns.close()
      
      val end = System.currentTimeMillis
      val interval: Double = ((end - start).toDouble./(1000))
      printf("Cost time : %.3f seconds.\n", interval)
    }
  }
  
}