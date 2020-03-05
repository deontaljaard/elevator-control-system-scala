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

The elevator implementation makes use of a TreeSet to keep track of pickup requests. 

It uses two TreeSets for floors above (up) and
below (down) the current floor. A set was used because it automatically discards duplicates. A TreeSet was used because of natural 
ordering on numbers. If the elevator is going up, it queries the next lowest floor in the up TreeSet. If the elevator is going down
it queries the next highest floor in the down TreeSet.

The elevator will set a new goal floor for itself after it reaches the defined goal floor. However, the implementation 
is direction biased, meaning the elevator will explore all floor in a certain direction before changing direction 
(assuming the goal floor does not get explicitly updated, which may alter the current direction). This internal tracking
addresses a concern in the brief that the elevator's state only holds on goal floor. 

Notable functions are `handleMoveForDirection`

The `handleMoveForDirection` function is the decision-making 'engine' of the elevator. The implementation serves all pick up 
requests in the direction's path. If there are no more pick up requests in the current direction, the elevator will look 
to see if there are pickup requests in the other direction, and if so, will change its direction and set a new goal floor. 
If there are no pickup requests in either direction, the elevator will remain idle at its last goal floor. 

## Elevator Controller (Elevator Control System - ECS)
See [ElevatorController.scala](./src/main/scala/system/elevator/ElevatorController.scala)

The elevator controller implements the provided ECS interface. The elevator controller makes use of a standard Queue 
data structure to queue pick up requests that cannot be immediately served. A queue was used for simplicity and the implicit
fair nature it provides for pickup requests.

Notable functions are `pickup` and `step`. 

The `pickup` function is the scheduler. It schedules a pickup floor request in the requested direction. The function 
searches for elevators moving in the desired direction. It calcs the diff between the requested pickup floor and an 
elevator's current floor, and builds a sorted list to insert the pickup request in the path of a moving elevator to 
reduce waiting time. This candidacy check basically determines if the floor can be inserted in the path of an elevator. 
If there are no candidates, the pickup request get queued. If after the next step an elevator becomes available, the 
pickup request will get added to a candidate elevator.

The `step` function invokes the move() function of each elevator in the system. It also checks to see if there are any 
pickup requests queued, and if so, will try find an idle elevator on which to add the pickup request.


## Simulator
See [Main.scala](./src/main/scala/simulator/Main.scala)

A naive simulator that creates an ECS, updates the elevator states, issues pickup requests, and steps through the system
 until there are no more pending pickup requests.

## Room for improvement
* The elevator logic can become biased towards a certain direction. E.g. continually update the goal floor in the 
elevator's current direction and it will continue to travel in that direction until it reaches the goal floor. 
This may become problematic if there's demand on lower floors and can potentially lead to 'starvation' of those requests.
* Using a queue to handle pickup requests that are not immediately servable feels fair for a small amount of pickup requests.
As the amount of elevators and floors grow, a more efficient method can potentially reduce unnecessary wait times for people.
* Do more tweaking to reduce travel time of elevators.
* Check for instances were all the elevators are travelling in a specific direction to ensure 'availability' for the 
opposite direction.
* Use value classes for numeric types in the elevator status case class.
* Use concurrent collections to make the solution thread-safe and more robust for use in a web service.