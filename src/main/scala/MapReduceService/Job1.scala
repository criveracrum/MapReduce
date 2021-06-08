package MapReduceService



import org.jsoup.Jsoup

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.HashMap
import scala.io.Source



trait Mapper[A, B, C, D]{
  def map(in_key: A, in_value: B): List[(C, D)]
}

trait Reducer[X, Y, Z]{
  val reduceMap : HashMap[X, Z]
  def reduce(out_key : X, inter_val: Y) : Unit
}

class MapJob1 extends Mapper[String, String, String, Int]{
    def map(in_key: String, in_value: String): List[(String, Int)] = {
    val content = Source.fromFile(in_value).mkString
    var jobs: List[(String, Int)] = List()
    var word : String = ""
    for (word <- content.split("[\\p{Punct}\\s]+")){
      if (!jobs.contains(word, in_key)){
        jobs = (word, 1) :: jobs
      }
    }
    jobs
  }
}

case class ReduceJob1() extends Reducer[String, Int, Int] {
   val reduceMap  = HashMap[String, Int]()
   def reduce(out_key: String, inter_val: Int): Unit = {
    if (reduceMap.contains(out_key)) {
//      reduceMap1 += (out_key -> (reduceMap1(out_key) + inter_val))
      reduceMap +=  (out_key -> (inter_val.+(reduceMap(out_key))  ))
    } else {
      reduceMap += (out_key -> inter_val)
    }
  }
}
case class ReduceJob2() extends Reducer[String, String, List[String]] {
  val reduceMap = HashMap[String, List[String]]()

  def reduce(out_key: String, inter_val: String): Unit = {
    if (reduceMap.contains(out_key)) {
      if (!reduceMap(out_key).contains(inter_val)) {
        reduceMap += (out_key -> (inter_val :: reduceMap(out_key)))
      } }else {
        reduceMap += (out_key -> List(inter_val))
        //println("ADDING TO LIST")
      }
    }

}
class MapJob2 extends Mapper[String, String, String, String]{
  val STOP_WORDS_LIST = List("a", "am", "an", "and", "are", "as", "at", "be",
    "do", "go", "if", "in", "is", "it", "of", "on", "the", "to")
  def map(in_key: String, in_value: String): List[(String, String)] = {
    val content = Source.fromFile(in_value).mkString
    var jobs: List[(String, String)] = List()
    var name : String = ""
    for (name <- content.split("[\\p{Punct}\\s]+")){
      if ((!STOP_WORDS_LIST.contains(name.toLowerCase())) && (name.matches("\\b[A-Z][a-z]{1,}\\b"))) {
        if (!jobs.contains((name, in_key))) { //prevent addition and sending of duplicate (name, title)
          jobs = (name, in_key) :: jobs
        }
      }
    }
    jobs
  }
}


class MapJob3 extends Mapper[String, String, String, String]{
  def map(in_key: String, in_value: String): List[(String, String)] = {
    val content = Source.fromFile(in_value).mkString
    var jobs: List[(String, String)] = List()
    var name : String = ""
    for (name <- content.split("\\s+")){
        if (!jobs.contains((name, in_key))) { //prevent addition and sending of duplicate (name, title)
          jobs = (name, in_key) :: jobs
        }
    }
    jobs
  }
}

case class ReduceJob3() extends Reducer[String, String, List[String]] {
  val reduceMap = HashMap[String, List[String]]()
  def reduce (out_key: String, inter_val: String) : Unit = {
    if (reduceMap.contains(out_key)) {
      if (!reduceMap(out_key).contains(inter_val)) {
        reduceMap += (out_key -> (inter_val :: reduceMap(out_key)))
      } }else {
      reduceMap += (out_key -> List(inter_val))
    }
  }
}