requirevars 'defaultDB' 'input_local_DB' 'db_query' 'x' 'y' 'kfold'; -- y = classname
attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

--Read dataset
drop table if exists inputdata;
create temp table inputdata as select * from (%{db_query});

--Read metadata
drop table if exists defaultDB.localmetadatatbl;
create table defaultDB.localmetadatatbl as
select code, sql_type , isCategorical as categorical from metadata where code in (select strsplitv('%{x}','delimiter:,')) or code ='%{y}';

-- Delete patients with null values (val is null or val = '' or val = 'NA'). Cast values of columns using cast function.
var 'nullCondition' from select create_complex_query(""," ? is not null and ? <>'NA' and ? <>'' ", "and" , "" , '%{x},%{y}');
var 'sqltypesxy'from select sqltypestotext(code,sql_type,'%{x},%{y}') from  defaultdb.localmetadatatbl;
var 'cast_xy' from select create_complex_query("","cast(? as ??) as ?", "," , "" , '%{x},%{y}','%{sqltypesxy}');--TODO!!!!
drop table if exists inputdata2;
create temp table inputdata2 as
select %{cast_xy} from inputdata where %{nullCondition};

-- Add a new column: "idofset". It is used in order to split dataset in training and test datasets.
drop table if exists defaultDB.localinputtblflat;
create table defaultDB.localinputtblflat as
select %{x},%{y}, kfold.idofset as idofset
from inputdata2  as h, (sklearnkfold splits:%{kfold} select rowid from inputdata2) as kfold
where kfold.rid = h.rowid;

var 'privacy' from select privacychecking(no) from (select count(*) as no from defaultDB.localinputtblflat where idofset==0);

select * from defaultDB.localmetadatatbl;
