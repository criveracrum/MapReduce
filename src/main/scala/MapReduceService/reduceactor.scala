package MapReduceService

import scala.collection.mutable.HashMap
import akka.actor.Actor
import akka.actor.ProviderSelection.cluster
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberUp, UnreachableMember}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.collection.mutable.HashMap

class ReduceActor extends Actor {
  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self,
      classOf[MemberEvent], classOf[UnreachableMember])
    println("ReduceActor Start Path is: " + self.path.toString)
  }
  override def postStop() : Unit = {
    cluster.unsubscribe(self)
  }

  val job1 = ReduceJob1()
  val job2 = ReduceJob2()
  val job3 = ReduceJob3()
  var currJob = 1
  val supervisor = context.actorSelection("akka://ClusterSystem@127.0.0.1:2552/user/supervisor")
  supervisor ! "WatchMe"

  var mappers = 0
  var FlushTotal = 0
  var reduceMap = HashMap[Any,Any]()
  def receive = {

    case Reduce(out_key, inter_val) =>
      reduce(out_key, inter_val)
    case JobNum(num) =>
      println("Reducer got job type ", num)
      currJob = num
    case MemberUp(member) if member.hasRole("mapper")=>
      mappers += 1
    case Flush =>
      FlushTotal += 1
      if (mappers == FlushTotal) {
        currJob match {
          case 1 =>
            println(self.path.toStringWithoutAddress + " : " + job1.reduceMap)
            job1.reduceMap.clear()
            FlushTotal = 0
          case 2 =>
            println(self.path.toStringWithoutAddress + " : " + job2.reduceMap)
            job2.reduceMap.clear()
            FlushTotal = 0
          case 3 =>
            println(self.path.toStringWithoutAddress + " : " + job3.reduceMap)
            job3.reduceMap.clear()
            FlushTotal = 0
        }
      }

  }

  def reduce[C, D](out_key: C, inter_val: D): Unit = {
    currJob match {
      case 1 => {
        job1.reduce(out_key.asInstanceOf[String], inter_val.asInstanceOf[Int])
      }
      case 2 => {
        job2.reduce(out_key.asInstanceOf[String], inter_val.asInstanceOf[String])
      }
      case 3 => {
        job3.reduce(out_key.asInstanceOf[String], inter_val.asInstanceOf[String])
      }
    }

  }

}
