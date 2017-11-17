import akka.actor._
import com.typesafe.config.ConfigFactory

class Node extends Actor {
  private val config = ConfigFactory.load()
  private lazy val root = config.getConfig("app")
  private lazy val id = root.getString("node-id")
  private lazy val parent = root.getString("parent")
  private lazy val parentId = root.getString("parent-id")

  override def preStart(): Unit = if (id != parentId) {
    val parentNode = context.actorSelection("akka.tcp://RemoteSystem@" + parent + ":5150/user/" + parentId)
    println("Parent found: " + parentNode)

    parentNode ! HandshakeRequest(id)
  }

  override def receive: Receive = {
    case HandshakeRequest(_id) =>
      println("Got handshake request from " + _id)
      sender() ! HandShakeResponse(id)
    case HandShakeResponse(_id) =>
      println("Got handshake response from " + _id)
  }
}

object Node {
  def main(args: Array[String]) {
    val config = ConfigFactory.load()
    val root = config.getConfig("app")
    val id = root.getString("node-id")

    val system = ActorSystem("RemoteSystem", config)
    root.getString("node-id")

    system.actorOf(Props[Node], name = id)
  }
}