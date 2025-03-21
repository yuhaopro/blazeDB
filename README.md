
<h1 align="center">
  <br>
<img src="images/blazeDB-fire-logo.png" alt="blazeDB logo" width="200"></a>

</h1>

<h4 align="center">A lightweight in-memory database CLI that supports basic SQL queries.</h4>

<p align="center">
    <img src="https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white"
         alt="Java openjdk 21.0.6">
</p>

<p align="center">
  <a href="#tasks">Tasks</a> •
  <a href="#key-features">Key Features</a> •
  <a href="#how-to-use">How To Use</a> •
  <a href="#download">Download</a> •
  <a href="#credits">Credits</a>
</p>

![screenshot](images/blazedb_edited.GIF)

## Tasks
### Task 1
* The join conditions are extracted using the `SplitExpressionDeparser`. A join condition will only consist of columns from different tables. This can be identified by evaluating the left and right columns and getting their table names. The extracted join condition will be stored in a hashmap with the table as it's key.

```
Student: [Student.A = Enrolled.A, Student.B = Course.D]

Enrolled: [Student.A = Enrolled.A]

Course: [Student.B = Course.D]
```

I can then find the list intersections in 2 tables to be joined, identifying the correct join expression. If there are multiple join expressions for the same 2 tables, it will be combined together to form 1 single join expression using AND.

### Task 2
* My optimization rule involves using projection to reduce the total number of tuples that will be processed prior to join, sort and group by operations. The projection captures all the columns that are required in the SELECT, WHERE and GROUP BY clause, and there wouldn't be any missing columns further up in the pipeline.

* I also extracted single table expressions and store them into a hashmap which can be combined together later. This ensures I only create 1 Select Operator for each Table, and thus would result in a smaller memory allocation on the heap due to fewer objects. This will help reduce the possibility of out of memory errors.

## Key Features

* Support SQL queries involving selection and projection, distinct, sort by and group by.
    - Does not support partial projection for all columns. `SELECT Student.*, Enrolled.E FROM Student, Enrolled` 


```sql
SELECT * FROM Student;
SELECT Student.A FROM Student;
SELECT Student.A FROM Student WHERE Student.B > 5;
```
* Support join queries with join expression in the WHERE clause.
    - Does not support join queries using JOIN. `SELECT * FROM Student JOIN Enrolled ON Student.A = Enrolled.A`
```sql
SELECT * FROM Student, Enrolled;
SELECT * FROM Student, Enrolled WHERE Student.A = Enrolled.A
```
* Only supports `SUM` aggregations containing multiplication, column or literal.
```sql
SELECT SUM(Student.A * Student.A) FROM Student
SELECT SUM(1) FROM Student GROUP BY Student.A
```




## How To Use

Please take a look at the samples directory to look at how the directory structure of the data and schema is supplied to the query evaluator.

To clone and run this application, you'll need [Git](https://git-scm.com) and [Java openJDK](https://jdk.java.net/java-se-ri/21) installed on your computer. From your command line:

```bash
# Install git
sudo apt install git

# Install java
sudo apt install openjdk-21-jre-headless:amd64

# Clone this repository
$ git clone https://github.com/yuhaopro/blazeDB.git

# Go into the repository
$ cd blazeDB

# Install dependencies
$ mvn clean compile assembly:single

# Run the jar file
$ java -jar target/blazedb-1.0.0-jar-with-dependencies.jar <db directory> <example.sql> <output.csv>
```


## Download

You can [download](https://github.com/yuhaopro/blazeDB/releases/tag/v1.0.0) the latest installable version of blazeDB.

## Credits

This is part of my advanced database coursework in the University of Edinburgh. Hope you like it! 

This software uses the following open source packages:

- [jsqparser](https://github.com/JSQLParser/JSqlParser)

---
