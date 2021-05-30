package server

import akka.actor.{Actor, ActorSystem, Props, ActorRef}
import akka.cluster.Cluster
import akka.routing.Broadcast
import com.typesafe.config.ConfigFactory
import akka.routing.FromConfig
import common._

class MapReduceService extends Actor {
  println("starting" + self.path.toString())

//  val mapRouter = context.actorOf(FromConfig.props(Props[MapActor]()),
//      name = "mapRouter")


//  val numberMappers  = ConfigFactory.load.getInt("number-mappers")
//  val numberReducers  = ConfigFactory.load.getInt("number-reducers")
//
//  var pending = numberReducers
//
//  val addresses = Seq(
//    Address("akka", "MapReduceClient"),
//    Address("akka", "MapReduceServer", "127.0.0.1", 25520)
//  )
//
//  def hashMapping: ConsistentHashMapping = {
//    case Word(word, title) => word
//  }
//
//  val reduceActors = context.actorOf(RemoteRouterConfig(ConsistentHashingPool(numberReducers, hashMapping = hashMapping), addresses).props(Props[ReduceActor]()))
//
//  val mapActors = context.actorOf(RemoteRouterConfig(RoundRobinPool(numberMappers), addresses).props(Props(classOf[MapActor], reduceActors)))

  def receive = {
//    case msg: Book =>
//      mapActors ! msg
//    case Flush =>
//      mapActors ! Broadcast(Flush)
//    case Done =>
//      println("Received Done from" + sender())
//      pending -= 1
//      if (pending == 0)
//        context.system.terminate()
    case msg =>
      println("got" + msg)

  }
}



//val config = ConfigFactory.parseString(s"""
////        akka.remote.artery.canonical.port=$port
////        """).withFallback(ConfigFactory.load.getConfig("server"))
//  val system = ActorSystem("MapReduceSystem", config)
//
//  system.actorOf(Props[MasterActor](), name = "masterActor")