package system.elevator

import org.scalatest.funsuite.AnyFunSuite
import system.enums.{Down, Up}

class ElevatorTest extends AnyFunSuite {

  test("An elevator should receive an update request (to set state)") {
    val elevator = new Elevator(0)
    elevator.update(5, 8)
    assert(elevator.direction == Up)
    assert(elevator.floorNumber == 5)
    assert(elevator.goalFloorNumber == 8)
    assert(elevator.upFloors.nonEmpty)
    assert(elevator.downFloors.isEmpty)
  }

  test("An elevator should derive its direction") {
    val elevator = new Elevator(0)
    elevator.update(0, 2)
    assert(elevator.direction == Up)

    elevator.update(8, 5)
    elevator.addPickup(12)
    assert(elevator.direction == Down)
    assert(elevator.upFloors.nonEmpty)
    assert(elevator.downFloors.nonEmpty)
  }

  test("An elevator should move in the right direction based on current floor number and goal floor") {
    val elevator = new Elevator(0)
    elevator.update(5, 3)
    assert(elevator.direction == Down)
    elevator.move()
    assert(elevator.floorNumber == 4)
  }

  test("An elevator should move towards goal floor, and set new goal floor after reaching goal floor") {
    val elevator = new Elevator(0)
    elevator.update(5, 7)
    assert(elevator.direction == Up)
    elevator.addPickup(3)
    elevator.addPickup(8)
    assert(elevator.upFloors.size == 2)
    assert(elevator.downFloors.size == 1)

    elevator.move()
    assert(elevator.direction == Up)
    assert(elevator.floorNumber == 6)
    elevator.move() // reached first goal floor, set goal floor to 8, which is in the same direction
    assert(elevator.upFloors.size == 1)
    assert(elevator.direction == Up)
    assert(elevator.floorNumber == 7)
    assert(elevator.goalFloorNumber == 8)
    elevator.move() // reached second goal floor
    assert(elevator.direction == Down) // new direction, because there are no more requests upward
    assert(elevator.upFloors.isEmpty)
    assert(elevator.floorNumber == 8)
    assert(elevator.goalFloorNumber == 3)
    elevator.move() // floor 7
    elevator.move() // floor 6
    elevator.addPickup(5)
    assert(elevator.downFloors.size == 2)
    elevator.move() // floor 5, lucky person
    assert(elevator.downFloors.size == 1)
    elevator.move() // floor 4
    elevator.move() // floor 3
    assert(elevator.upFloors.isEmpty)
    assert(elevator.downFloors.isEmpty)
  }
}
