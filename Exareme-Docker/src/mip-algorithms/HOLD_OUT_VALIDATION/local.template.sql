requirevars 'defaultDB' 'input_local_tbl' 'dataset' 'columns' 'classname' 'test_size' 'train_size' 'random_state' 'shuffle';

--'Dataset_BayesNaive_CategoricalValues.csv' -- 'datasetforTestingBayesNaiveNullInput.csv'
--var 'columns' 'outlook,temperature,humidity,windy,column1,column2';
--var 'classname' 'play';
--var 'test_size'  0.90;
--var 'train_size' 0.50;
--var 'random_state' None;
--var 'shuffle' True;
--var 'dataset' 'adni';

-- 'Iris_dataset'
--var 'defaultDB' 'defaultDB';
--var 'columns' 'SepalLength,SepalWidth,PetalLength,PetalWidth';
--var 'classname' 'Species';
--var 'kfold' 2;
--var 'alpha' 1;

attach database '%{defaultDB}' as defaultDB;

var 'min_k_data_aggregation' 5;

---------------------------------------------------------------------------------------------------------------------
--Dataset names
drop table if exists datasetsTBL;
create table datasetsTBL as
select strsplitv('%{dataset}','delimiter:,') as dataset;

--Column names
drop table if exists columnsTBL;
create table columnsTBL as
select strsplitv('%{columns}','delimiter:,') as col;

--------------------------------------------------------------------------------------------------------------------------
drop table if exists defaultDB.local_inputvariables; --contains the names of classname
create table defaultDB.local_inputvariables as
select 'classname' as variablename, '%{classname}' as val;


--Import dataset for testing in madis and select specific datasets and columns
--drop table if exists table1;
--create table table1 as
--select rid, key as colname,  tonumber(val) as val
--from ( select rid, jdictsplitv(cjdict) from (file toj:1 header:t 'Dataset_BayesNaive_Real&CategoricalValues.csv'))
--where colname in (select * from columnsTBL) or colname = '%{classname}' or colname = 'dataset';

drop table if exists table1;
create table table1 as
select rid,colname, tonumber(val) as val from (toeav select * from %{input_local_tbl})
where colname in (select * from columnsTBL) or colname = '%{classname}' or colname = 'dataset';

--Keep only patients of the correct dataset & delete the rows which are colname = 'dataset'
drop table if exists table2;
create table table2 as
select rid, colname, val
from table1
where rid in (select distinct rid from table1 where colname ='dataset' and val in (select * from datasetsTBL));
delete from table2 where colname = 'dataset';

-- Delete patients with null values
drop table if exists table3;
create table table3 as
select rid, colname, val
from table2
where rid not in (select distinct rid from table2 where val is null or val = '' or val = 'NA');

--------------------------------------------------------------------------------------------------------------
--Define the type of the "columns" & "classname"  -- TODO:  We should read the type of variables from a metadata file.
drop table if exists defaultDB.local_variablesdatatype_Existing;
create table defaultDB.local_variablesdatatype_Existing as
select colname1, type,
       case when type <> 'real' and uniquevalues <= 30 then 'Yes' else 'No' end as categorical
	   from
      (select colname as colname1, count (distinct val) as uniquevalues from table3 group by colname),
      (select distinct colname as colname2, typeof(val) as type from table3 group by colname)
where colname1=colname2;

--Check the type of the "kfold" -- TODO SOFIA:  If the result of the query is 0 then wrong type "kfold"
select typeof(tonumber('%{kfold}')) ='integer' as typeint;

-- Check the type of "classname"  -- TODO SOFIA:  If the result of the query is 0 then wrong type "classname"
select categorical = 'Yes' from defaultDB.local_variablesdatatype_Existing where colname1 = '%{classname}';

-----------------------------------------------------------------------------------------------------------------
-- Add two new columns: "idofset","classval"
-- "idofset" is used in order to split dataset in training and test datasets.
drop table if exists defaultDB.local_inputTBL;
create table defaultDB.local_inputTBL as
select h.rid as rid, h.colname as colname, h.val as val , case when holdout.idofset == 'Train' then 0 else 1 end as idofset,  c.val as classval
from table3  as h,
  (traintestsplit test_size:%{test_size} train_size:%{train_size} random_state:%{random_state} shuffle:%{shuffle} select distinct rid from table3) as holdout,
  (select rid, val from table3 where colname = var('classname')) as c
where h.rid = c.rid and holdout.rid =h.rid;

--Check that initial dataset conatins more than "min_k_data_aggregation" rows
select count(distinct(rid))>= %{min_k_data_aggregation} from defaultDB.local_inputTBL;  -- TODO SOfia: Prepei na epistrefei 1 gia na sunexizei to nosokomeio


select * from defaultDB.local_variablesdatatype_Existing;
