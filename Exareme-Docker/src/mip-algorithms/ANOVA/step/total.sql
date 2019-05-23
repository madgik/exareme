
var 'formula' from select formula from defaultDB.localAnovatbl where no in ( select min(no) from defaultDB.localAnovatbl where sst is null);
var 'metadata' from select jgroup(code,enumerations) from defaultdb.metadatatbl;

drop table if exists xvariables;
create table xvariables as
select xname from (select strsplitv(regexpr("\+|\:|\*|\-",'%{formula}',"+") ,'delimiter:+') as xname); --where xname!='intercept' ;

var 'xnames' from select group_concat(xname) as xname from (select distinct xname from xvariables) ;
select '%{xnames}';

var 'derivedcolumnsofmodel' from
select group_concat (modelcolnamesdummycodded) from (
select formulaparts, modelcolnamesdummycodded from (
select strsplitv(regexpr("\+",'%{formula}',"+") ,'delimiter:+') as formulaparts),
(select modelcolnames,group_concat(modelcolnamesdummycodded) as modelcolnamesdummycodded
from (select modelvariables('%{formula}','%{metadata}'))
group by modelcolnames)
where formulaparts = modelcolnames);
select '%{derivedcolumnsofmodel}';

var 'xnames2' from select case when '%{xnames}' <> 'None' then
create_complex_query("createderivedcolumns derivedcolumns:%{derivedcolumnsofmodel},%{y} select ","?", "," , " from defaultDB.localinputtblflat;" , '%{xnames},%{y}')
else
create_complex_query("createderivedcolumns derivedcolumns:%{derivedcolumnsofmodel},%{y} select ","?", "," , " from defaultDB.localinputtblflat;" ,'%{y}')
end;

drop table if exists defaultDB.input_local_tbl_LR_Final;
create table defaultDB.input_local_tbl_LR_Final as
%{xnames2};

--Result: (computation of gramian and statistics):
drop table if exists defaultDB.localgramianandstatistics;
create table defaultDB.localgramianandstatistics (tablename text,attr1 text,attr2 text,val real,reccount real,colname text,S1 real,N real);

insert into defaultDB.localgramianandstatistics
select "gramian" as tablename, attr1,attr2, val, reccount , null, null, null
from (gramianflat select * from defaultDB.input_local_tbl_LR_Final);

insert into defaultDB.localgramianandstatistics
select 'statistics' as tablename, null, null, null, null,colname,  S1,  N
from (statisticsflat select * from defaultDB.input_local_tbl_LR_Final);

select * from defaultDB.localgramianandstatistics;


------------------------------------------------------------------------------


var 'input_global_tbl' 'defaultDB.localgramianandstatistics';

drop table if exists gramian;
create table gramian as
select attr1,attr2, sum(val) as val, sum(reccount) as reccount
from %{input_global_tbl}
where tablename = "gramian"
group by attr1,attr2;

drop table if exists defaultDB.statistics;
create table  defaultDB.statistics as
select  colname, S1A/NA as mean, NA as N
from ( select colname, sum(S1) as S1A,sum(N) as NA
       from %{input_global_tbl}
       where tablename = "statistics"
       group by colname );

--------------------------------------------------------------------------------------------
--D. COMPUTE b estimators (X'X)^-1 * X'y = b  (GLOBAL LAYER)
--D1. Create X'X table
drop table if exists XTX;
create table XTX as
select attr1, attr2, val from gramian where attr1 != "%{y}" and  attr2 != "%{y}" order by attr1,attr2;

--D2. Invert table (X'X)^-1
drop table if exists defaultDB.XTXinverted;
create table defaultDB.XTXinverted as
select invertarray(attr1,attr2,val,sizeofarray)
from ( select attr1,attr2,val, sizeofarray
       from XTX, (select count(distinct attr1) as sizeofarray from XTX ));

--D3. Create X'y table
drop table if exists XTy;
create table XTy as
select attr2 as attr,val from gramian where attr1 = "%{y}" and attr1!=attr2
order by attr;

--D4 COMPUTE b estimators (X'X)^-1 * X'y = b
drop table if exists defaultDB.coefficients;
create table defaultDB.coefficients as
select  attr1, sum(XTXinverted.val*XTy.val) as estimate from
XTXinverted
join
XTy
on attr2 = attr
group by attr1;

drop table if exists defaultDB.globalcoefficientsandstatistics;
create table defaultDB.globalcoefficientsandstatistics (tablename text, attr1 text,estimate real, colname text, mean real);

insert into defaultDB.globalcoefficientsandstatistics
select "coefficients" as tablename, attr1, estimate, null, null
from defaultDB.coefficients;

insert into defaultDB.globalcoefficientsandstatistics
select "statistics" as tablename, null, null, colname, mean
from defaultDB.statistics;

select * from defaultDB.globalcoefficientsandstatistics;

--------------------------------------------------------------------------------------------------------


var 'prv_output_global_tbl' 'defaultDB.globalcoefficientsandstatistics'; -- statistics & coefficients

--E1. Compute residuals y-ypredictive = Y-sum(X(i)*estimate(i)) (Local Layer)
var 'a' from select tabletojson(attr1,estimate,"attr1,estimate") from %{prv_output_global_tbl} where tablename ="coefficients";
var 'grandmean' from select mean as mean_observed_value from %{prv_output_global_tbl} where tablename ="statistics" and colname = '%{y}';

drop table if exists defaultDB.residuals;
create table defaultDB.residuals as
residualscomputation coefficients:%{a} y:%{y} select * from input_local_tbl_LR_Final;
hidden var 'partial_sse' from select sum(val*val) from defaultDB.residuals;

hidden var 'partial_sst' from
select sum( (%{y}-%{grandmean})*(%{y}-%{grandmean}))
from defaultdb.localinputtblflat;

drop table if exists localsss;
create table localsss as
select '%{partial_sst}' as sst,'%{partial_sse}' as sse;

select * from localsss;

---------------------------------------------------

var 'input_global_tbl' 'localsss';

hidden var 'sse' from select sum(sse) from  %{input_global_tbl};
hidden var 'sst' from select sum(sst) from  %{input_global_tbl};
hidden var 'ssregs' from select sum(sst)- sum(sse) from  %{input_global_tbl};

update defaultDB.globalAnovatbl
set sst = '%{sst}', sse = '%{sse}', ssregs = '%{ssregs}'
where no in ( select min(no) from defaultDB.globalAnovatbl where sst is null);

select * from defaultDB.globalAnovatbl;

-----------------------------------------------------------------------------------

var 'prv_output_global_tbl' 'defaultDB.globalAnovatbl;';

drop table if exists defaultDB.localAnovatbl;
create table defaultDB.localAnovatbl as select * from %{prv_output_global_tbl};

select "ok";
