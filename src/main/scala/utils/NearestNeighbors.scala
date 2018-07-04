package utils

import org.apache.spark.SparkContext
import breeze.linalg._
import breeze.linalg.{DenseVector,Vector,SparseVector}
import com.github.fommil.netlib.BLAS
import scala.util.Random
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import scala.collection.mutable.ArrayBuffer

object NearestNeighbors {

	def runNearestNeighbors(data: RDD[Array[(LabeledPoint,Int,Int)]], 
		kNN: Int, 
		sampleData: Array[(LabeledPoint,Int,Int,Int)]): Array[(String,Array[((Int,Int),Double)])] = {
		
		val globalNearestNeighborsByIndex = data.mapPartitionsWithIndex(localNearestNeighbors(_,_,kNN,sampleData)).groupByKey().map(x => (x._1,x._2.toArray.sortBy(r => r._2).take(kNN))).collect()		

		globalNearestNeighborsByIndex 
	}


	private def localNearestNeighbors(partitionIndex: Long,
		iter: Iterator[Array[(LabeledPoint,Int,Int)]],
		kNN: Int,
		sampleData: Array[(LabeledPoint,Int,Int,Int)]): Iterator[(String,((Int,Int),Double))] = {
			
			var result = List[(String,((Int,Int),Double))]()
			val dataArr = iter.next
			val nLocal = dataArr.size - 1			
			val sampleDataSize = sampleData.size - 1


			val kLocalNeighbors = Array.fill[distanceIndex](sampleDataSize+1)(null)
			for {
			    i1 <- 0 to sampleDataSize
			} 
			kLocalNeighbors(i1) = distanceIndex(sampleData(i1)._4.toInt, sampleData(i1)._2.toInt, DenseVector.zeros[Double](kNN) + Int.MaxValue.toDouble, DenseVector.zeros[Int](kNN))

			for (i <- 0 to nLocal) {
				val currentPoint = dataArr(i)
				val features = currentPoint._1.features
				val rowId = currentPoint._3.toInt	
				for (j <- 0 to sampleDataSize) {
					val samplePartitionId = sampleData(j)._2
					val sampleRowId = sampleData(j)._3
					val sampleFeatures = sampleData(j)._1.features
					if (!((rowId == sampleRowId) & (samplePartitionId == partitionIndex))) {
						val distance = Math.sqrt(sum((sampleFeatures - features) :* (sampleFeatures - features)))
						if (distance < max(kLocalNeighbors(j).distanceVector)) {
							val indexToReplace = argmax(kLocalNeighbors(j).distanceVector)
							kLocalNeighbors(j).distanceVector(indexToReplace) = distance
							kLocalNeighbors(j).neighborRowId(indexToReplace) = rowId
						}
					}
				}
			}
			for (m <- 0 to sampleDataSize){
				for (l <-0 to kNN-1) {
					
					val key = kLocalNeighbors(m).partitionId.toString+","+kLocalNeighbors(m).sampleRowId.toString
					val tup = (partitionIndex.toInt,kLocalNeighbors(m).neighborRowId(l))
					result.::=(key,(tup,kLocalNeighbors(m).distanceVector(l)))
				}
			}			
		result.iterator 
	}	
}
