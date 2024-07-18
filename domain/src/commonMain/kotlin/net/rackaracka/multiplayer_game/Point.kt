package net.rackaracka.multiplayer_game

data class Point(val x: Int, val y: Int)

operator fun Point.minus(point: Point) = Point(x - point.x, y - point.y)
