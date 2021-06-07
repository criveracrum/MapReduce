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

  override def postStop(): Unit = cluster.unsubscribe(self)

  override val supervisorStrategy = OneForOneStrategy() {
    case _ => {
      println("supervisor received crash")
      Resume
    }
  }
  var serviceNode = 0
  var serviceAvailable = false


  def receive: Receive =  {
    case MemberUp(member) if member.hasRole("service") =>
      serviceNode += 1
      serviceAvailable = true

      println("Master is online")


    case MemberUp(member) =>
      println("Member is Up: {}", member.address)

    case UnreachableMember(member) if member.hasRole("service")=>
      serviceNode -= 1
      serviceAvailable = false
      println("Master is offline. Service is down")

    case UnreachableMember(member) =>
      println("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      println("Member is Removed: {} after {}",
        member.address, previousStatus)
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
