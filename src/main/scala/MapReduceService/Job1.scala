package MapReduceService

import scala.collection.mutable
import scala.collection.mutable.HashMap
import scala.io.Source



class MapJob1 {
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

class ReduceJob1 [A, B]{

  def reduce (out_key: A, inter_val: B) : HashMap[A, Int] = {
    var reduceMap1 = HashMap[A, Int]()
    if (reduceMap1.contains(out_key)) {
      reduceMap1 += (out_key -> (reduceMap1(out_key) + 1))
    } else {
      reduceMap1 += (out_key -> 1)
    }
    reduceMap1
  }


}

class MapJob2 {
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
class ReduceJob2 {

  def reduce (out_key: String, inter_val: String) : HashMap[String, List[String]]  = {
    var reduceMap2 = HashMap[String, List[String]]()
    if (reduceMap2.contains(out_key)) {
      if (!reduceMap2(out_key).contains(inter_val)){
        reduceMap2(out_key) = inter_val::reduceMap2(inter_val)
        //println("ADDING TO LIST")
      }
    } else {
      reduceMap2 += (out_key -> List(inter_val))
    }
    reduceMap2
  }

}
class MapJob3 {
  def map(in_key: String, in_value: String): List[(String, String)] = {
    val content = Source.fromFile(in_value).mkString
    var jobs: List[(String, String)] = List()
    var name : String = ""
    for (name <- content.split("[\\p{Punct}\\s]+")){
        if (!jobs.contains((name, in_key))) { //prevent addition and sending of duplicate (name, title)
          jobs = (name, in_key) :: jobs
        }
    }
    jobs
  }
}
class ReduceJob3 {

  def reduce (out_key: String, inter_val: String) : HashMap[String,List[String]] = {
    var reduceMap3 = HashMap[String,List[String]]()
    if (reduceMap3.contains(out_key)) {
      if (!reduceMap3(out_key).contains(inter_val)){
        reduceMap3(out_key) = inter_val::reduceMap3(inter_val)
        //println("ADDING TO LIST")
      }
    } else {
      reduceMap3 += (out_key -> List(inter_val))
    }
    reduceMap3
  }
}