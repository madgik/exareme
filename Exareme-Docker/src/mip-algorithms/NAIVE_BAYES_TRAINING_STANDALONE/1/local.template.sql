
------------------Input for testing
------------------------------------------------------------------------------
--Test 2
-- var 'x' 'lefthippocampus,righthippocampus,leftententorhinalarea,rightententorhinalarea';
-- var 'y' 'alzheimerbroadcategory';
-- var 'kfold' 5;
-- var 'defaultDB' 'mydefaultDB2.db';
-- attach 'datasets.db' as localDB;
--
-- drop table if exists inputdata;
-- create table inputdata as
-- select %{x},%{y}
-- from (file header:t '/home/eleni/Desktop/HBP/exareme/Exareme-Docker/src/mip-algorithms/unit_tests/datasets/CSVs/desd-synthdata.csv');
-------------------------------------------------------------------------------------

requirevars 'defaultDB' 'input_local_DB' 'db_query' 'x' 'y' ; -- y = classname
attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

--Read dataset
drop table if exists inputdata;
create table inputdata as select * from (%{db_query});

--Read metadata
drop table if exists defaultDB.localmetadatatbl;
create table defaultDB.localmetadatatbl as
select code, sql_type , isCategorical as categorical from metadata where code in (select strsplitv('%{x}','delimiter:,')) or code ='%{y}';

-- Delete patients with null values (val is null or val = '' or val = 'NA'). Cast values of columns using cast function.
var 'nullCondition' from select create_complex_query(""," ? is not null and ? <>'NA' and ? <>'' ", "and" , "" , '%{x},%{y}');
var 'sqltypesxy'from select sqltypestotext(code,sql_type,'%{x},%{y}') from  defaultdb.localmetadatatbl;
var 'cast_xy' from select create_complex_query("","cast(? as ??) as ?", "," , "" , '%{x},%{y}','%{sqltypesxy}');--TODO!!!!
drop table if exists defaultDB.local_trainingset;
create table defaultDB.local_trainingset as
select %{cast_xy} from inputdata where %{nullCondition};

var 'privacy' from select privacychecking(no) from (select count(*) as no from defaultDB.local_trainingset);

select * from defaultDB.localmetadatatbl;
