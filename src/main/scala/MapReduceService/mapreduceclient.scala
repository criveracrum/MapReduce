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
    val f = (x: String, y: List[String]) => {
      var jobs: List[(String, String)] = List()
      for (word <- y){
        if (!jobs.contains(word, x)){
          jobs = (word, x) :: jobs
        }
      }
      jobs
    }
//    println(f("1", List("Bear", "Deer") ))
    router ! Job(f, "1", List("Bear", "Deer"))
    router ! Job(f, "2", List("Deer", "Deer"))
    router ! Job(f, "3", List("Insect", "Pheromone"))
    router ! Job(f, "4", List("Insect", "Pheromone"))
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



