package MapReduceService

import akka.actor.{Actor, ActorSystem, Address, Props, RootActorPath}
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent.{CurrentClusterState, InitialStateAsEvents, MemberEvent, MemberRemoved, MemberUp, UnreachableMember}
import akka.cluster.MemberStatus
import akka.routing.{Broadcast, FromConfig}
import com.typesafe.config.ConfigFactory

object MapReduceClient {

  def main(args: Array[String]): Unit = {
    // note that client is not a compute node, role not defined
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"""
        akka.remote.artery.canonical.port=$port
        """)
      .withFallback(ConfigFactory.load("master"))

    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props(classOf[MapReduceClient]), "client")
  }

//  master ! Book("A Tale of Two Cities", "http://reed.cs.depaul.edu/lperkovic/gutenberg/pg98.txt")
//  master ! Book("The Pickwick Papers", "http://reed.cs.depaul.edu/lperkovic/gutenberg/pg580.txt")
//  master ! Book("A Child's History of England", "http://reed.cs.depaul.edu/lperkovic/gutenberg/pg699.txt")
//  master ! Book("The Old Curiosity Shop", "http://reed.cs.depaul.edu/lperkovic/gutenberg/pg700.txt")
//  master ! Book("Oliver Twist", "http://reed.cs.depaul.edu/lperkovic/gutenberg/pg730.txt")
//  master ! Book("David Copperfield", "http://reed.cs.depaul.edu/lperkovic/gutenberg/pg766.txt")
//  master ! Book("Hunted Down", "http://reed.cs.depaul.edu/lperkovic/gutenberg/pg807.txt")
//  master ! Book("Dombey and Son", "http://reed.cs.depaul.edu/lperkovic/gutenberg/pg821.txt")
//  master ! Book("Our Mutual Friend", "http://reed.cs.depaul.edu/lperkovic/gutenberg/pg883.txt")
//  master ! Book("Little Dorrit", "http://reed.cs.depaul.edu/lperkovic/gutenberg/pg963.txt")
//  master ! Book("Life And Adventures Of Martin Chuzzlewit", "http://reed.cs.depaul.edu/lperkovic/gutenberg/pg967.txt")
//  master ! Book("The Life And Adventures Of Nicholas Nickleby", "http://reed.cs.depaul.edu/lperkovic/gutenberg/pg968.txt")
//  master ! Book("Bleak House", "http://reed.cs.depaul.edu/lperkovic/gutenberg/pg1023.txt")
//  master ! Book("Great Expectations", "http://reed.cs.depaul.edu/lperkovic/gutenberg/pg1400.txt")
//  master ! Book("A Christmas Carol", "http://reed.cs.depaul.edu/lperkovic/gutenberg/pg19337.txt")
//  master ! Book("The Cricket on the Hearth", "http://reed.cs.depaul.edu/lperkovic/gutenberg/pg20795.txt")
//
//  master ! Flush
}

class MapReduceClient extends Actor {
//  val config = ConfigFactory.parseString("akka.cluster.roles = [client]").
//    withFallback(ConfigFactory.load("master"))
  val cluster = Cluster(context.system)

  val router = context.actorOf(FromConfig.props(Props[MapReduceService]()),
    name = "workerRouter")

  override def preStart(): Unit = {
    println("Client Start Path is: " + self.path.toString)
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }

  var total = 0

  def receive = {
    case MemberUp(member) =>
      total += 1
      println("Client total is ", total)
      println("Got member info: {} Total is now: {}", member.address, total)
      if (total >= 3) {
        router ! Broadcast("Hi")
      }
    case UnreachableMember(member) =>
      //println("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      total -= 1
      println("Client total members: {} ",
        total)

  }
}



