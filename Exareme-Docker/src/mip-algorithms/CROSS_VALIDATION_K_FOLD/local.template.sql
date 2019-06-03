
------------------Input for testing
------------------------------------------------------------------------------
--Test 1
-- var 'x' 'car_buying,car_maint,car_doors,car_persons,car_lug_boot,car_safety';
-- var 'y' 'car_class';
-- var 'kfold' 3;
-- var 'defaultDB' 'mydefaultDB2.db';
-- attach 'datasets.db' as localDB;
-- --
-- drop table if exists inputdata;
-- create table inputdata as
-- select %{x},%{y}
-- from (file header:t '/home/eleni/Desktop/HBP/exareme/Exareme-Docker/src/mip-algorithms/unit_tests/datasets/CSVs/car.csv');


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
--

-------------------------------------------------------------------------------------

requirevars 'defaultDB' 'input_local_DB' 'db_query' 'x' 'y' 'kfold'; -- y = classname
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
drop table if exists inputdata2;
create table inputdata2 as
select %{cast_xy} from inputdata where %{nullCondition};

-- Add a new column: "idofset". It is used in order to split dataset in training and test datasets.
drop table if exists defaultDB.localinputtblflat;
create table defaultDB.localinputtblflat as
select %{x},%{y}, kfold.idofset as idofset
from inputdata2  as h, (sklearnkfold splits:%{kfold} select rowid from inputdata2) as kfold
where kfold.rid = h.rowid;

var 'privacy' from select privacychecking(no) from (select count(*) as no from defaultDB.localinputtblflat where idofset==0);


-- var 'file' from select  'worker2Dataset.csv';
-- output '%{file}' header:t select * from defaultDB.localinputtblflat;

select * from defaultDB.localmetadatatbl;
