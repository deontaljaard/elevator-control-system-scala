package system.enums

sealed abstract class Direction(val value: Int)

case object Up extends Direction(1)

case object Down extends Direction(-1)


