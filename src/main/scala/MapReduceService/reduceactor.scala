package MapReduceService

import akka.actor.Actor
import com.typesafe.config.ConfigFactory

import scala.collection.mutable.HashMap

class ReduceActor extends Actor {

  override def preStart(): Unit = {
    println("ReduceActor Start Path is: " + self.path.toString)
  }


  def receive = {

    case Reduce(out_key, inter_val) =>
      println("ReduceActor received ", out_key)
    case msg =>
      println("Misc Message ", msg)
//    case Flush =>
//      remainingMappers -= 1
//      if (remainingMappers == 0) {
//        println(self.path.toStringWithoutAddress + " : " + reduceMap)
//        context.actorSelection("../..") ! Done
//        // context stop self
//      }
  }
}
