package MapReduceService

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, UnreachableMember}
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope
import akka.routing.{Broadcast, FromConfig}

import scala.collection.mutable.HashSet
import scala.io.Source

class MapActor extends Actor {



  val reduceRouter = context.actorOf(FromConfig.props(Props[ReduceActor]()),
    name = "reduceRouter")

  override def preStart(): Unit = {
    println("MapActor Start Path is: " + self.path.toString)
  }


  def receive = {
    case Job(func, in_key, in_value) =>
      //println("MapActor received ", in_value)
      map(func, in_key, in_value)
    case Flush =>
      reduceRouter ! Broadcast(Flush)

    case msg =>
          println("Misc Message ", msg)


  }

  def map[S,T,X,Y](f: (S,T) => List[(X,Y)], in_key: S, in_value: T): Unit ={
      val list = f(in_key, in_value)
      println("Mapper List ", list)
      for (i <- list){
        //(out_key, intermediate_value)
        reduceRouter ! ConsistentHashableEnvelope(Reduce(i._1, i._2), i._1)
      }
  }

  // Get the content at the given URL and return it as a string

}
