package MapReduceService

import scala.collection.mutable

trait Message
//case class Job[S, T, X, Y](func: (S,T) => List[(X,Y)], in_key: S, in_value: T) extends Message
case class Job(in_key: String, in_value: String) extends Message
case class Reduce[+C, +D](out_key: C, out_value: D) extends Message
case class JobNum(num: Int) extends Message
case class RequestForService() extends Message
case object Flush extends Message
case object Done extends Message



