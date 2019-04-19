# About

[madIS] (http://madgik.github.io/madis) is an extensible relational database system built on top of the [SQLite] (http://www.sqlite.org/) database with extensions implemented in Python (via [APSW] (https://github.com/rogerbinns/apsw) SQLite wrapper).

In usage, madIS, feels like **Hive** (**Hadoop**'s SQL with User Defined Functions language), without the overhead but also without the distributed processing capabilities. Nevertheless madIS can easily handle tens of millions of rows on a single desktop/laptop computer.

madIS' main goal is to promote the handling of data related tasks within an extended relational model. In doing so, it upgrades the database, from having a support role (storing and retrieving data), to being a full data processing system on its own. madIS comes with functions for file import/export, keyword analysis, data mining tasks, fast indexing, pivoting, statistics and workflow execution.

madIS was designed and implemented by a small team of developers at the MaDgIK lab of the University of Athens, under the supervision of professor Yannis Ioannidis.

# madIS is suitable for
## Complex data analysis tasks

With madIS it is very easy to create additional relational functions, or join against external files without first importing them into the database.

madIS also offers a very fast multidimensional index that greatly speeds up multi-constraint joins, even when joining against external sources.

## Data transformations
madIS can already use the file system or network sources as tables. In addition, with a little knowledge of Python, complex data transformation functions can be created and used inside the database. All these can be achieved without having to import the data in the database.

In addition madIS offers an easy to work with, workflow engine that automates data transformation steps.

##Database research
You can easily develop and test a new indexing structure with madIS, as it uses Python for its extensions and already has plenty of code to start from.

# Documentation

You'll find madIS [documentation here] (http://madgik.github.io/madis).

##Installation

[Installation instructions] (http://madgik.github.io/madis/install.html)

# Screenshot

![ScreenShot] (madis-screen.png)
