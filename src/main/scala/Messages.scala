import akka.actor.ActorRef

case class HandshakeRequest(id: String)

case class HandShakeResponse(id: String)

case class ChildDied(orphans: Set[ActorRef])

case class ParentDied(newParent: Option[ActorRef])

case class AssignInitiator()

case class BeginElection()

case class ElectionCandidate(candidateId: String, candidate: ActorRef)

case class LeaderElected(elected: ElectionCandidate)

case class JoinTeam()

case class CollectData()

case class DataRequest()

case class DataChunk(chunk: Array[String]) {
  override def toString: String = chunk.mkString(", ")
}

case class CompleteData(chunks: Set[DataChunk])

case class LeaderDied()

case class TeamMemberDied()
