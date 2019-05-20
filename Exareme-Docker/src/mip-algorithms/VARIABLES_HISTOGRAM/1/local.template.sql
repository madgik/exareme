requirevars 'defaultDB' 'input_local_DB' 'db_query' 'column1' 'column2' 'nobuckets' 'dataset';

attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

-- We assume that a categorical integer does not have more than 32 different values. Check: var 'column1IsCategoricalNumber'
-- We assume that all the columns of text type are categorical


--------------------------------------------- FOR TESTING -------------------------------------------------------------------------
--var 'column1' 'subjectageyears';
-- text columns: subjectcode, adnicategory , agegroup, alzheimerbroadcategory, dataset, gender, neurogenerativescategories, parkinsonbroadcategory, ppmicategory, edsdcategory
-- categorical integer: apoe4 ,minimentalstate
-- real columns:subjectageyears
--var 'column2' 'adnicategory';
--var 'dataset' 'adni,ppmi';
--var 'defaultDB' 'defaultDB';
--var 'nobuckets' 3;
--attach database '%{defaultDB}' as defaultDB;

--drop table if exists inputtablefromraw;
--create table inputtablefromraw as
--select rid, colname, val
--from (file header:t '/home/eleni/Documents/HBP-SGA2/DATASETS FOR TESTING/DATASETS FROM SGA1/chuv_f.csv');
--insert into inputtablefromraw
--select rid, colname, val
--from  (file header:t '/home/eleni/Documents/HBP-SGA2/DATASETS FOR TESTING/DATASETS FROM SGA1/epfl_f.csv');
--insert into inputtablefromraw
--select rid, colname, val
--from (file  header:t '/home/eleni/Documents/HBP-SGA2/DATASETS FOR TESTING/DATASETS FROM SGA1/uoa_f.csv');
---------------------------------------------------------------------------------------------------------------------------------------------

drop table if exists inputtablefromraw;
create table inputtablefromraw as
select rid,colname,  val from (toeav %{db_query});

--Check if nobuckets is empty
var 'nobucketsisempty' from select case when (select '%{nobuckets}')='' then 0 else 1 end;
emptyfield '%{nobucketsisempty}';

--Check if nobuckets is integer
var 'nobucketstype' from select case when (select typeof(tonumber('%{nobuckets}'))) = 'integer' then 1 else 0 end;
vartypebucket '%{nobucketstype}';

var 'columns' from select case when '%{column2}'<>'' then '{"1": "'||'%{column1}'||'","2": "'||'%{column2}'||'"}' else '{"1": "'||'%{column1}'||'"}' end as  columns;
var 'columnsshouldbeoftype' from select case when '%{column2}'<>'' then '{"1": "Real,Float,Integer,Text", "2": "Text"}' else '{"1": "Real,Float,Integer,Text"}' end as  columnstype;

execnselect 'defaultDB' 'columns''columnsshouldbeoftype''dataset'
select filetext('/root/mip-algorithms/VARIABLES_HISTOGRAM/1/CreateInputData.sql');
--select filetext('/root/mip-algorithms/WP_VARIABLES_HISTOGRAM/CreateInputData_v1_EL_SO.sql');

var 'column1IsText' from select case when (select typeof(val) from defaultDB.inputlocaltbl where colname = '%{column1}' limit 1) ='text' then 1 else 0 end;

var 'column1IsCategoricalNumber' from
select case when (select count(distinct val) from defaultDB.inputlocaltbl where colname = '%{column1}')< 32 and
                 (select count(distinct val) from defaultDB.inputlocaltbl where colname = '%{column1}')> 0 and
                  %{column1IsText}=0 then 1 else 0 end;

-----------------------------------------------------------------
drop table if exists defaultDB.localResult;
create table defaultDB.localResult as
-- 1. case when column1 is not text
select * from
(select  '%{column1}' as colname,
       "NA" as val,
       min(val) as minvalue,
       max(val) as maxvalue,
       count(val) as N,
       count(distinct rid) as patients,
       %{column1IsText} as column1IsText,
       %{column1IsCategoricalNumber} as column1IsCategoricalNumber
from defaultDB.inputlocaltbl
where colname = '%{column1}')
where %{column1IsText}=0
union all
 -- 2. case when column1 is text
select null,null,null,null,null,null, %{column1IsText} as column1IsText,%{column1IsCategoricalNumber} as column1IsCategoricalNumber
where %{column1IsText}=1;

---------------------------------------------------------------------------
--Check privacy due to minimum records in each bucket ----
var 'minimumrecords' 10;
var 'containsmorethantheminimumrecords' from
select case when patients < %{minimumrecords} then 0 else 1 end as containsmorethantheminimumrecords
from  defaultDB.localResult
where colname = '%{column1}' and  %{column1IsText}=0
union all
select 1 as containsmorethantheminimumrecords  where %{column1IsText}=1;

--varminimumrec '%{containsmorethantheminimumrecords}'; 		--prepei  = 1
select * from defaultDB.localResult;
