package MapReduceService

import scala.collection.mutable.HashMap
import akka.actor.Actor
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.collection.mutable.HashMap

class ReduceActor extends Actor {


  override def preStart(): Unit = {
    println("ReduceActor Start Path is: " + self.path.toString)
  }
  val job1 = ReduceJob1()
  val job2 = ReduceJob2()
  val job3 = ReduceJob3()
  var currJob = 1
  val supervisor = context.actorSelection("akka://ClusterSystem@127.0.0.1:2552/user/supervisor")
  supervisor ! "WatchMe"

  var mappers = 3
  var reduceMap = HashMap[Any,Any]()
  def receive = {

    case Reduce(out_key, inter_val) =>
      //println(self.toString(), " received ", out)
      reduce(out_key, inter_val)
    case JobNum(num) =>
      println("Reducer got job type ", num)
      currJob = num

    case Flush =>
      mappers -= 1
      if (mappers == 0) {
        currJob match {
          case 1 =>
            println(self.path.toStringWithoutAddress + " : " + job1.reduceMap)
            job1.reduceMap.clear()
          case 2 =>
            println(self.path.toStringWithoutAddress + " : " + job2.reduceMap)
            job2.reduceMap.clear()
          case 3 =>
            println(self.path.toStringWithoutAddress + " : " + job3.reduceMap)
            job3.reduceMap.clear()
        }
        mappers = 3
      }
    case msg =>
      println("Misc Message ", msg)
  }

  def reduce[C, D](out_key: C, inter_val: D): Unit = {
    currJob match {
      case 1 => {
        job1.reduce(out_key.asInstanceOf[String], inter_val.asInstanceOf[Int])
      }
      case 2 => {
        job2.reduce(out_key.asInstanceOf[String], inter_val.asInstanceOf[String])
      }
      case 3 => {
        job3.reduce(out_key.asInstanceOf[String], inter_val.asInstanceOf[String])
      }

    }

    //        case "2" => map2(in_key, in_value)
      //        case "3" => map3(in_key, in_value)


  }

}
