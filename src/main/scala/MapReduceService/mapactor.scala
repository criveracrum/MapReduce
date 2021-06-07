package MapReduceService

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, UnreachableMember}
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope
import akka.routing.{Broadcast, FromConfig}

import scala.collection.mutable.{HashMap, HashSet}
import scala.io.Source

class MapActor extends Actor {



  val reduceRouter = context.actorOf(FromConfig.props(Props[ReduceActor]()),
    name = "reduceRouter")

  override def preStart(): Unit = {
    println("MapActor Start Path is: " + self.path.toString)
  }

  var currJob = 3
  def receive = {
    case Job(func, in_key, in_value) =>
      //println("MapActor received ", in_value)
      currJob match {
        case 1 => map(new MapJob1, in_key, in_value)
        case 2 => map(new MapJob2,in_key, in_value)
        case 3 => map(new MapJob3, in_key, in_value)
      }

    case Flush =>
      reduceRouter ! Broadcast(Flush)

    case msg =>
          println("Misc Message ", msg)


  }

//  def map[S, T, U, V](in_key: S, in_value: T): Unit ={
//
//      val list = f.map(in_key, in_value)
//      println("Mapper List ", list)
////      for (i <- list){
////        //(out_key, intermediate_value)
////        reduceRouter ! ConsistentHashableEnvelope(Reduce(i._1, i._2), i._1)
////      }
//  }
  def map[A, B, C, D](func: Mapper[A, B, C, D], value: A, value1: B): Unit = {
    val list = func.map(value, value1)
    //println(list)
    for (i <- list){
      reduceRouter ! ConsistentHashableEnvelope(Reduce(i._1, i._2), i._1)
    }
  }

  // Get the content at the given URL and return it as a string

}
