package MapReduceService

trait Message
case class Job[S, T](in_key: S, in_value: T) extends Message
case class Reduce[X, Y](out_key: X, inter_val: Y) extends Message
case object Flush extends Message
case object Done extends Message
