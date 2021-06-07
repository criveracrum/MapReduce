package MapReduceService


import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory


object MapReduceApp {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      startUpServisor()
      startupMaster()
      startupWorkers()
      MapReduceClient.main(Array.empty)
    } else {
      startupWorkers()
    }
  }

  def startupMaster(): Unit = {

    val config = ConfigFactory.parseString(
      s"""
        akka.remote.artery.canonical.port=2551
        """).withFallback(
      ConfigFactory.parseString("akka.cluster.roles = [service]")).
      withFallback(ConfigFactory.load("workers"))
    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props[MapReduceService](), name = "service")
  }
  def startUpServisor(): Unit = {
    val config = ConfigFactory.parseString(
      s"""
        akka.remote.artery.canonical.port=2552
        """).withFallback(
      ConfigFactory.parseString("akka.cluster.roles = [supervisor]")).
      withFallback(ConfigFactory.load("workers"))
    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props[ClusterSupervisor](), name = "supervisor")
  }
    def startupWorkers() : Unit = {
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
