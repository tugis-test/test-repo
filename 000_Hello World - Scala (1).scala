// Databricks notebook source
// MAGIC %md #**WordCount Example**
// MAGIC 
// MAGIC ###Goal:  Determine the most popular words in a given text file using Scala and SQL

// COMMAND ----------

// MAGIC %fs ls /databricks-datasets/SPARK_README.md

// COMMAND ----------

// MAGIC %md ### ![](http://training.databricks.com/databricks_guide/downarrow.png) **Step 1**: Load text file from our [Hosted Datasets](/#workspace/databricks_guide/04 Importing Data/8 Hosted Datasets).  **Shift-Enter runs the code below.**

// COMMAND ----------

// MAGIC %fs ls "dbfs:/databricks-datasets/cs100/lab3/data-001"

// COMMAND ----------

val filePath = "dbfs:/databricks-datasets/SPARK_README.md"
//val filePath = "dbfs:/databricks-datasets/cs100/lab1/data-001/shakespeare.txt" // path in Databricks File System
val lines = sc.textFile(filePath) // read the file into the cluster
lines.take(10).mkString("\n") // display first 10 lines in the file

// COMMAND ----------

// MAGIC %md ###![](http://training.databricks.com/databricks_guide/downarrow.png) **Step 2**:  Inspect the number of partitions (workers) used to store the dataset

// COMMAND ----------

val numPartitions = lines.partitions.length // get the number of partitions
println(s"Number of partitions (workers) storing the dataset = $numPartitions")

// COMMAND ----------

// MAGIC %md ###![](http://training.databricks.com/databricks_guide/downarrow.png) **Step 3**:  Split each line into a list of words separated by a space from the dataset

// COMMAND ----------

val words = lines.flatMap(x => x.split(' ')) // split each line into a list of words
words.take(10).mkString("\n") // display the first 10 words

// COMMAND ----------

// MAGIC %md ###![](http://training.databricks.com/databricks_guide/downarrow.png) **Step 4**:  Filter the list of words to exclude common stop words

// COMMAND ----------

val stopWords = Seq("","a","*","and","is","of","the","a") // define the list of stop words
val filteredWords = words.filter(x => !stopWords.contains(x.toLowerCase())) // filter the words
filteredWords.take(10).mkString("\n") // display the first 10 filtered words

// COMMAND ----------

// MAGIC %md ###![](http://training.databricks.com/databricks_guide/downarrow.png) **Step 5**:  Cache the filtered dataset in memory to speed up future actions.

// COMMAND ----------

filteredWords.cache() // cache filtered dataset into memory across the cluster worker nodes
filteredWords.count()  // materialize the cache

// COMMAND ----------

// MAGIC %md ###![](http://training.databricks.com/databricks_guide/downarrow.png) **Step 6**:  Transform filtered words into list of (word,1) tuples for WordCount

// COMMAND ----------

val word1Tuples = filteredWords.map(x => (x, 1)) // map the words into (word,1) tuples
word1Tuples.take(10).mkString("\n") // display the (word,1) tuples

// COMMAND ----------

// MAGIC %md ###![](http://training.databricks.com/databricks_guide/downarrow.png) **Step 7**:  Aggregate the (word,1) tuples into (word,count) tuples

// COMMAND ----------

val wordCountTuples = word1Tuples.reduceByKey{case (x, y) => x + y} // aggregate counts for each word
wordCountTuples.take(10).mkString("\n") // display the first 10 (word,count) tuples

// COMMAND ----------

// MAGIC %md ###![](http://training.databricks.com/databricks_guide/downarrow.png) **Step 8**:  Display the top 10 (word,count) tuples by count

// COMMAND ----------

val sortedWordCountTuples = 
  wordCountTuples.top(10)(Ordering.by(tuple => tuple._2)).mkString("\n") // top 10 (word,count) tuples

// COMMAND ----------

// MAGIC %md ###![](http://training.databricks.com/databricks_guide/downarrow.png) **Step 9**:  Create a table from the (word,count) tuples

// COMMAND ----------

case class WordCount(word: String, count: Int) // create a case class to name the tuple elements
val wordCountRows = wordCountTuples.map(x => WordCount(x._1,x._2)) // tuples -> WordCount 
wordCountRows.toDF.createOrReplaceTempView("word_count") // convert RDDs to DataFrames and register a temp table for querying

// COMMAND ----------

// MAGIC %md ###![](http://training.databricks.com/databricks_guide/downarrow.png) **Step 10**:  Use SQL to visualize the words with count >= 2

// COMMAND ----------

// MAGIC %sql 
// MAGIC SELECT word, count 
// MAGIC FROM word_count 
// MAGIC WHERE count >= 2 
// MAGIC ORDER BY count DESC --use SQL to query words with count >= 2 in descending order

// COMMAND ----------

// MAGIC %md ### Congrats! First Notebook for Hello Word of Distributed Computing
