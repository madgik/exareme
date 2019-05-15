requirevars 'defaultDB' 'iterationNumber';
-- The iterationNumber shows the id of the testing dataset.
attach database '%{defaultDB}' as defaultDB;

--var 'iterationNumber' 0;

--Training Dataset that has one more column: classval
drop table if exists defaultDB.local_trainingsetplusclassval;
create table defaultDB.local_trainingsetplusclassval as
select rid,colname,val,idofset,classval from defaultDB.local_inputTBL
where idofset <> %{iterationNumber};

var 'file' from select  'Trainingset'||%{iterationNumber}||'.csv';
output '%{file}' header:t fromeav select rid,colname,val from defaultDB.local_trainingsetplusclassval;

--For each categorical column x: segment the data by the distinct values of each column, and by the class values, and then count the rows.
drop table if exists defaultDB.local_counts;
create table defaultDB.local_counts as
select colname, val, classval,  'NA' as S1, 'NA' as S2, count(*) as quantity
from defaultDB.local_trainingsetplusclassval
where colname in (select colname1 from defaultDB.local_variablesdatatype_Existing where categorical = 'Yes')
group by colname,classval,val;

-- For each non-categorical column x: segment the data by the class values, and then compute the local mean and variance of x in each class.
insert into defaultDB.local_counts
select colname, 'NA' as val, classval, sum(val) as S1, sum( val*val) as S2, count(val) as quantity
from defaultDB.local_trainingsetplusclassval
where colname in (select colname1 from defaultDB.local_variablesdatatype_Existing where categorical <> 'Yes')
group by colname,classval;

select * from defaultDB.local_counts;
