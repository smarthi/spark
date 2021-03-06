/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.execution.datasources

import org.apache.hadoop.mapreduce.TaskAttemptContext

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.sources.{HadoopFsRelation, HadoopFsRelationProvider, OutputWriter, OutputWriterFactory}
import org.apache.spark.sql.types.StructType

/**
 * A container for bucketing information.
 * Bucketing is a technology for decomposing data sets into more manageable parts, and the number
 * of buckets is fixed so it does not fluctuate with data.
 *
 * @param numBuckets number of buckets.
 * @param bucketColumnNames the names of the columns that used to generate the bucket id.
 * @param sortColumnNames the names of the columns that used to sort data in each bucket.
 */
private[sql] case class BucketSpec(
    numBuckets: Int,
    bucketColumnNames: Seq[String],
    sortColumnNames: Seq[String])

private[sql] trait BucketedHadoopFsRelationProvider extends HadoopFsRelationProvider {
  final override def createRelation(
      sqlContext: SQLContext,
      paths: Array[String],
      dataSchema: Option[StructType],
      partitionColumns: Option[StructType],
      parameters: Map[String, String]): HadoopFsRelation =
    throw new UnsupportedOperationException("use the overload version with bucketSpec parameter")
}

private[sql] abstract class BucketedOutputWriterFactory extends OutputWriterFactory {
  final override def newInstance(
      path: String,
      dataSchema: StructType,
      context: TaskAttemptContext): OutputWriter =
    throw new UnsupportedOperationException("use the overload version with bucketSpec parameter")
}

private[sql] object BucketingUtils {
  // The file name of bucketed data should have 3 parts:
  //   1. some other information in the head of file name, ends with `-`
  //   2. bucket id part, some numbers
  //   3. optional file extension part, in the tail of file name, starts with `.`
  // An example of bucketed parquet file name with bucket id 3:
  //   part-r-00000-2dd664f9-d2c4-4ffe-878f-c6c70c1fb0cb-00003.gz.parquet
  private val bucketedFileName = """.*-(\d+)(?:\..*)?$""".r

  def getBucketId(fileName: String): Option[Int] = fileName match {
    case bucketedFileName(bucketId) => Some(bucketId.toInt)
    case other => None
  }
}
