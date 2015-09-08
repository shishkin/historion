# Historion
VCS log mining in Scala.

Inspired by [Your Code as a Crime Scene](http://pragprog.com/book/atcrime/code-as-a-crime-scene).

## Core

Core implements the analyses in pure scala. For performance reasons flat changes
from the repository log are loaded into memory as a parallel vector. This data 
structure is fast to perform analytical operations in a multi-core environment.

### How to run

Start SBT console of the core module `sbt core/console`. Then:

    scala> val git = GitSource("/path/to/repo/")
    ...

    scala> val changes = git.fileStats()
    ... this operation might take time for big repositories

    scala> changes.summary()
    res0: historion.Summary = Summary(12766,20658,864,118448)

## Spark

Spark module is a learning experiment using [Apache Spark](http://spark.apache.org/).
Doing analytics on Spark can be beneficial if the dataset is bigger than local 
memory. Spark can also cache intermediary results to speedup subsequent 
computations (especially repetitive interactive analytics). Spark streaming can
also be interesting to investigate for real-time analytics.

### How to run

To test locally start SBT console of the spark module `sbt spark/console`. Then:

    scala> val git = GitSource("/path/to/repo/")
    ...

    scala> val changes = git.fileStats().toRdd()
    ...

    scala> changes.summary()
    ...
    res0: historion.Summary = Summary(12766,20658,864,118448)

See [Apache Spark documentation](http://spark.apache.org/docs/latest/submitting-applications.html)
 for information on clustered deployment.