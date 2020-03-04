elevator-control-system-scala
=

# Run
Assuming a Scala/SBT environment is set up.

## Tests
There are a couple of base [tests](./src/test/scala/system/elevator) to cover decision-making logic.

In the root of the project, run:
```bash
sbt test
```

## Simulator
In the root of the project, run:
```bash
sbt package
sbt run
```
or combined
```bash
sbt ";package;run"
```

# Write-up
Here follows some thoughts on implementation details, like data structures used, design decisions, etc.

## Elevator
See [Elevator.scala](./src/main/scala/system/elevator/Elevator.scala)

The elevator implementation makes use of a TreeSet to keep track of pickup requests. It uses two TreeSets for floors above (up) and
below (down) the current floor. A set was used because it automatically discards duplicates. A TreeSet was used because of natural 
ordering on numbers. If the elevator is going up, it queries the next lowest floor in the up TreeSet. If the elevator is going down
it queries the next highest floor in the down TreeSet.


## Elevator Controller (Elevator Control System - ECS)
See [ElevatorController.scala](./src/main/scala/system/elevator/ElevatorController.scala)

The elevator controller implements the provided ECS interface. Notable functions are `pickup` and `step`. 

The `pickup` function is the scheduler. It schedules a pickup floor request in the requested direction. The function 
searches for elevators moving in the desired direction. It calcs the diff between the requested pickup floor and an 
elevator's current floor, and builds a sorted list. This candidacy check basically determines if the floor can be inserted in
the path of an elevator. If there are no candidates, the pickup request get queued. If after the next step an
elevator becomes available, the pickup request will get added to that elevator.

The `step` function invokes the move() function of each elevator in the system. It also checks to see if there are any 
pickup requests queued, and if so, will try find an idle elevator on which to add the pickup request.

The controller makes use of a standard Queue data structure to queue pick up requests that cannot be immediately served.

## Simulator
See [Main.scala](./src/main/scala/simulator/Main.scala)

A naive simulator that creates an ECS, updates the elevator states, issues pickup requests, and steps through the system
 until there are no more pending pickup requests.

## Improvements
* The elevator logic can become biased towards a certain direction. E.g. update the goal floor followed by and add pickup 
floor request between current floor and goal floor.
* Using a queue to handle pickup requests that are not immediately servable feels fair for a small amount of pickup requests.
As the amount of elevators and floors grow, a more efficient method can potentially reduce unnecessary wait times for people.
* Do more tweaking to reduce travel time of elevators. 
* Check for instances were all the elevators are travelling in a specific direction to ensure 'availability' for the opposite direction.

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