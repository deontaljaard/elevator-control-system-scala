
package system.elevator

import system.enums.{Direction, Down, Up}

import scala.collection.mutable


class Elevator(val id: Int) {

  private def idleMessage = s"Elevator ID: $id is idle on floor $floorNumber. No more requests."

  val upFloors: mutable.TreeSet[Int] = mutable.TreeSet.empty[Int]
  val downFloors: mutable.TreeSet[Int] = mutable.TreeSet.empty[Int](Ordering[Int].reverse)
  var floorNumber, goalFloorNumber: Int = 0

  def direction: Direction = if (goalFloorNumber > floorNumber) Up else Down

  def status: (Int, Int, Int) = (id, floorNumber, goalFloorNumber)

  def pendingRequests(): Int = upFloors.size + downFloors.size

  /**
   * Updates the state of the elevator. Clears internal floor tracking sets. Sets new current and goal floors. 
   * Adds the goal floor as a pick up request to initiate the elevators new direction.
   *
   * @param newFloorNumber     sets a new current floor number.
   * @param newGoalFloorNumber sets a new 
   */
  def update(newFloorNumber: Int, newGoalFloorNumber: Int): Unit = {
    upFloors.clear()
    downFloors.clear()
    floorNumber = newFloorNumber
    goalFloorNumber = newGoalFloorNumber
    addPickup(goalFloorNumber)
  }

  /**
   * Adds a new pickup floor to the elevator. Ignores pickup requests on the same floor.
   *
   * @param pickupFloorNumber a floor the elevator should stop at.
   */
  def addPickup(pickupFloorNumber: Int): Unit = {
    if (pickupFloorNumber > floorNumber) upFloors += pickupFloorNumber
    else if (floorNumber > pickupFloorNumber) downFloors += pickupFloorNumber
  }

  /**
   * Delegates the movement of the elevator in its current direction
   */
  def move(): Unit = direction match {
    case Up => handleMoveForDirection(upFloors, downFloors, Up)
    case Down => handleMoveForDirection(downFloors, upFloors, Down)
  }

  /**
   * A helper function that takes the two sets of pickup floor requests for up and down directions. It uses the
   * current direction to move the elevator by 1 floor. If the floor number matches the a pickup request for that floor,
   * the pickup floor gets removed (the elevator has stopped at the floor and the assumption is that the people got out).
   * If the floor number matches the goal number, a new goal floor is set from the current direction's closest next pickup floor.
   * If there are no pickup requests remaining for the current direction, the opposite direction is used to select a
   * new goal floor, which subsequently changes the direction of the elevator.
   * If there are no pending pickup floor requests, the elevator remains idle at its last goal floor.
   *
   * @param currentDirectionRequests  a set of pickup floors in the elevator's current direction
   * @param oppositeDirectionRequests a set of pickup floors in the elevator's opposite direction
   * @param currentDirection          the current direction of the elevator
   */
  private def handleMoveForDirection(currentDirectionRequests: mutable.TreeSet[Int],
                                     oppositeDirectionRequests: mutable.TreeSet[Int],
                                     currentDirection: Direction): Unit = {
    if (currentDirectionRequests.nonEmpty) {
      floorNumber += currentDirection.value
      if (floorNumber == currentDirectionRequests.head)
        currentDirectionRequests -= currentDirectionRequests.head

      if (floorNumber == goalFloorNumber) {
        if (currentDirectionRequests.nonEmpty) goalFloorNumber = currentDirectionRequests.head
        else if (oppositeDirectionRequests.nonEmpty) goalFloorNumber = oppositeDirectionRequests.head
        else println(idleMessage)
      }
    }
    else if (oppositeDirectionRequests.nonEmpty) goalFloorNumber = oppositeDirectionRequests.head
    else println(idleMessage)
  }

  override def toString: String = s"Elevator ID: $id, floor number: $floorNumber, goal floor: $goalFloorNumber"
}
