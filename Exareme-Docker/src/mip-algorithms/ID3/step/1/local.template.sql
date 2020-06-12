requirevars 'defaultDB' 'y';
attach database '%{defaultDB}' as defaultDB;

--For each categorical column x:
--segment the data by the distinct values of each column, and by the class values, and then count the rows.
var 'columns1' from getschema outputformat:1 select * from defaultDB.localinputtblcurrent;

drop table if exists localcounts;
create temp table localcounts (colname text, val text, classval text, quantity int);
var  'a' from select create_complex_query(""," insert into localcounts select '?', `?`, `%{y}`, count(*)
from  defaultDB.localinputtblcurrent group by `?`, `%{y}`; ", "" , "" , '%{columns1}');
%{a};

select * from localcounts;
