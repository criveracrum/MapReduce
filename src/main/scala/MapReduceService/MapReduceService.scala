package MapReduceService

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberRemoved, MemberUp, UnreachableMember}
import akka.routing.{Broadcast, FromConfig}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.language.postfixOps


class MapReduceService extends Actor {
  val cluster = Cluster(context.system)



  val mapRouter = context.actorOf(FromConfig.props(Props[MapActor]()),
    name = "mapRouter")
  val supervisor = context.actorSelection("akka://ClusterSystem@127.0.0.1:2552/user/supervisor")
  supervisor ! "WatchMe"

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }
  def sendJob(): Unit ={

      while (queue.nonEmpty){
        val message = queue.dequeue()
        println("sending", message)
        mapRouter ! message
      }
  }
  var queue = new mutable.Queue[Message]()
  var total = 0
  var mapTotal = 0
  def receive = {
    case Job(in_key, in_value) =>

      queue.enqueue(Job(in_key, in_value))
//      println("Received job with ", in_key, in_value, queue.size)
      if (mapTotal >=1){
        sendJob()
      }
    case JobNum(num) =>
      mapRouter ! Broadcast(JobNum(num))
    case MemberUp(member) if member.hasRole("mapper")=>
      mapTotal += 1
      println("Master sees mapper ", mapTotal)
    case Flush =>
      mapRouter ! Broadcast(Flush)

    case UnreachableMember(member) =>
      //println("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      total -= 1
      //println("total members: {} ",
//        total)

    case msg if (msg == "Hi") =>
      println("Received ", msg, sender().path.toString)

  }
}




