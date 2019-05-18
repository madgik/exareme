requirevars 'defaultDB' 'input_global_tbl' ;
attach database '%{defaultDB}' as defaultDB;

var 'input_global_tbl' 'defaultDB.localLRresults';

-- Merge Local Results
hidden var 'rows' from select sum(rows) from %{input_global_tbl};
hidden var 'cols' from select max(cols) from %{input_global_tbl};

hidden var 'sst' from select sum(sst) from %{input_global_tbl};
hidden var 'sse' from select sum(sse) from %{input_global_tbl};

hidden var 'mine' from select min(mine) from %{input_global_tbl};
hidden var 'maxe' from select max(maxe) from %{input_global_tbl};
hidden var 'sume' from select sum(sume) from %{input_global_tbl};

--Compute: dSigmaSq <-- sum((Y-X*bcoefficients)^2)/(rows(X)-(columns(X)-1)) (Global Layer)
hidden var 'dSigmaSq' from select case when %{rows} = %{cols} then 0 else  %{sse}/(%{rows}-%{cols}) end;

--Compute for each estimate: standardError =sqroot(dSigmaSq*val) ,  tvalue = estimate/dSigmaSq , p value <-- 2*pt (-abs(t.value), df = length(data)-1)  (Global Layer)
drop table if exists defaultDB.LRtotalresulttbl;
create table defaultDB.LRtotalresulttbl (tablename text, predictor text, estimate real, stderror real, tvalue real, prvalue real,
                                                      residualsmin real,residualsavg real,residualsmax real,residualstandarderror real, degreesoffreedom int,
                                                      rsquared real, adjustedR real,
                                                      fstatistic real, noofvariables int);

--Coefficients statistics
insert into defaultDB.LRtotalresulttbl
select "Model Coefficients" as tablename, attr as predictor, estimate, stderror,
                               tvalue, 2*t_distribution_cdf(-abs(tvalue), %{rows} -%{cols}) as prvalue,
       null, null, null, null, null, null, null, null,null
from (  select attr, estimate, stderror, estimate/stderror as tvalue
	from (	select coefficients.attr1 as attr,
                       estimate,
                       sqroot(var('dSigmaSq')*val)  as stderror,
                       estimate/sqroot(var('dSigmaSq')*val) as tvalue
		from defaultDB.coefficients, defaultDB.XTXinverted
		where coefficients.attr1 = XTXinverted.attr1 and XTXinverted.attr1 = XTXinverted.attr2));

--Residuals statistics
insert into defaultDB.LRtotalresulttbl
select "Residuals" , null, null, null, null, null,
                    %{mine}, %{sume}/%{cols} as residualsavg, %{maxe},
case when  %{rows} = %{cols} then 0 else sqroot(%{sse}/(%{rows} -%{cols})) end as residualstandarderror,
           %{rows}-%{cols} as degreesoffreedom,
           null, null, null, null;

--Compute R^2 and adjustedR^2 (Global Layer)
var 'rsquared' from select 1- %{sse}/%{sst};
insert into defaultDB.LRtotalresulttbl
select "R squared", null, null, null, null, null,
                    null, null, null, null, null,
                   %{rsquared} as rsquared, 1 - %{sse}*(%{rows}-1) / (%{sst}*(%{rows}-%{cols})) as adjustedR ,
                   null,null;

--Compute F and F statistics (Global Layer)
insert into defaultDB.LRtotalresulttbl
select "F-statistics", null, null, null, null, null,
                       null, null, null, null, %{rows}-%{cols} as degreesoffreedom,
                       null, null,
                       %{rsquared} * (%{rows}-%{cols}) / ((1-%{rsquared})*(%{cols}-1)) as fstatistic,
                       %{cols}-1 as noofvariables;


drop table if exists defaultDB.LRtotalresultvisual;
create table defaultDB.LRtotalresultvisual as
setschema 'result'
select * from (totabulardataresourceformat title:LINEAR_REGRESSION_TABLE types:text,text,real,real,real,real,real,real,real,real,int,real,real,real,int
               select * from defaultDB.LRtotalresulttbl);

select * from defaultDB.LRtotalresultvisual;




--------------------------------------------------------------------------------------------------------
