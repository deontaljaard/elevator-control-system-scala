package system

import system.enums.Direction

trait ElevatorControlSystem {
  def status(): Seq[(Int, Int, Int)]
  def update(elevatorId: Int, floorNum: Int, goalFloorNum: Int)
  def pickup(pickupFloor: Int, direction: Direction)
  def step()
}
