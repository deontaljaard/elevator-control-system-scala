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
   * The control system schedules a pickup floor request in the requested direction. The function searches for
   * elevators moving in the desired direction. It calcs the diff between the requested pickup floor and an elevator's
   * current floor, and builds a sorted list. This candidacy check basically determines if the floor can be inserted in
   * the path of an elevator. If there are no candidates, the pickup request get queued. If after the next step an
   * elevator becomes available, the pickup request will get added to that elevator.
   *
   * @param pickupFloor the pickup floor an elevator is requested to stop at.
   * @param direction   the direction in which the person at the floor would like to travel.
   */
  override def pickup(pickupFloor: Int, direction: Direction): Unit = {
    // collect elevator candidates, sorted by smallest diff
    val elevatorsInDirection = elevators.collect {
      case elevator: Elevator if elevator.direction == direction  =>
        val diff = direction.value * (pickupFloor - elevator.floorNumber)
        (diff, elevator.id)
    }.sorted

    elevatorsInDirection.headOption match {
      case Some((_, elevatorId)) => elevators(elevatorId).addPickup(pickupFloor)
      case _ => pickupQ.enqueue((pickupFloor, direction))
    }
  }

  /**
   * Invokes the move() function of each elevator in the system. It also checks to see if there are any pickup requests
   * queued, and if so, will try find an idle elevator on which to add the pickup request.
   */
  override def step(): Unit = {
    elevators.filterNot(idleElevator).foreach(elevator => elevator.move())

    if (pickupQ.nonEmpty) {
      val (pickupFloor, direction) = pickupQ.head
      elevators.filter(idleElevator)
        .filter(elevator => elevator.direction == direction && elevator.goalFloorNumber > pickupFloor)
        .foreach { elevator =>
          val (floor, _) = pickupQ.dequeue()
          elevator.addPickup(floor)
        }
    }
  }

  // not very efficient, but can deal with it if for small amounts of elevators
  def totalPendingRequests: Int = elevators.map(_.pendingRequests()).sum + pickupQ.size

  override def toString: String = s"Controlling $elevatorCount elevators."
}
