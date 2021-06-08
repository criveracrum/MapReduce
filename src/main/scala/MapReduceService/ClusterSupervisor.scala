package MapReduceService
import akka.actor.SupervisorStrategy.Resume
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.actor.{Actor, ActorLogging, ActorRef, Address, OneForOneStrategy}


class ClusterSupervisor extends Actor{
  val cluster = Cluster(context.system)
  override def preStart(): Unit = {
    println("Supervisor Start Path is: " + self.path.toString)
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop() : Unit = {
    cluster.unsubscribe(self)
  }

  override val supervisorStrategy = OneForOneStrategy() {
    case _ => {
      println("supervisor received crash")
      Resume
    }
  }
  var serviceNode = 0
  var mapper = 0
  var reducer = 0
  var serviceAvailable = false

  def receive: Receive =  {
    case MemberUp(member) if member.hasRole("service") =>
      serviceNode += 1
      serviceAvailable = true
      println("Master is online")


    case MemberUp(member) if member.hasRole("mapper") =>
      mapper += 1
      serviceAvailable = true
      println("Mapper is online")
    case MemberUp(member) if member.hasRole("reducer") =>
      reducer += 1
      serviceAvailable = true
      println("Reducer is online")


    case UnreachableMember(member) if member.hasRole("service")=>
      serviceNode -= 1
      serviceAvailable = false
      println("Master is offline. Service is down")
    case UnreachableMember(member) if member.hasRole("mapper") =>
      mapper -= 1
      if (mapper == 0){
        serviceAvailable = false
        MapReduceApp.startupWorkers()
      }
    case UnreachableMember(member) if member.hasRole("mapper") =>
      reducer -= 1
      if (reducer == 0){
        serviceAvailable = false
        MapReduceApp.startupWorkers()
      }

    case MemberRemoved(member, previousStatus) if member.hasRole("service")=>
      MapReduceApp.startupMaster("0")
    case RequestForService() =>
      println("Supervisor got request for Service")
      if (serviceAvailable){
        sender() ! "Start"
      } else {
        sender() ! "Wait"
      }
    case msg if(msg == "Hi") =>
      println("Supervisor got this message")
    case msg if(msg == "WatchMe") =>
      context.watch(sender())
    case _: MemberEvent => // ignore
  }
}
