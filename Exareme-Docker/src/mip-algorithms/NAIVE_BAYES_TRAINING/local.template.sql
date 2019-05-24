requirevars 'defaultDB' 'iterationNumber' 'y';
attach database '%{defaultDB}' as defaultDB;

--var 'iterationNumber' 0;

--Training Dataset
drop table if exists defaultDB.local_trainingset;
create table defaultDB.local_trainingset as
select * from defaultdb.localinputtblflat where idofset <> %{iterationNumber};

--var 'file' from select  'Trainingset'||%{iterationNumber}||'.csv';
--output '%{file}' header:t select * from defaultDB.local_trainingset;

--For each categorical column x: segment the data by the distinct values of each column, and by the class values, and then count the rows.
drop table if exists defaultDB.local_counts;
create table defaultDB.local_counts(colname text, val text, classval text, S1 real, S2 real, quantity int);

--For each categorical column x: segment the data by the distinct values of each column, and by the class values, and then count the rows.
var 'categoricalcolumns' from select case when count(*)==0 then '' else group_concat(code) end from defaultDB.localmetadatatbl where categorical=1;
var 'categorical_localcounts' from select create_complex_query("","
insert into defaultDB.local_counts
select '?' as colname, ? as val, %{y} as classval, 'NA' as S1, 'NA' as S2, count(?) as quantity
from defaultDB.local_trainingset
group by colname,%{y},?;", "" , "" , '%{categoricalcolumns}');
%{categorical_localcounts};

-- For each non-categorical column x: segment the data by the class values, and then compute the local mean and variance of x in each class.--
var 'non_categoricalcolumns' from select case when count(*)==0 then '' else group_concat(code) end from defaultDB.localmetadatatbl where categorical<>1 ;
var 'non_categorical_localcounts' from select create_complex_query("","
insert into defaultDB.local_counts
select '?' as colname, 'NA' as val, %{y} as classval, sum(?) as S1, sum( ?*?) as S2, count(?)
from defaultDB.local_trainingset
group by colname,%{y};", "" , "" , '%{non_categoricalcolumns}');
%{non_categorical_localcounts};

select * from defaultDB.local_counts;
