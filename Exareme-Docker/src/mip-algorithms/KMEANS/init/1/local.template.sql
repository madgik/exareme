------------------Input for testing
------------------------------------------------------------------------------
--
-- hidden var 'defaultDB' defaultDB_KMEANS2;
-- hidden var 'columns' 'Iris_Sepal_Length,Iris_Sepal_Width,Iris_Petal_Length,Iris_Petal_Width';
-- var 'centers' '';
-- '[{"clid":1, "Iris_Sepal_Length":6.0, "Iris_Sepal_Width":2.5, "Iris_Petal_Length":4.0 ,"Iris_Petal_Width":1.5 },
-- {"clid":2, "Iris_Sepal_Length":5.0, "Iris_Sepal_Width":3.5, "Iris_Petal_Length":1.5 ,"Iris_Petal_Width":0.5},
-- {"clid":3, "Iris_Sepal_Length":6.5, "Iris_Sepal_Width":3.0, "Iris_Petal_Length":6.0 ,"Iris_Petal_Width":2.0}]';
--
-- var 'k' 3;
-- hidden var 'outputformat' 'pfa';
--
-- drop table if exists inputdata;
-- create table inputdata as
--    select %{columns}
--    from (file header:t '/home/eleni/Desktop/HBP/exareme/Exareme-Docker/src/mip-algorithms/KMEANS_accurate/iris.csv');
-- --where dataset = 'Iris';
--
-- select * from inputdata;

------------------ End input for testing
-----------------------------------------------------------------------------
--Error Handling --TODO!!!!!!!!!!!!!!!!!
--k or centers should be null. Otherwise the algorithm should stop. The algorithm should stop if Var 'error' ==1 . TODO Sofia k>=2
--var 'error' from  select case when tonumber(%{centersisempty}) + tonumber(%{kisempty}) =1 then 0 else 1 end;

requirevars 'defaultDB' 'input_local_DB' 'db_query' 'db_query' 'columns' 'centers' 'k' 'dataset' 'outputformat';
attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

--Read dataset
drop table if exists inputdata;
create table inputdata as
select %{columns} from (%{db_query});

-- drop table if exists defaultDB.lala;
-- create table defaultDB.lala as select * from inputdata;

drop table if exists defaultDB.algorithmparameters; --used for testing !!!
create table defaultDB.algorithmparameters (name,val);
insert into defaultDB.algorithmparameters select 'centers' , '%{centers}' ;
insert into defaultDB.algorithmparameters select 'columns' , '%{columns}' ;
insert into defaultDB.algorithmparameters select 'outputformat' , '%{outputformat}' ;
insert into defaultDB.algorithmparameters select 'iterations' , 0 ;

-- Delete patients with null values (val is null or val = '' or val = 'NA'). Cast values of columns using cast function.
var 'nullCondition' from select create_complex_query(""," ? is not null and ? <>'NA' and ? <>'' ", "and" , "" , '%{columns}');
var 'cast_columns' from select create_complex_query("","tonumber(?) as ?", "," , "" , '%{columns}');
drop table if exists defaultDB.localinputtbl;
create table defaultDB.localinputtbl as
select cast(rowid as text) as rid, %{cast_columns}
from inputdata where %{nullCondition};

var 'privacy' from select privacychecking(no) from (select count(*) as no from defaultDB.localinputtbl);
var 'inputerrorchecking' from select kmeans_inputerrorchecking('%{centers}','%{k}');

drop table if exists defaultDB.assignnearestcluster;
create table defaultDB.assignnearestcluster(rid int primary key, clid int, mindist real);

var 'schema' from select create_complex_query("clid int, clN int,","?_clS real",",","",'%{columns}');
var 'partialSums' from select create_complex_query("clid, count(*) as clN,", "sum(?) as ?_clS" , "," , '' ,'%{columns}');
var 'nulls' from select create_complex_query("null,null," , "null", ",","",'%{columns}');

var 'centersisempty' from select case when (select '%{centers}')='' then 1 else 0 end;
var 'k' from select  case when  %{centersisempty}= 0 then 2 else tonumber(%{k}) end;

drop table if exists defaultDB.partialclustercenters;
create table defaultDB.partialclustercenters (%{schema});
insert into defaultDB.partialclustercenters
select  %{partialSums}
from (   select rid, clid, %{columns}
         from defaultDB.localinputtbl,
              (select rid as rid1,idofset as clid from (sklearnkfold splits:%{k} select distinct rid from defaultDB.localinputtbl))
         where rid1 =rid)
where %{centersisempty} = 1
group by clid;

select * from defaultDB.partialclustercenters;
