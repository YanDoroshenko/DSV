import akka.actor.ActorRef

case class HandshakeRequest(id: String)

case class HandShakeResponse(id: String)

case class ChildDied(orphans: Set[ActorRef])

case class ParentDied(newParent: Option[ActorRef])

case class AssignInitiator()

case class BeginElection()

case class ElectionCandidate(candidateId: String, candidate: ActorRef)

case class LeaderElected(elected: ElectionCandidate)