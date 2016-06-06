requirevars 'defaultDB' 'input_global_tbl' 'showtable';
attach database '%{defaultDB}' as defaultDB;


--E3. Compute rows, columns and SSE (Global Layer) dhladh kanw sum twn sse,myrow apotelesmatwn twn local layer (Global Layer)

hidden var 'myrow' from select sum((select varvalue from %{input_global_tbl} where varname="partial_myrow")) ;
hidden var 'mycol' from select varvalue from %{input_global_tbl} where varname="mycol" ;
hidden var 'sst' from select sum((select varvalue from %{input_global_tbl} where varname="partial_sst")) ;

hidden var 'sse' from select sum(e*e) from defaultDB.residuals ;

--E4. dSigmaSq <-- sum((Y-X*bcoefficients)^2)/(rows(X)-(columns(X)-1)) (Global Layer)
hidden var 'dSigmaSq' from select var('sse')/ (var('myrow')-var('mycol')) ;
select var('sse');
select var('myrow');
select var('mycol');
select var('dSigmaSq');


--E5. Compute standardError =sqroot(dSigmaSq*val) ,  tvalue = estimate/dSigmaSq , p value <-- 2*pt (-abs(t.value), df = length(data)-1)  (Global Layer)
drop table if exists coefficients2;
create table coefficients2 as
select attr, estimate, stderror, tvalue, 2*t_distribution_cdf(-abs(tvalue), var('myrow') - var('mycol')) as prvalue
from (  select attr, estimate, stderror, estimate/stderror as tvalue
	from (	select coefficients.attr1 as attr,
                       estimate,
                       sqroot(var('dSigmaSq')*val) as stderror,
                       estimate/sqroot(var('dSigmaSq')*val) as tvalue
		from defaultDB.coefficients, defaultDB.XTXinverted
		where coefficients.attr1 = XTXinverted.attr1 and XTXinverted.attr1 = XTXinverted.attr2));

drop table if exists defaultDB.coefficients;
alter table coefficients2 rename to coefficients;


-----------------------------------------------------------------------------------------------------------------
--F. Residuals min.Q1,median,Q3,max,stderror degreesoffreedom (GLOBAL LAYER)
drop table if exists residualsStatistics;
create table residualsStatistics as
select min(e) e_min, Q1, e_median, Q3, max(e) e_max,
       sqroot(sum(e*e)/(var('myrow')- var('mycol'))) as residualstandarderror,
       var('myrow')-var('mycol') as degreesoffreedom
from
defaultDB.residuals,
(select median(e) as e_median from defaultDB.residuals),
(select median(e) as Q1 from defaultDB.residuals, (select median(e) as e_median from residuals) where e < e_median),
(select median(e) as Q3 from defaultDB.residuals, (select median(e) as e_median from residuals) where e > e_median);

-------------------------------------------------------------------------------------
--G3. Compute R^2 and adjustedR^2 (Global Layer)
drop table if exists Rsquared_Table;
create table Rsquared_Table as
select 1-var('sse')/var('sst') as rsquared, 1 - var('sse')*(var('myrow')-1) / (var('sst')*(var('myrow')-var('mycol'))) as adjustedR ;

--G4. Compute F and F statistics (Global Layer)
drop table if exists F_Table; -- I am not sure....
create table F_Table as
select rsquared * (var('myrow')-var('mycol')) / ((1-rsquared)*(var('mycol')-1)) as fstatistic,
       var('myrow')-var('mycol') as degreesoffreedom,
       var('mycol')-1 as noofvariables
from
Rsquared_Table;


drop table if exists TotalResults;
create table TotalResults as
select jdict('coefficients',t1,'residualsStatistics',t2,'Rsquared_Table',t3,'F_Table',t4)
from (select jgroup(attr,estimate,stderror,tvalue,prvalue) as t1 from coefficients),
     (select jgroup(e_min,Q1,e_median,Q3,e_max,residualstandarderror, degreesoffreedom) as t2 from residualsStatistics),
     (select jgroup(rsquared,adjustedR) as t3 from Rsquared_Table),
     (select jgroup(fstatistic,degreesoffreedom,noofvariables) as t4 from F_Table);



select * from %{showtable};


--select * from coefficients;
--select * from residualsStatistics;
--select * from Rsquared_Table;
--select * from F_Table;