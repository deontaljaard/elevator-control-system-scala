package system.elevator

import org.scalatest.funsuite.AnyFunSuite
import system.MAX_ELEVATORS
import system.enums.{Down, Up}

class ElevatorControllerTest extends AnyFunSuite {

  test("An ECS should instantiate the num of elevators given to its constructor") {
    val ecs = new ElevatorController(8)
    assert(ecs.elevators.size == 8)
  }

  test(s"An ECS should not instantiate more than $MAX_ELEVATORS") {
    val thrown = intercept[Exception] {
      new ElevatorController(17)
    }
    assert(thrown.getMessage === s"Maximum elevator count is $MAX_ELEVATORS")
  }

  test(s"An ECS should be able to receive update requests") {
    val ecs = new ElevatorController(1)
    ecs.update(ElevatorStatus(0, 3, 8))
    ecs.elevators.head.status == ElevatorStatus(0, 3, 8)
    assert(ecs.elevators.head.status == ElevatorStatus(0, 3, 8))
  }

  test(s"An ECS should be able to query the status of the elevators") {
    val ecs = new ElevatorController(3)
    ecs.update(ElevatorStatus(0, 3, 8))
    ecs.update(ElevatorStatus(1, 6, 2))
    ecs.update(ElevatorStatus(2, 1, 19))
    val expectedStatus = List(ElevatorStatus(0, 3, 8), ElevatorStatus(1, 6, 2), ElevatorStatus(2, 1, 19))
    assert(ecs.status() === expectedStatus)
  }

  test(s"An ECS should be able to receive pickup requests") {
    val ecs = new ElevatorController(2)
    ecs.update(ElevatorStatus(0, 3, 8))
    ecs.update(ElevatorStatus(1, 6, 2))
    assert(ecs.totalPendingRequests == 2)
    ecs.pickup(3, Down)
    ecs.pickup(4, Up)
    assert(ecs.totalPendingRequests == 4)
  }

  test(s"An ECS should be able to step") {
    val ecs = new ElevatorController(2)
    ecs.update(ElevatorStatus(0, 3, 8))
    ecs.update(ElevatorStatus(1, 6, 2))
    ecs.pickup(3, Down)
    ecs.pickup(4, Up)
    assert(ecs.totalPendingRequests == 4)
    ecs.step()
    assert(ecs.totalPendingRequests == 3) // elevator id 0 reached floor 4
    ecs.step()
    ecs.step()
    assert(ecs.totalPendingRequests == 2) // elevator id 1 reached floor 3
    ecs.step()
    assert(ecs.totalPendingRequests == 1) // elevator id 1 reached floor 2
    ecs.step()
    assert(ecs.totalPendingRequests == 0) // elevator id 0 reached floor 8
  }

  test(s"An ECS should be able to handle pickup requests that cannot be served straight away") {
    val ecs = new ElevatorController(2)
    ecs.update(ElevatorStatus(0, 4, 7))
    ecs.update(ElevatorStatus(1, 1, 3))
    ecs.pickup(2, Up)
    ecs.pickup(5, Up)
    ecs.pickup(1, Down)
    assert(ecs.totalPendingRequests == 5)
    assert(ecs.pickupQ.size == 1)
  }
}
