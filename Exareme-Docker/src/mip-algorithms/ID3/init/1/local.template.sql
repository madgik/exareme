------------------Input for testing
------------------------------------------------------------------------------
--
-- hidden var 'defaultDB' defaultDB_ID3;
-- hidden var 'columns' 'car_buying,car_maint,car_doors,car_persons,car_lug_boot,car_safety';
-- var 'classname' 'car_class';
-- hidden var 'outputformat' 'json';
--
-- drop table if exists inputdata;
-- create table inputdata as
--    select %{columns},%{classname}
--    from (file header:t '/home/eleni/Desktop/HBP/exareme/Exareme-Docker/src/mip-algorithms/ID3/car.csv');

--
-- select * from inputdata;

------------------ End input for testing
-----------------------------------------------------------------------------
--Error Handling --TODO!!!!!!!!!!!!!!!!!
--k or centers should be null. Otherwise the algorithm should stop. The algorithm should stop if Var 'error' ==1 . TODO Sofia k>=2
--var 'error' from  select case when tonumber(%{centersisempty}) + tonumber(%{kisempty}) =1 then 0 else 1 end;

requirevars 'defaultDB' 'input_local_DB' 'db_query' 'columns' 'classname' 'dataset' 'outputformat';
attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

--Read dataset
drop table if exists inputdata;
create table inputdata as
select %{columns},%{classname} from (%{db_query});


drop table if exists defaultDB.algorithmparameters; --used for testing !!!
create table defaultDB.algorithmparameters (name,val);
insert into defaultDB.algorithmparameters select 'classname' , '%{clasname}' ;
insert into defaultDB.algorithmparameters select 'columns' , '%{columns}' ;
insert into defaultDB.algorithmparameters select 'iterations' , 0 ;

-- Delete patients with null values (val is null or val = '' or val = 'NA'). Cast values of columns using cast function.
var 'castcolumnsandclassname' from select create_complex_query("","cast(`?` as text) as `?`", "," , "" , '%{columns},%{classname}');
var 'nullCondition' from select create_complex_query("","`?` is not null and `?`!='NA' and `?`!=''", "and" , "" , '%{columns},%{classname}');
drop table if exists defaultDB.localinputtbl;
create table defaultDB.localinputtbl as
select %{castcolumnsandclassname}
from inputdata where %{nullCondition};

-- Initialize data needed for executing ID3
drop table if exists defaultDB.localinputtblcurrent;
create table defaultDB.localinputtblcurrent as
select * from defaultDB.localinputtbl;

select 'ok';
