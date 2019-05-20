requirevars 'defaultDB' 'input_global_tbl';
attach database '%{defaultDB}' as defaultDB;

--E3. Compute rows, columns and SSE (Global Layer) dhladh kanw sum twn sse,myrow apotelesmatwn twn local layer (Global Layer)

hidden var 'myrow' from select sum(varvalue) from %{input_global_tbl} where varname="partial_myrow" group by varname ;
hidden var 'mycol' from select max(varvalue) from %{input_global_tbl} where varname="mycol" ;
hidden var 'sst' from select sum(varvalue) from %{input_global_tbl} where varname="partial_sst" group by varname;

hidden var 'sse' from select sum_ee from defaultDB.residuals;

--hidden var 'sse' from select sum(e*e) from defaultDB.residuals ;

--E4. dSigmaSq <-- sum((Y-X*bcoefficients)^2)/(rows(X)-(columns(X)-1)) (Global Layer)
--hidden var 'dSigmaSq' from select var('sse')/ (var('myrow')-var('mycol')) ;
--sum((Y-X*bcoefficients)^2)/(rows(X)-(columns(X)-1)) (Global Layer)
hidden var 'dSigmaSq' from select case when (var('myrow')-var('mycol')) = 0 then 0 else  var('sse')/ (var('myrow')-var('mycol')) end ;

--E5. Compute standardError =sqroot(dSigmaSq*val) ,  tvalue = estimate/dSigmaSq , p value <-- 2*pt (-abs(t.value), df = length(data)-1)  (Global Layer)
drop table if exists coefficients2;
create table coefficients2 as
select attr, estimate, stderror, tvalue, 2*t_distribution_cdf(-abs(tvalue), var('myrow') - var('mycol')) as prvalue
from (  select attr, estimate, stderror, estimate/stderror as tvalue
	from (	select coefficients.attr1 as attr,
                       estimate,
                       sqroot(var('dSigmaSq')*val)  as stderror,
                     estimate/sqroot(var('dSigmaSq')*val) as tvalue
		from defaultDB.coefficients, defaultDB.XTXinverted
		where coefficients.attr1 = XTXinverted.attr1 and XTXinverted.attr1 = XTXinverted.attr2));

alter table coefficients2 rename to coefficients;

insert into coefficients
select colname, 'NA','NA','NA','NA' from defaultDB.deletedcolumns;
-----------------------------------------------------------------------------------------------------------------
--F. Residuals min.Q1,median,Q3,max,stderror degreesoffreedom (GLOBAL LAYER)
drop table if exists defaultDB.residualsStatistics;
create table defaultDB.residualsStatistics as
select min_e, sum_e/counte as avge,max_e,
--select min(e) e_min, Q1, e_median, Q3, max(e) e_max,
       --sqroot(sum(e*e)/(var('myrow')- var('mycol'))) as residualstandarderror,
case when var('myrow')- var('mycol') = 0 then 0 else   sqroot(sum_ee/(var('myrow')- var('mycol'))) end as residualstandarderror, --sum(e*e)
       var('myrow')-var('mycol') as degreesoffreedom
from
defaultDB.residuals;--,
--(select median(e) as e_median from defaultDB.residuals),
--(select median(e) as Q1 from defaultDB.residuals, (select median(e) as e_median from residuals) where e < e_median),
--(select median(e) as Q3 from defaultDB.residuals, (select median(e) as e_median from residuals) where e > e_median);

-------------------------------------------------------------------------------------
--G3. Compute R^2 and adjustedR^2 (Global Layer)
drop table if exists defaultDB.Rsquared_Table;
create table defaultDB.Rsquared_Table as
select 1-var('sse')/var('sst') as rsquared, 1 - var('sse')*(var('myrow')-1) / (var('sst')*(var('myrow')-var('mycol'))) as adjustedR ;

--G4. Compute F and F statistics (Global Layer)
drop table if exists F_Table; -- I am not sure....
create table F_Table as
select rsquared * (var('myrow')-var('mycol')) / ((1-rsquared)*(var('mycol')-1)) as fstatistic,
       var('myrow')-var('mycol') as degreesoffreedom,
       var('mycol')-1 as noofvariables
from
defaultDB.Rsquared_Table;

--------------------------------------------------------------------------------------------------------
-- Visualization 
--drop table if exists defaultDB.finalresult;
--create table defaultDB.finalresult as
select linearregressionresultsviewer(attr,estimate,stderror,tvalue,prvalue) as mytable from coefficients;
--select jdict('result', mytable ) from defaultDB.finalresult;

