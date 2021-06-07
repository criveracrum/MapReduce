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
    var i = 0
//    val f = (in_key: String, in_value: String) => {
//      val content = Source.fromFile(in_value).mkString
//      var jobs: List[(String, Int)] = List()
//      var word : String = ""
//      for (word <- content.split("[\\p{Punct}\\s]+")){
//        if (!jobs.contains(word, in_key)){
//          jobs = (word, 1) :: jobs
//        }
//      }
//      jobs
//    }
//    println(f("1", List("Bear", "Deer") ))


    router ! Job("1", "Title1", "Jobs/job1/Title1.txt")
    router ! Job("1", "Title2", "Jobs/job1/Title2.txt")
    router ! Job("1", "Title3", "Jobs/job1/Title3.txt")

    Thread sleep (200)
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
//      else {
//        println("Service Currently Unavailable")
//      }
    case MemberUp(member) if member.hasRole("mapper")=>
      mapTotal += 1
      println("mapper seen ", mapTotal)
//      if (total >= 9){
//        sendJob()
//      }

    case UnreachableMember(member) =>
      //println("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      total -= 1
      println("Member removed! Client total members:  ",
        total, member.address)

  }
}



