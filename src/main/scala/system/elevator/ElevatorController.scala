package system.elevator

import system.enums.Direction
import system.{ElevatorControlSystem, MAX_ELEVATORS}

import scala.collection.mutable

class ElevatorController(val elevatorCount: Int) extends ElevatorControlSystem {

  lazy val pickupQ: mutable.Queue[(Int, Direction)] = mutable.Queue.empty[(Int, Direction)]

  val elevators: List[Elevator] = {
    if (elevatorCount <= MAX_ELEVATORS) Range(0, elevatorCount).map(new Elevator(_)).toList
    else throw new Exception(s"Maximum elevator count is $MAX_ELEVATORS")
  }

  val idleElevator: Elevator => Boolean = _.pendingRequests() == 0

  /**
   * Return a sequence of Tuple3[Int, Int, Int] that represent the state of each elevator in the system.
   * Each Tuple3[Int, Int, Int] represents the elevator id, (current) floor number, goal floor number
   *
   * @return a sequence of Tuple3[Int, Int, Int]
   */
  override def status(): Seq[(Int, Int, Int)] = elevators.map(_.status)

  /**
   * Update elevator with new values (new state)
   *
   * @param elevatorId   the id of the elevator
   * @param floorNum     new current floor
   * @param goalFloorNum new goal floor
   */
  override def update(elevatorId: Int, floorNum: Int, goalFloorNum: Int): Unit =
    elevators(elevatorId).update(floorNum, goalFloorNum)

  /**
   * The control system schedules a pickup floor request in the requested direction.
   *
   * @param pickupFloor the pickup floor an elevator is requested to stop at.
   * @param direction   the direction in which the person at the floor would like to travel.
   */
  override def pickup(pickupFloor: Int, direction: Direction): Unit = {
    // collect elevator candidates, sorted by smallest diff
    val elevatorsInDirection = elevators.collect {
      case elevator: Elevator if elevator.direction == direction =>
        val diff = direction.value * (pickupFloor - elevator.floorNumber)
        (diff, elevator.id)
    }.sorted

    elevatorsInDirection.headOption match {
      case Some((_, elevatorId)) => elevators(elevatorId).addPickup(pickupFloor)
      case _ => pickupQ.enqueue((pickupFloor, direction))
    }
  }

  override def step(): Unit = {
    elevators.foreach(_.move())

    if (pickupQ.nonEmpty) {
      elevators.find(idleElevator).foreach { elevator =>
        val (floor, _) = pickupQ.dequeue()
        elevator.addPickup(floor)
      }
    }
  }

  // not very efficient, but can deal with it if for small amounts of elevators
  def totalPendingRequests: Int = elevators.map(_.pendingRequests()).sum + pickupQ.size

  override def toString: String = s"Controlling $elevatorCount elevators."
}
