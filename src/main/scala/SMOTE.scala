package SMOTE

import org.apache.spark.SparkContext
import breeze.linalg._
import breeze.linalg.{DenseVector,Vector,SparseVector}
import com.github.fommil.netlib.BLAS
import scala.util.Random
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import scala.collection.mutable.ArrayBuffer
import utils._

object SMOTE {

	def runSMOTE(sc: SparkContext, 
		inPath: String, 
		outPath: String,
		numFeatures: Int,  
		oversamplingPctg: Double,
        kNN: Int,
		delimiter: String,
        numPartitions: Int): Unit = {

		val rand = new Random()

		val data = loadData.readDelimitedData(sc, inPath, numFeatures, delimiter, numPartitions)
		
		val dataArray = data.mapPartitions(x => Iterator(x.toArray)).cache()

        val numObs = dataArray.map(x => x.size).reduce(_+_)

		println("Number of Filtered Observations "+numObs.toString)

		val incrementPctg = oversamplingPctg + 1
		val floorOver = math.floor(incrementPctg).toInt
		val clonedRDD = (1 to floorOver).map(_ => dataArray).reduce(_ union _)
		val nToSample = math.round(numObs * incrementPctg).toInt
		val sampleData = clonedRDD.flatMap(x => x)
			.takeSample(false, num = nToSample, seed = 1L)
			.zipWithIndex
			.map{ case ((lp, pi, ri), di) => (lp, pi, ri, di)}
			.sortBy(r => (r._2, r._3))

		println("Sample Data Count "+sampleData.size.toString)

	 	val globalNearestNeighbors = NearestNeighbors.runNearestNeighbors(dataArray, kNN, sampleData)
		
        var randomNearestNeighbor = globalNearestNeighbors.map(x => (x._1.split(",")(0).toInt,x._1.split(",")(1).toInt,x._2(rand.nextInt(kNN)))).sortBy(r => (r._1,r._2))
		
        var sampleDataNearestNeighbors = randomNearestNeighbor.zip(sampleData).map(x => (x._1._3._1._1, x._1._2, x._1._3._1._2, x._2._1))

		val syntheticData = dataArray.mapPartitionsWithIndex(createSyntheticData(_,_,sampleDataNearestNeighbors,delimiter)).persist()
		println("Synthetic Data Count "+syntheticData.count.toString)
		val newData = syntheticData.union(sc.textFile(inPath))
		println("New Line Count "+newData.count.toString)
		newData.saveAsTextFile(outPath)
	
	}

	private def createSyntheticData(partitionIndex: Long,
		iter: Iterator[Array[(LabeledPoint,Int,Int)]],
		sampleDataNN: Array[(Int,Int,Int,LabeledPoint)],
		delimiter: String): Iterator[String]  = {
			
			var result = List[String]()
			val dataArr = iter.next
			val nLocal = dataArr.size - 1			
			val sampleDataNNSize = sampleDataNN.size - 1
			val rand = new Random()			

			for (j <- 0 to sampleDataNNSize){
				val partitionId = sampleDataNN(j)._1
				val neighborId = sampleDataNN(j)._3
				val sampleFeatures = sampleDataNN(j)._4.features
				if (partitionId == partitionIndex.toInt){
					val currentPoint = dataArr(neighborId)	
					val features = currentPoint._1.features	
					sampleFeatures += (sampleFeatures - features) * rand.nextDouble
					result.::=("1.0"+delimiter+sampleFeatures.toArray.mkString(delimiter))	
				}
			}
		result.iterator
	}		
}
