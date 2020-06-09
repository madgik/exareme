requirevars 'defaultDB' 'input_local_DB' 'db_query' 'x' 'y' 'dataset' ;
attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

--Read dataset and Delete patients with null values (val is null or val = '' or val = 'NA'). Cast values of columns using cast function.
var 'castcolumnsandclassname' from select create_complex_query("","cast(`?` as text) as `?`", "," , "" , '%{x},%{y}');
var 'nullCondition' from select create_complex_query("","`?` is not null and `?`!='NA' and `?`!=''", "and" , "" , '%{x},%{y}');
drop table if exists defaultDB.localinputtbl;
create table defaultDB.localinputtbl as
select %{castcolumnsandclassname}
from  (select %{x},%{y} from (%{db_query})) where %{nullCondition};

var 'privacy' from select privacychecking(no) from (select count(*) as no from defaultDB.localinputtbl);

-- Initialize data needed for executing ID3
drop table if exists defaultDB.localinputtblcurrent;
create table defaultDB.localinputtblcurrent as
select * from defaultDB.localinputtbl;

select 'ok';
