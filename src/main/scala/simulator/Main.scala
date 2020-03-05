package simulator

import system.elevator.{ElevatorController, ElevatorStatus}
import system.enums.{Down, Up}

object Main {
  def main(args: Array[String]): Unit = {
    val elevatorCount = 16
    val maxFloors = 20
    val r = scala.util.Random

    // construct the controller
    val ecs = new ElevatorController(elevatorCount)

    // update the states (randomly)
    Range(0, elevatorCount).foreach(idx => ecs.update(ElevatorStatus(idx, r.nextInt(maxFloors) + 1, r.nextInt(maxFloors) + 1)))
    val startState = ecs.status()

    // issue pickup requests
    Range(0, elevatorCount).foreach(idx => ecs.pickup(r.nextInt(maxFloors) + 1, if (idx % 2 == 0) Up else Down))
    val totalPickupRequests = ecs.totalPendingRequests

    // step through till there are no more pickup requests to serve
    var steps = 0
    while (ecs.totalPendingRequests > 0) {
      ecs.step()
      steps += 1
    }
    println(s"It took $steps steps to serve $totalPickupRequests pickup requests and go from \n$startState \nto \n${ecs.status()}")
  }
}
