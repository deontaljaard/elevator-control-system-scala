elevator-control-system-scala
=

# Task
Design and implement an elevator control system in Scala.
Your elevator control system should be able to handle a few elevators — say up to 16.
In the end, your system should provide an interface for:
1. querying the state of the elevators (what floor are they on and  where they are going)
2. receiving an update about the status of an elevator
3. receiving a pickup request
4. time-stepping the simulation
For example, we could imagine an interface like this:
```scala
trait ElevatorControlSystem {
    def status(): Seq[(Int, Int, Int)]
    def update(elevatorId: Int, floorNum: Int, goalFloorNum: Int)
    def pickup(pickupFloor: Int, direction: Int)
    def step()
}
```
Here we have chosen to represent elevator state as 3 integers: Elevator ID, Floor Number, Goal Floor Number

A pickup request is two integers: Pickup Floor, Direction (negative for down, positive for up)

This is not a particularly nice interface, and it leaves some open questions. For example, the elevator state only has 
one goal floor; but it is conceivable that an elevator holds more than one person, and each person wants to go to a 
different floor, so there could be a few goal floors queued up. 

# Write-up
