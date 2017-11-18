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

  override def receive: Receive = {
    case HandshakeRequest(_id) =>
      children += sender()
      println("Child attached " + _id)
      sender() ! HandShakeResponse(id)
    case HandShakeResponse(_id) =>
      parentNode = sender()
      println("Parent attached " + _id)
    case AssignInitiator =>
      Option(parentNode).foreach(_ ! BeginElection)
      if (children.nonEmpty)
        children.foreach(_ ! BeginElection)
      else
        Option(parentNode).foreach(_ ! ElectionCandidate(id))
    case BeginElection =>
      println("Invited")
      Option(parentNode).filterNot(_ == sender).foreach(_ ! BeginElection)
      if (children.nonEmpty)
        children.filterNot(_ == sender).foreach(_ ! BeginElection)
      else
        Option(parentNode).foreach(_ ! ElectionCandidate(id))
    case ElectionCandidate(candidateId) =>
      messages += candidateId
      if (messages.size == children.size)
        Option(parentNode) match {
          case Some(p) => p ! ElectionCandidate((messages + candidateId).max)
          case None =>
            println("Elected " + (messages + candidateId).max)
            children.foreach(_ ! LeaderElected((messages + candidateId).max))
        }
    case LeaderElected(leaderId) =>
      println("Elected " + leaderId)
      children.foreach(_ ! LeaderElected(leaderId))
  }

  override def hashCode(): Int = super.hashCode()

  override def equals(o: scala.Any): Boolean = o match {
    case n: Node => id == n.id
    case _ => false
  }
}

object Node {

  private var parentNode: ActorRef = _
  private val children = mutable.Set[ActorRef]()

  private val messages = mutable.Set[String]()

  def main(args: Array[String]) {
    val config = ConfigFactory.load()
    val root = config.getConfig("app")
    val id = root.getString("node-id")

    val system = ActorSystem("RemoteSystem", config)
    root.getString("node-id")

    val localActor = system.actorOf(Props[Node], name = id)

    while (true)
      StdIn.readLine match {
        case "elect" =>
          localActor ! AssignInitiator
        case "shutdown" =>
          system.terminate()
        case _ =>
      }
  }
}