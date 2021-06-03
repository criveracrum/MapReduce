package MapReduceService

trait Message
case class Job[S, T, X, Y](func: (S,T) => List[(X,Y)], in_key: S, in_value: T) extends Message
case class Reduce[X, Y](out_key: X, out_value: Y) extends Message
case object Flush extends Message
case object Done extends Message



