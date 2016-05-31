requirevars 'defaultDB' 'input_local_tbl' 'variable' 'covariables' 'groupings';
attach database '%{defaultDB}' as defaultDB;

create temp table localinputtbl1 as
select __rid as rid, __colname as colname, tonumber(__val) as val
from %{input_local_tbl};


drop table if exists localinputtbl;
create table localinputtbl as
select * from localinputtbl1
where val <> 'NA' and val is not null and val <> ""
order by rid, colname,val;


var 'x' from (select (regexpr '[,]' '%{groupings}' '*') || '+' || (regexpr '[,]'  '%{covariables}' '+'));
var 'y' '%{variable}';


drop table if exists xvariables;
create table xvariables as
select strsplitv(regexpr("\+|\:|\*|\-","%{x}","+") ,'delimiter:+') as xname;


--------------------------------------------------------------------------------------------
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
create table defaultDB.input_local_tbl_LR_Final as
select modelFormulae("all",rid,colname,val, "%{x}") from input_local_tbl_LR group by rid;

insert into defaultDB.input_local_tbl_LR_Final
select * from input_local_tbl_LR where colname = "%{y}";

drop table if exists T;
drop table if exists input_local_tbl_LR;
--------------------------------------------------------------------------------------------

select colname,
       min(val) as minvalue,
       max(val) as maxvalue,
       FSUM(val) as S1,
       FSUM(FARITH('*', val, val)) as S2,
       count(val) as N
from ( select *
       from defaultDB.input_local_tbl_LR_Final
       --where (val<>'NA' and val is not null)
     )
group by colname;

