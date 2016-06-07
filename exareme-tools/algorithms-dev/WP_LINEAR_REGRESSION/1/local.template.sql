requirevars 'defaultDB' 'input_local_tbl' 'variable' 'covariables' 'groupings';
attach database '%{defaultDB}' as defaultDB;

--hidden var 'y' 'AV45';
--hidden var 'x' 'DX_bl*APOE4+AGE+PTEDUCAT+PTGENDER';


--hidden var 'grouping' 'DX_bl,APOE4';
--hidden var 'covariables' 'AGE,PTEDUCAT,PTGENDER';

var 'y' from (select '%{variable}');

var 'x' from
(select group_concat(x,'+')
from (
select group_concat(x1,'+') as x from (select strsplitv('%{covariables}','delimiter:,') as x1)
union
select group_concat(x2,'*') as x from (select strsplitv('%{groupings}','delimiter:,') as x2)));

drop table if exists xvariables;
create table xvariables as
select strsplitv(regexpr("\+|\:|\*|\-","%{x}","+") ,'delimiter:+') as xname;


create temp table localinputtbl1 as
select __rid as rid, __colname as colname, tonumber(__val) as val
from %{input_local_tbl}
--where __colname in (select xname from xvariables) or colname = "%{y}"
order by rid, colname, val;



drop table if exists localinputtbl;
create table localinputtbl as
select rid, colname, val
from localinputtbl1
where rid not in (select distinct rid from localinputtbl1 where val="")
--where val != ''
order by rid, colname, val;



--------------------------------------------------------------------------------------------
-- Create input dataset for LR, that is input_local_tbl_LR_Final

drop table if exists input_local_tbl_LR;
create table input_local_tbl_LR as
select * from localinputtbl;
--where colname in (select xname from xvariables) or colname = "%{y}";

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
select modelFormulae(rid,colname,val, "%{x}") from input_local_tbl_LR group by rid;

insert into defaultDB.input_local_tbl_LR_Final
select * from input_local_tbl_LR where colname = "%{y}";

insert into defaultDB.input_local_tbl_LR_Final
select distinct rid as rid,"intercept" as colname, 1.0 as val from input_local_tbl_LR;

drop table if exists T;
drop table if exists input_local_tbl_LR;
--------------------------------------------------------------------------------------------

--C. Compute gramian (LOCAL LAYER)
--drop table if exists partial_gramian;
--create table partial_gramian as
select gramian(rid,colname, cast (val as real)) from
(select * from defaultDB.input_local_tbl_LR_Final order by rid, colname);
