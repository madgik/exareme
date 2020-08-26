requirevars 'defaultDB' 'input_local_DB' 'db_query' 'x' 'y' ; -- y = classname
attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

--Read metadata
drop table if exists localmetadatatbl;
create temp table localmetadatatbl as
select code, sql_type , isCategorical as categorical from metadata where code in (select strsplitv('%{x}','delimiter:,')) or code ='%{y}';

--Read dataset and Delete patients with null values (val is null or val = '' or val = 'NA'). Cast values of columns using cast function.
var 'nullCondition' from select create_complex_query(""," ? is not null and ? <>'NA' and ? <>'' ", "and" , "" , '%{x},%{y}');
var 'sqltypesxy'from select sqltypestotext(code,sql_type,'%{x},%{y}') from  localmetadatatbl;
var 'cast_xy' from select create_complex_query("","cast(? as ??) as ?", "," , "" , '%{x},%{y}','%{sqltypesxy}');--TODO!!!!
drop table if exists defaultDB.local_trainingset;
create table defaultDB.local_trainingset as
select %{cast_xy} from (select * from (%{db_query})) where %{nullCondition};

var 'privacy' from select privacychecking(no) from (select count(*) as no from defaultDB.local_trainingset);

select * from localmetadatatbl;
