requirevars 'defaultDB' 'input_local_DB' 'db_query' 'x' 'y' 'test_size' 'train_size' 'random_state' 'shuffle'; -- y = classname
attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

select categoricalparameter_inputerrorchecking('shuffle', '%{shuffle}', 'True,False,');

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
var 'inputchecking' from select holdoutvalidation_inputerrorchecking1('%{train_size}','%{test_size}');
var 'random_state2' from select case when '%{random_state}'=='' or '%{random_state}'=='None' then 'None' else cast('%{random_state}' as int) end;
var 'shuffle2'      from select case when '%{shuffle}'==''or '%{shuffle}'=='False'  then 'False' else 'True' end;
var 'train_size2'   from select case when '%{train_size}'=='' then 1.0 -cast('%{test_size}' as float)  else cast('%{train_size}' as float) end;
var 'test_size2'    from select case when '%{test_size}'==''  then 1.0 - cast('%{train_size}' as float) else cast('%{test_size}' as float) end;
var 'inputchecking' from select holdoutvalidation_inputerrorchecking2(%{train_size2},%{test_size2});

drop table if exists defaultDB.localinputtblflat;
create table defaultDB.localinputtblflat as
select %{x},%{y}, case when kfold.idofset=='Train' then 1 else 0 end as idofset
from inputdata2  as h, (traintestsplit test_size:%{test_size2} train_size:%{train_size2} random_state:%{random_state2} shuffle:%{shuffle2}  select rowid from inputdata2) as kfold
where kfold.rid = h.rowid;

var 'privacy' from select privacychecking(no) from (select count(*) as no from defaultDB.localinputtblflat where idofset==0);
var 'privacy' from select privacychecking(no) from (select count(*) as no from defaultDB.localinputtblflat where idofset==1);

select * from defaultDB.localmetadatatbl;
