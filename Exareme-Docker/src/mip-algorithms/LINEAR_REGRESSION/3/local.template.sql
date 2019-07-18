requirevars 'defaultDB' 'prv_output_global_tbl' 'y';
attach database '%{defaultDB}' as defaultDB;

--var 'prv_output_global_tbl' 'defaultDB.globalcoefficientsandstatistics'; -- statistics & coefficients

--E1. Compute residuals y-ypredictive = Y-sum(X(i)*estimate(i)) (Local Layer)

var 'a' from select tabletojson(attr1,estimate,"attr1,estimate",0) from %{prv_output_global_tbl} where tablename ="coefficients";

drop table if exists defaultDB.residuals;
create table defaultDB.residuals as
select * from (residualscomputation coefficients:%{a} y:%{y} select * from defaultDB.input_local_tbl_LR_Final);

var 'grandmean' from select mean as mean_observed_value from %{prv_output_global_tbl} where tablename ="statistics" and colname = '%{y}';

var 'rows' from setschema 'c1' select count(*) from defaultDB.input_local_tbl_LR_Final ;
var 'cols' from setschema 'c1' select count(*)-1 from (select strsplitv(schema,'delimiter:,')
                                                     from (getschema select * from defaultdb.input_local_tbl_lr_final));

var 'mine' from select min(val) from defaultDB.residuals;
var 'maxe' from select max(val) from defaultDB.residuals;
var 'sume' from select sum(val) from defaultDB.residuals;

var 'sse' from select sum(val*val) from defaultDB.residuals;
var 'sst' from select sum( (%{y}-%{grandmean})*(%{y}-%{grandmean})) from defaultdb.localinputtblflat;

drop table if exists defaultDB.localLRresults;
create table defaultDB.localLRresults (rows int,cols int,mine real, maxe real, sume real, sst real, sse real);
insert into  defaultDB.localLRresults
select  '%{rows}' as rows, '%{cols}' as cols,
        '%{mine}' as mine, '%{maxe}' as maxe,'%{sume}' as sume,
        '%{sst}' as sst,'%{sse}' as sse ;

select * from defaultDB.localLRresults;
