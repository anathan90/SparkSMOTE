# SparkSMOTE
The Synthetic Minority Oversampling Technique (SMOTE) implemented in Spark [(see original paper)](https://www.jair.org/media/953/live-953-2037-jair.pdf). This is a very useful method for dealing with highly imbalanced datasets. 

## Get started
```
sbt compile
sbt package
./run
```

## Details
The run file allows you select the **number of nearest neighbors** to consider when creating artificial examples (**K**, default is 5), the **oversampling percentage** (**oversamplingPctg**), and **delimiter** for the data (**delimiter**). 

The data is expected to be formatted in the following way: first column corresponds to the label for that datapoint, whereas all remaining columns are the features. The user must also specify the number of features (**numFeatures**). Any headers must be removed from the data. 
