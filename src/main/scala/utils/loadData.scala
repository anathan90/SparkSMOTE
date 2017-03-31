package utils

import org.apache.spark.SparkContext
import breeze.linalg._
import breeze.linalg.{DenseVector,Vector,SparseVector}
import org.apache.spark.rdd.RDD
import org.apache.spark.broadcast.Broadcast

object loadData {

 	def readDelimitedData(sc: SparkContext, path: String, numFeatures: Int, delimiter: String): RDD[(LabeledPoint,Int,Int)] = {
		val data = sc.textFile(path).filter{x => x.split(delimiter)(0).toDouble == 1.0}.repartition(20).mapPartitions{x => Iterator(x.toArray)}
		val formatData = data.mapPartitionsWithIndex{(partitionId,iter) =>
			var result = List[(LabeledPoint,Int,Int)]()
			val dataArray = iter.next
			val dataArraySize = dataArray.size - 1
			var rowCount = dataArraySize
			for (i <- 0 to dataArraySize) {
				val parts = dataArray(i).split(delimiter)
				result.::=((LabeledPoint(parts(0).toDouble,DenseVector(parts.slice(1,numFeatures+1)).map(_.toDouble)),partitionId.toInt,rowCount))
				rowCount = rowCount - 1
			}
			result.iterator
		}

		formatData
	}
	
}
