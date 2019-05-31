------------------Input for testing
------------------------------------------------------------------------------
--Test 1
-- var 'x' 'car_buying,car_maint,car_doors,car_persons,car_lug_boot,car_safety';
-- var 'y' 'car_class';
-- var 'test_size'  0.90;
-- var 'train_size' 0.50;
-- var 'random_state' None;
-- var 'shuffle' True;
--
-- drop table if exists inputdata;
-- create table inputdata as
-- select %{x},%{y}
-- from (file header:t '/home/eleni/Desktop/HBP/exareme/Exareme-Docker/src/mip-algorithms/unit_tests/datasets/CSVs/car.csv');

-------------------------------------------------------------------------------------

requirevars 'defaultDB' 'input_local_DB' 'db_query' 'dataset' 'columns' 'classname' 'test_size' 'train_size' 'random_state' 'shuffle';
attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

--Read dataset
drop table if exists inputdata;
create table inputdata as select * from (%{db_query});

-- Delete patients with null values (val is null or val = '' or val = 'NA'). Cast values of columns using cast function.
var 'nullCondition' from select create_complex_query(""," ? is not null and ? <>'NA' and ? <>'' ", "and" , "" , '%{x},%{y}');
var 'cast_x' from select create_complex_query("","tonumber(?) as ?", "," , "" , '%{x}');--TODO!!!!
drop table if exists inputdata2;
create table inputdata2 as
select %{cast_x}, tonumber(%{y}) as '%{y}' from inputdata where %{nullCondition};

-- Add a new column: "idofset". It is used in order to split dataset in training and test datasets.
drop table if exists defaultDB.localinputtblflat;
create table defaultDB.localinputtblflat as
select %{x},%{y}, holdout.idofset as idofset
from inputdata2  as h, (traintestsplit test_size:%{test_size} train_size:%{train_size} random_state:%{random_state} shuffle:%{shuffle} select rowid from inputdata2) as holdout
where holdout.rid = h.rowid;

drop table if exists defaultDB.localmetadatatbl;
create table defaultDB.localmetadatatbl as
select code, isCategorical as categorical from metadata where code in (select strsplitv('%{x}','delimiter:,')) or code ='%{y}';

select * from defaultDB.localmetadatatbl;
