package MapReduceService
import scala.io.Source

class Jobs {
      val job1Map = (x: String, y: String) => {
        val content = Source.fromFile(y).mkString
        var jobs: List[(Any, String)] = List()
        var word : String = ""
        for (word <- content.split("[\\p{Punct}\\s]+")){
          if (!jobs.contains(word, x)){
            jobs = (word, x) :: jobs
          }
        }
        jobs
      }









}
