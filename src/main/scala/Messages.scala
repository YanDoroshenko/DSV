case class HandshakeRequest(id: String)
case class HandShakeResponse(id: String)

case class AssignInitiator()
case class BeginElection()
case class ElectionCandidate(candidateId: String)
case class LeaderElected(leaderId: String)