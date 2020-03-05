package system

import system.elevator.ElevatorStatus
import system.enums.Direction

trait ElevatorControlSystem {
  def status(): Seq[ElevatorStatus]

  def update(elevatorStatus: ElevatorStatus)

  def pickup(pickupFloor: Int, direction: Direction)

  def step()
}
