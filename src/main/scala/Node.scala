import Node.{parentNode, _}
import akka.actor._
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.io.StdIn

class Node extends Actor {
  private val config = ConfigFactory.load()
  private lazy val root = config.getConfig("app")
  private lazy val id = root.getString("node-id")
  private lazy val parent = root.getString("parent")
  private lazy val parentId = root.getString("parent-id")

  override def preStart(): Unit = if (id != parentId) {
    val parentNode = context.actorSelection("akka.tcp://RemoteSystem@" + parent + ":5150/user/" + parentId)
      parentNode ! HandshakeRequest(id)
  }


  override def postStop(): Unit = {
    parentNode.foreach(_ ! ChildDied(children.toSet))
    children.foreach(_ ! ParentDied(parentNode))
    if (leader.contains(self))
      team.foreach(_ ! LeaderDied)
    else
      leader.foreach(_ ! TeamMemberDied)
  }

  override def receive: Receive = {
    case HandshakeRequest(_id) =>
      children += sender
      println("Child attached " + _id)
      sender ! HandShakeResponse(id)
    case HandShakeResponse(_id) =>
      parentNode = Option(sender)
      println("Parent attached " + _id)
    case ParentDied(newParent) =>
      parentNode = newParent
      println("Parent died, new parent is " + newParent)
    case ChildDied(orphans) =>
      children -= sender
      children ++= orphans
      println("Child died, adopting orphans " + orphans)
    case LeaderDied =>
      println("Leader died")
      leader = None
    case TeamMemberDied =>
      println("Team member died")
      team -= sender
      println("Team is now:\n" + team.mkString("\n"))

    case AssignInitiator =>
      println("Starting election")
      if (parentNode.isEmpty && children.isEmpty)
        self ! LeaderElected(ElectionCandidate(id, self))
      else {
        parentNode.foreach(_ ! BeginElection)
        if (children.nonEmpty)
          children.foreach(_ ! BeginElection)
        else
          parentNode.foreach(_ ! ElectionCandidate(id, self))
      }
    case BeginElection =>
      leader = None
      team.clear()
      println("Invited to join election by " + sender)
      parentNode.filterNot(_ == sender).foreach(_ ! BeginElection)
      if (children.nonEmpty)
        children.filterNot(_ == sender).foreach(_ ! BeginElection)
      else
        parentNode.foreach(_ ! ElectionCandidate(id, self))
    case c: ElectionCandidate =>
      println("Candidate with id" + c.candidateId + " suggested by " + sender)
      candidates += c
      if (candidates.size == children.size) {
        parentNode match {
          case Some(p) =>
            p ! (candidates + c).maxBy(_.candidateId)
          case None =>
            val elected = candidates.maxBy(_.candidateId)
            leader = Some(elected.candidate)
            println("Elected " + elected.candidateId)
            children.foreach(_ ! LeaderElected(elected))
            leader.filterNot(self == _).foreach(_ ! JoinTeam)
        }
        candidates.clear()
      }
          case LeaderElected(elected) =>
            leader = Some(elected.candidate)
            println("Elected " + elected.candidateId)
            children.foreach(_ ! LeaderElected(elected))
            leader.filterNot(self == _).foreach(_ ! JoinTeam)
          case JoinTeam =>
            team += sender
            sender ! "Yo m8, welcome to the team!"
          case CollectData =>
            leader match {
              case Some(l) => l ! DataRequest
              case None => println("No leader, do an election first")
            }
              case DataRequest =>
                println("Recieved data request")
                if (leader.contains(self)) {
                  requestedBy = Some(sender)
                  team.foreach(_ ! DataRequest)
                }
                else
                  sender ! DataChunk(data)
              case chunk: DataChunk if leader.contains(self) =>
                chunks += chunk
                if (chunks.size == team.size) {
                  requestedBy.foreach(_ ! CompleteData(chunks.toSet + DataChunk(data)))
                  chunks.clear()
                }
              case CompleteData(dataChunks) =>
                println("Got complete data: " + dataChunks.mkString(", "))
              case m =>
                println("Something unknown: " + m)
  }

  override def hashCode(): Int = super.hashCode()

  override def equals(o: scala.Any): Boolean = o match {
    case n: Node => id == n.id
    case _ => false
  }
}

object Node {

  private val config = ConfigFactory.load()
  private val root = config.getConfig("app")
  private val id = root.getString("node-id")

  val data: Array[String] = Map(
    "node-1" -> Array("Chunk 1", "Chunk 2"),
    "node-2" -> Array("Chunk 3"),
    "node-3" -> Array("Chunk 4"),
    "node-4" -> Array("Chunk 5", "Chunk 6"),
    "node-5" -> Array("Chunk 7")
  )(id)

  private var parentNode: Option[ActorRef] = None
  private val children = mutable.Set[ActorRef]()
  private var leader: Option[ActorRef] = None
  private val team = mutable.Set[ActorRef]()
  private val chunks = mutable.Set[DataChunk]()
  private var requestedBy: Option[ActorRef] = None

  private val candidates = mutable.Set[ElectionCandidate]()

  def main(args: Array[String]) {

    val system = ActorSystem("RemoteSystem", config)
    root.getString("node-id")

    val localActor = system.actorOf(Props[Node], name = id)

    while (true)
      StdIn.readLine match {
        case "elect" =>
          localActor ! AssignInitiator
        case "die" =>
          system.terminate()
        case "data" =>
          localActor ! CollectData
        case _ =>
      }
  }
}
