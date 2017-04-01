import java.io._
import utils._
import SMOTE._
import org.apache.log4j.Logger
import org.apache.log4j.Level
import breeze.linalg._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import scala.collection.mutable.{ArrayBuffer,Map}


object driver {
	
	def main(args: Array[String]) {
			
		val conf = new SparkConf()

		val options = args.map { arg =>
			arg.dropWhile(_ == '-').split('=') match {
				case Array(opt, v) => (opt -> v)
				case Array(opt) => (opt -> "")
				case _ => throw new IllegalArgumentException("Invalid argument: "+arg)
			}
		}.toMap

        val rootLogger = Logger.getRootLogger()
        rootLogger.setLevel(Level.ERROR)

		val sc = new SparkContext(conf)	

		// read in general inputs
		val inputDirectory = options.getOrElse("inputDirectory","")
		val outputDirectory = options.getOrElse("outputDirectory","")
		val numFeatures = options.getOrElse("numFeatures","0").toInt
		val oversamplingPctg = options.getOrElse("oversamplingPctg","1.0").toDouble
        val kNN = options.getOrElse("K","5").toInt
		val delimiter = options.getOrElse("delimiter",",")
		val numPartitions = options.getOrElse("numPartitions","20").toInt

		SMOTE.runSMOTE(sc, inputDirectory, outputDirectory, numFeatures, oversamplingPctg, kNN, delimiter, numPartitions)	

		println("The algorithm has finished running")
		sc.stop()
	}
}
