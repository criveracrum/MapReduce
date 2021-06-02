package MapReduceService

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, UnreachableMember}
import akka.routing.{Broadcast, FromConfig}

import scala.collection.mutable.HashSet
import scala.io.Source

class MapActor extends Actor {

  val reduceRouter = context.actorOf(FromConfig.props(Props[ReduceActor]()),
    name = "reduceRouter")

  override def preStart(): Unit = {
    println("MapActor Start Path is: " + self.path.toString)
  }

  val STOP_WORDS_LIST = List("a", "am", "an", "and", "are", "as", "at", "be",
    "do", "go", "if", "in", "is", "it", "of", "on", "the", "to")

  def receive = {
    case Job(in_key, in_value) =>
      println("MapActor received ", in_value)
      reduceRouter ! Reduce(in_key, in_value)
    case msg =>
          println("Misc Message ", msg)

//    case Flush =>
//      reduceActors ! Broadcast(Flush)
  }

//  def process

  // Get the content at the given URL and return it as a string
  def getContent( url: String ) = {
    try {
      Source.fromURL(url).mkString
    } catch {     // If failure, just return an empty string
      case e: Exception => ""
    }
  }
}
