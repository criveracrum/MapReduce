package MapReduceService

import akka.actor.{Actor, ActorSystem, Address, Props, RootActorPath}
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent.{CurrentClusterState, InitialStateAsEvents, MemberEvent, MemberRemoved, MemberUp, UnreachableMember}
import akka.cluster.MemberStatus
import akka.routing.{Broadcast, FromConfig}
import com.typesafe.config.ConfigFactory

import scala.io.Source

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
}

class MapReduceClient extends Actor {

  val cluster = Cluster(context.system)

  val router = context.actorOf(FromConfig.props(Props[MapReduceService]()),
    name = "workerRouter")
  var supervisor = context.actorSelection("akka://ClusterSystem@127.0.0.1:2552/user/supervisor")

  var serviceAccept = false

  override def preStart(): Unit = {
    println("Client Start Path is: " + self.path.toString)
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }
  override def postStop() : Unit = {
    cluster.unsubscribe(self)
  }

  def sendJob(): Unit = {

    if (serviceAccept) {
      router ! JobNum(1)
      Thread sleep (800)
      router ! Job("Title1", "Jobs/job1/Title1.txt")
      router ! Job("Title2", "Jobs/job1/Title2.txt")
      router ! Job("Title3", "Jobs/job1/Title3.txt")
      router ! Job("Title4", "Jobs/job1/Title4.txt")
      Thread sleep (400)
      router ! Flush
    }
    Thread sleep (7000)
    if (serviceAccept) {
      router ! JobNum(2)
      Thread sleep (800)
      router ! Job("Bleak House", "Jobs/job2/Bleak House.txt")
      router ! Job("The Cricket on the Hearth", "Jobs/job2/The Cricket on the Hearth.txt")
      router ! Job("The Old Curiosity Shop", "Jobs/job2/The Old Curiosity Shop.txt")
      router ! Job("Hunted Down", "Jobs/job2/Hunted Down.txt")
      Thread sleep (400)
      router ! Flush
    }
    Thread sleep (7000)
    if (serviceAccept) {
      router ! JobNum(3)
      Thread sleep (800)
      router ! Job("test.html", "Jobs/job3/test.txt")
      router ! Job("test1.html", "Jobs/job3/test1.txt")
      router ! Job("test2.html", "Jobs/job3/test2.txt")
      router ! Job("test4.html", "Jobs/job3/test4.txt")

      Thread sleep (400)
      router ! Flush
    }
  }



  def receive = {
    case MemberUp(member) if member.hasRole("supervisor") => {
      println("client sees supervisor")
      /*
      amends correct path to supervisor
       */
      supervisor = context.actorSelection(RootActorPath(member.address) / "user" / "supervisor")
      supervisor ! "Hi"
      supervisor ! RequestForService()
    }
//    case MemberUp(member) if member.hasRole("service") =>
//      total += 1
//      //println("Client total is ", total)
//
//    case MemberUp(member) if member.hasRole("mapper")=>
//      mapTotal += 1
//      //println("mapper seen ", mapTotal)
//
//    case UnreachableMember(member) =>
//      //println("Member detected as unreachable: {}", member)
//    case MemberRemoved(member, previousStatus) =>
//      total -= 1
//      println("Member removed! Client total members:  ",
//        total, member.address)
    case msg if (msg == "Start") =>
      println(msg)
      serviceAccept = true
      sendJob()
    case msg if (msg == "Wait") =>
      println(msg)
      serviceAccept = false
      Thread sleep (800)
      supervisor ! RequestForService
  }
}



