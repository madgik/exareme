requirevars 'defaultDB' 'columns' 'classname';
attach database '%{defaultDB}' as defaultDB;

update defaultDB.algorithmparameters set val=val+1 where name ='iterations';

--For each categorical column x:
--segment the data by the distinct values of each column, and by the class values, and then count the rows.
var 'columns1' from getschema outputformat:1 select * from defaultDB.localinputtblcurrent;

drop table if exists localcounts;
create table localcounts (colname text, val text, classval text, quantity int);
var  'a' from select create_complex_query(""," insert into localcounts select '?', `?`, `%{classname}`, count(*)
from  defaultDB.localinputtblcurrent group by `?`, `%{classname}`; ", "" , "" , '%{columns1}');
%{a};

select * from localcounts;
