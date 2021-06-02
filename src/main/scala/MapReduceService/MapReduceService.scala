package MapReduceService

import akka.actor.{Actor, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberRemoved, MemberUp, UnreachableMember}
import akka.routing.Broadcast
import com.typesafe.config.ConfigFactory

class MapReduceService extends Actor {
  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }

  var total = 0

  def receive = {
    case MemberUp(member) =>
      total += 1
      //println("Got member info: {} Total is now: {}", member.address, total)
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




