requirevars 'defaultDB' 'input_local_DB' 'db_query' 'x' 'y' 'dataset';
attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

-- It is used for testing
--drop table if exists mydata;
--create table mydata as select * from (file header:t 'epfl_flattable.csv');
--var 'input_local_tbl' 'mydata';
--var 'y' 'av45';
--var 'x' 'adnicategory*apoe4+subjectage+minimentalstate+gender';
--var 'dataset' 'adni';
-------------------------------

drop table if exists datasets;
create table datasets as
select strsplitv('%{dataset}','delimiter:,') as d;

drop table if exists xvariables;
create table xvariables as
select strsplitv(regexpr("\+|\:|\*|\-",'%{x}',"+") ,'delimiter:+') as xname;

--1. Keep only the correct colnames from a flat table
drop table if exists localinputtbl_1a;
create table localinputtbl_1a as
select rid,colname, val from (toeav %{db_query});

--Check if x is empty
var 'empty' from select case when (select '%{x}')='' then 0 else 1 end;
emptyfield '%{empty}';
------------------
--Check if y is empty
var 'empty' from select case when (select '%{y}')='' then 0 else 1 end;
emptyfield '%{empty}';
------------------
create table columnexist as setschema 'colname' select distinct(colname) as colname2 from localinputtbl_1a;
--Check if x exist in dataset
var 'counts' from select count(distinct(colname)) from columnexist where colname in (select xname from xvariables);
var 'result' from select count(xname) from xvariables;
var 'valExists' from select case when(select %{counts})=%{result} then 1 else 0 end;
vars '%{valExists}';
--Check if y exist in dataset
var 'valExists' from select case when (select exists (select colname from columnexist where colname='%{y}'))=0 then 0 else 1 end;
vars '%{valExists}';
----------

--3.  Delete patients with null values
drop table if exists localinputtbl;
create table localinputtbl as
select rid, colname, val
from localinputtbl_1a
where rid not in (select distinct rid from localinputtbl_1a
                  where val is null or val = '' or val = 'NA')
order by rid, colname, val;

--y value:Real,Float or Integer.
--Some values could be null (type:Text). We want to make sure that if "rid-colname('%{y}')-val" exist in a node, colname type is not "Text". That is why
--we previously Delete patients with null values.
var 'type' from select case when (select distinct(typeof(tonumber(val))) as val from localinputtbl where colname='%{y}')='integer' or  (select distinct(typeof(tonumber(val))) as val from localinputtbl where colname = '%{y}')='real' or (select distinct(typeof(tonumber(val))) as val from localinputtbl where colname='%{y}')='float' then 1 else 0 end;
var 'empty' from select count(colname) from localinputtbl where colname='%{y}';          --is epmpty?...If it is, then 'type' is 0
var 'checkEpmpty' from select case when (select  %{empty})= 0 then 1 else 0 end;
var 'final' from select case when  (%{type}=0 and %{checkEpmpty}=1) or (%{type}=1 and %{checkEpmpty}=0) then 1 else 0 end;
vartypey '%{final}';

----Check if number of patients are more than minimum records----
var 'minimumrecords' 10;
create table emptytable(rid  text primary key, colname, val);
var 'privacycheck' from select case when (select count(distinct(rid)) from localinputtbl) < %{minimumrecords} then 0 else 1 end;
create table localinputtbl2 as setschema 'rid , colname, val'
select * from localinputtbl where %{privacycheck}=1
union
select * from emptytable where %{privacycheck}=0;
drop table if exists localinputtbl;
alter table localinputtbl2 rename to localinputtbl;
-----------------------------------------------------------------

-- Create input dataset for LR, that is input_local_tbl_LR_Final

drop table if exists input_local_tbl_LR;
create table input_local_tbl_LR as
select * from localinputtbl
where colname in (select xname from xvariables) or colname = "%{y}";

-- A. Dummy code of categorical variables
drop table if exists T;
create table T as
select rid, colname||'('||val||')' as colname, 1 as val
from input_local_tbl_LR
where colname in (
select colname from (select colname, typeof(val) as t from localinputtbl group by colname) where t='text');

insert into T
select R.rid,C.colname, 0
from (select distinct rid from T) R,
     (select distinct colname from T) C
where not exists (select rid from T where R.rid = T.rid and C.colname = T.colname);

insert into input_local_tbl_LR
select * from T;

delete from input_local_tbl_LR
where colname in (
select colname from (select colname, typeof(val) as t from localinputtbl group by colname) where t='text');

-- B. Model Formulae
drop table if exists defaultDB.input_local_tbl_LR_Final;
create table defaultDB.input_local_tbl_LR_Final as setschema 'rid , colname, val'
select modelFormulae(rid,colname,val, "%{x}") from input_local_tbl_LR group by rid;

var 'colnames' from select jmergeregexp(jgroup(colname)) from (select colname from localinputtbl group by colname having count(distinct val)=1); --NEW
drop table if exists defaultDB.deletedcolumns; --NEW
create table defaultDB.deletedcolumns as setschema 'colname'
select distinct colname from defaultDB.input_local_tbl_LR_Final where regexprmatches('%{colnames}' ,colname); --NEW

delete from  defaultDB.input_local_tbl_LR_Final --NEW
where colname in (select * from defaultDB.deletedcolumns); --NEW

insert into defaultDB.input_local_tbl_LR_Final
select rid,colname,val from input_local_tbl_LR where colname = '%{y}';
--
insert into defaultDB.input_local_tbl_LR_Final
select distinct rid as rid,'(Intercept)' as colname, 1.0 as val from input_local_tbl_LR;

drop table if exists defaultDB.lala;
create table defaultDB.lala as select colname, FSUM(val) as S1, count(val) as N from defaultDB.input_local_tbl_LR_Final
group by colname;

select colname, FSUM(val) as S1, count(val) as N from defaultDB.input_local_tbl_LR_Final
group by colname;
