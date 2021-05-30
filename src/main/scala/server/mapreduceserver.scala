package server

import com.typesafe.config.ConfigFactory
import akka.actor.{Actor, ActorSystem, Address, PoisonPill, Props, RelativeActorPath}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberEvent, ReachabilityEvent}
import akka.routing.FromConfig
import common._

object MapReduceServer {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      startup(Seq("2551", "2552", "0"))
      SampleClient.main(Array.empty)
    } else {
      startup(args.toIndexedSeq)
    }
  }

  def startup(ports: Seq[String]): Unit = {
    ports foreach { port =>
      val config = ConfigFactory.parseString(s"""
        akka.remote.artery.canonical.port=$port
        """)
        .withFallback(
          ConfigFactory.parseString("akka.cluster.roles = [MapReduceService]")).
        withFallback(ConfigFactory.load("master"))

      val system = ActorSystem("ClusterSystem", config)

//      system.actorOf(Props[ReduceActor](), name = "reduceActor")
//      system.actorOf(Props[MapActor](), name = "mapActor")
      system.actorOf(Props[MapReduceService](), name = "service")
    }
  }
}
object SampleClient {
  def main(args: Array[String]): Unit = {
    // note that client is not a compute node, role not defined
    val system = ActorSystem("ClusterSystem")
    system.actorOf(Props(classOf[SampleClient], "/user/service"), "client")
  }
}

class SampleClient(servicePath: String) extends Actor {
  val cluster = Cluster(context.system)
  println("Client starting " + self.path.toString())
//  val servicePathElements = servicePath match {
//    case RelativeActorPath(elements) => elements
//    case _ => throw new IllegalArgumentException(
//      "servicePath [%s] is not a valid relative actor path" format servicePath)
//  }
  val router = context.actorOf(FromConfig.props(),
    name = "masterRouter")

  router ! Broadcast("Hi")

  def receive = {
    case msg =>
      println("msg received")
  }

}