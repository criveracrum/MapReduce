package MapReduceService

import scala.collection.mutable.HashMap
import akka.actor.Actor
import com.typesafe.config.ConfigFactory

import scala.collection.mutable.HashMap

class ReduceActor extends Actor {


  override def preStart(): Unit = {
    println("ReduceActor Start Path is: " + self.path.toString)
  }

  var mappers = 2
  var reduceMap = HashMap[Any,List[Any]]()
  def receive = {

    case Reduce(out_key, inter_val) =>
      //println(self.toString(), " received ", out)
      reduce(out_key, inter_val)

    case Flush =>
      mappers -= 1
      if (mappers == 0) {
        println(self.path.toStringWithoutAddress + " : " + reduceMap)
        // context stop self
      }
    case msg =>
      println("Misc Message ", msg)
  }

  def reduce[X,Y](out_key: X, inter_val: Y): Unit = {

    if (reduceMap.contains(out_key)){
      if (!reduceMap(out_key).contains(inter_val))
        reduceMap += (out_key -> (inter_val :: reduceMap(out_key)))
    } else {
      reduceMap += (out_key -> List(inter_val))
    }

    //println(reduceMap)
  }

}
