package MapReduceService

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, UnreachableMember}
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope
import akka.routing.{Broadcast, FromConfig}

import scala.collection.mutable.{HashMap, HashSet}
import scala.io.Source

class MapActor extends Actor {



  val reduceRouter = context.actorOf(FromConfig.props(Props[ReduceActor]()),
    name = "reduceRouter")
  val supervisor = context.actorSelection("akka://ClusterSystem@127.0.0.1:2552/user/supervisor")
  supervisor ! "WatchMe"
  override def preStart(): Unit = {
    println("MapActor Start Path is: " + self.path.toString)
  }
  override def postStop() : Unit = {
    val cluster = Cluster(context.system)
    cluster.unsubscribe(self)
  }

  var currJob = 1
  def receive = {
    case Job(in_key, in_value) =>
      //println("MapActor received ", in_value)
      currJob match {
        case 1 => map(new MapJob1, in_key, in_value)
        case 2 => map(new MapJob2,in_key, in_value)
        case 3 => map(new MapJob3, in_key, in_value)
      }
    case JobNum(num) =>
      println("Mapper got job type ", num)
      currJob = num
      reduceRouter ! Broadcast(JobNum(num))
    case Flush =>
      reduceRouter ! Broadcast(Flush)




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
