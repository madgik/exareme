requirevars 'defaultDB' 'columns' 'columnsshouldbeoftype''dataset' ;
--The algorithm wil be executed only if the variables have the following values: 
--var 'checkifcolumnsisempty' --> 1 
--var 'checkifdatasetsisempty'--> 1
--var 'checkifcolumnsexistinthedataset' --> 1
--var 'checkifcolumnstypeiscorrect' --> 1
--Table3 is the outputtable


---- FOR TESTING -------------------------------------------------------------------------
--drop table if exists table1;
--create table table1 as 
--select rid, colname, val 
--from (file header:t '/home/eleni/Documents/HBP-SGA2/DATASETS FOR TESTING/DATASETS FROM SGA1/chuv_f.csv');

--insert into table1 
--select rid, colname, val 
--from  (file header:t '/home/eleni/Documents/HBP-SGA2/DATASETS FOR TESTING/DATASETS FROM SGA1/epfl_f.csv');
--insert into table1 
--select rid, colname, val 
--from (file  header:t '/home/eleni/Documents/HBP-SGA2/DATASETS FOR TESTING/DATASETS FROM SGA1/uoa_f.csv');


--var 'defaultDB' 'defaultDB'; 
--var 'columns' '{"1": "apoe4","2": "av45"}';
--var 'columnsshouldbeoftype' '{"1": "Real,Float,Integer,Text", "2": "Text"}';
--var 'datasets' 'adni,ppmi'; -- edsd

------------------------------------------------------------------------------------------
--attach database '%{defaultDB}' as defaultDB;

--1. Check if variable columns is empty then 0 else 1
var 'checkifcolumnsisempty' from select case when (select '%{columns}')='' then 0 else 1 end;

--Column names
drop table if exists columns;
create table columns as
select key as id, val as colname from 
(select jdictsplitv('%{columns}'));

-- Keep only the  datasets and columns at hand
drop table if exists table2; 
create table table2 as
select rid, colname, tonumber(val) as val
from inputtablefromraw
where colname in (select colname from columns);


--3. Check if columns exist in the dataset
var 'checkifcolumnsexistinthedataset' from select col1 = col2 
from (select count(colname) as col1 from columns), 
(select count(distinct colname) as col2 from table2);


--3.  Delete patients with null values 
drop table if exists defaultDB.inputlocaltbl;
create table defaultDB.inputlocaltbl as 
select rid, colname, val
from table2
where rid not in (select distinct rid from table2 
                  where val is null or val = '' or val = 'NA');


--4. Check types of columns based on the metadatafile
drop table if exists columnsshouldbeoftype;
create table columnsshouldbeoftype as
select id, strsplitv(colname,'dialect:csv') as type 
from (select key as id, val as colname 
      from (select jdictsplitv('%{columnsshouldbeoftype}')));

drop table if exists columns2;
create table columns2 as
select id, colname1,type from 
(select id,colname as colname1 from columns),
(select distinct colname as colname2, typeof(tonumber(val)) as type from defaultDB.inputlocaltbl group by colname)
where colname1=colname2;

var 'checkifcolumnstypeiscorrect'from
select case when count(*)=0 then 1 else 0 end from(
select jmerge(id,type) as c from columns2 where c not in (select jmerge(id,type)));

--emptyfield '%{checkifcolumnsisempty}';
--vars '%{checkifcolumnsexistinthedataset}';
--vartypeshistogram '%{checkifcolumnstypeiscorrect}';

select '%{checkifcolumnsisempty}' as v1, '%{checkifcolumnsexistinthedataset}' as v2, '%{checkifcolumnstypeiscorrect}' as v3;




