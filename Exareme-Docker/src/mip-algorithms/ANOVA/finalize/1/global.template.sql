requirevars 'defaultDB' 'type' 'outputformat';
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' 'defaultDB.metadatatbl';
----------

var 'metadata' from select jgroup(code,enumerations) from (select code ,enumerations from defaultDB.metadatatbl);

drop table if exists defaultDB.sumofsquares;
create table defaultDB.sumofsquares as
select sumofsquares(no,formula,sst,ssregs,sse,%{type}) from defaultDB.globalAnovatbl;

var 'a' from select max(no) from defaultDB.sumofsquares;
insert into defaultDB.sumofsquares
select %{a}+1, "residuals", sse
from defaultDB.globalAnovatbl,(select max(no) as maxno from defaultDB.globalAnovatbl)
where no==maxno;

var 'SST' from select max(sst) from defaultDB.globalAnovatbl;
var 'N' from select N from defaultDB.statistics limit 1;
drop table if exists defaultDB.globalresult;
create table defaultDB.globalresult (`model variables` text, `sum of squares` real,`Df` int,`mean square` real, `f` real, `p` real,`eta squared` real, `part eta squared` real, `omega squared` real);

insert into defaultDB.globalresult
select modelvariables, sumofsquares, df, meansquare, f, p, etasquared, partetasquared, omegasquared
                from (select anovastatistics(no, modelvariables, sumofsquares, '%{metadata}',%{N},%{SST} ) from defaultDB.sumofsquares);

update defaultDB.globalresult
set `f`= null,`p`= null,`eta squared`= null,`part eta squared`= null, `omega squared`= null where `model variables` =  'residuals';

drop table if exists defaultDB.ANOVAresult;
create table defaultDB.ANOVAresult as
setschema 'result'
select * from (totabulardataresourceformat title:ANOVA_TABLE types:text,number,number,number,number,number,number,number,number
                select * from defaultDB.globalresult where `model variables` <> 'intercept') where '%{outputformat}'= 'pfa';

select * from defaultDB.ANOVAresult;
