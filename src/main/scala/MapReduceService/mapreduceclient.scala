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

  override def preStart(): Unit = {
    println("Client Start Path is: " + self.path.toString)
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }
//  def f(x: String, y: List[String]): List[(String, String)] = {
//    var jobs: List[(String, String)] = List()
//    for (word <- y){
//      if (!jobs.contains(word, x)){
//        jobs = (word, x) :: jobs
//      }
//    }
//    jobs
//  }
  def sendJob(): Unit = {


    router ! JobNum(1)
    Thread sleep (800)
    router ! Job("Title1", "Jobs/job1/Title1.txt")
    router ! Job("Title2", "Jobs/job1/Title2.txt")
    router ! Job("Title3", "Jobs/job1/Title3.txt")
    Thread sleep (400)
    router ! Flush

    Thread sleep (7000)
    router ! JobNum(2)
    Thread sleep (800)
    router ! Job("Bleak House", "Jobs/job2/Bleak House.txt")
    router ! Job("The Cricket on the Hearth", "Jobs/job2/The Cricket on the Hearth.txt")
    router ! Job("The Old Curiosity Shop", "Jobs/job2/The Old Curiosity Shop.txt")
    Thread sleep (400)
    router ! Flush

    Thread sleep (7000)
    router ! JobNum(3)
    Thread sleep (800)
    router ! Job("test.txt", "Jobs/job3/test.txt")
    router ! Job("test1.txt", "Jobs/job3/test1.txt")
    router ! Job("test2.txt", "Jobs/job3/test2.txt")

    Thread sleep (400)
    router ! Flush
  }

  var total = 0
  var mapTotal= 0

  def receive = {
    case MemberUp(member) if member.hasRole("service")=>
      total += 1
      //println("Client total is ", total)
      if (total >= 3) {
        sendJob()
      }

    case MemberUp(member) if member.hasRole("mapper")=>
      mapTotal += 1
      println("mapper seen ", mapTotal)

    case UnreachableMember(member) =>
      //println("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      total -= 1
      println("Member removed! Client total members:  ",
        total, member.address)

  }
}



