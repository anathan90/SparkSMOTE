# SparkSMOTE
The Synthetic Minority Oversampling Technique (SMOTE) implemented in Spark [(see original paper)](https://www.jair.org/media/953/live-953-2037-jair.pdf). This is a very useful method for dealing with highly imbalanced datasets. 

## Get started
```
sbt compile
sbt package
./run
```

## Details
### Data format
* Any headers must be removed from the data.
* First column corresponds to the datapoint's label (Y). The remaining clumns are the features. 
* Labels have to 0 and 1. 
* Label 0 (Y=0) corresponds to majority class examples, and label 1 (Y=1) corresponds to minority class examples.

### Algorithmic parameters
Parameters that **MUST** specified in the "run" file:
* **inputDirectory**: Path to training data file.
* **inputDirectory**: Path to output of SMOTE.
* **numFeatures**: Number of features in dataset.

Parameters that can be specified in the "run" file:
* **K**: Number of nearest neighbors to consider when creating artificial examples. If ommited, the default is K=5.
* **oversamplingPctg**: Oversampling percentage, i.e. by how much the size of the minority class will be increased. If ommited, the default is oversamplingPctg=1.0 (100%). 
* **delimiter**: Delimiter of dataset. If ommited, the default is delimiter=",".
* **numPartitions**: After filtering out the majority class data points, the minority class examples are repartitioned -- this prevents ending up with all minority class data points in a single partition and not taking advantage of data parallelism. This variable indicates how many partitions to create. If ommited, the default is numPartitions=20. 
