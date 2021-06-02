package MapReduceService

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory


object MapReduceApp {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      startup(Seq("2551", "2552", "0"))
      MapReduceClient.main(Array.empty)
    } else {
      startup(args.toIndexedSeq)
    }
  }

  def startup(ports: Seq[String]): Unit = {
    ports foreach { port =>
      val config = ConfigFactory.parseString(s"""
        akka.remote.artery.canonical.port=$port
        """).withFallback(
      ConfigFactory.parseString("akka.cluster.roles = [service]")).
        withFallback(ConfigFactory.load("workers"))
      val system = ActorSystem("ClusterSystem", config)
      system.actorOf(Props[MapReduceService](), name = "service")
    }
    for (i <- 0 to 2){
      val config = ConfigFactory.parseString(s"""
        akka.remote.artery.canonical.port=0
        """).withFallback(
        ConfigFactory.parseString("akka.cluster.roles = [reducer]")).
        withFallback(ConfigFactory.load())
      val system = ActorSystem("ClusterSystem", config)
      system.actorOf(Props[ReduceActor](), name = "reducer")
    }
    for (i <- 0 to 2){
      val config = ConfigFactory.parseString(s"""
        akka.remote.artery.canonical.port=0
        """).withFallback(
        ConfigFactory.parseString("akka.cluster.roles = [mapper]")).
        withFallback(ConfigFactory.load("mapper"))
      val system = ActorSystem("ClusterSystem", config)
      system.actorOf(Props[MapActor](), name = "mapper")
    }


  }
}
