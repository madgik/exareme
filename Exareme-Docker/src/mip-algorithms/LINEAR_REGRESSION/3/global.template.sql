requirevars 'defaultDB' 'input_global_tbl' ;
attach database '%{defaultDB}' as defaultDB;

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
--Coefficients statistics
drop table if exists LRtotalresulttbl;
create temp table LRtotalresulttbl (predictor text, estimate real, stderror real, tvalue real, prvalue real);

insert into LRtotalresulttbl
select attr as predictor, estimate, stderror, tvalue, 2*t_distribution_cdf(-abs(tvalue), %{rows} -%{cols}) as prvalue
from ( select attr, estimate, stderror, estimate/stderror as tvalue
	     from (	select coefficients.attr1 as attr,
                     estimate,
                     sqroot(var('dSigmaSq')*val)  as stderror,
                    estimate/sqroot(var('dSigmaSq')*val) as tvalue
		          from defaultDB.coefficients, defaultDB.XTXinverted
		          where coefficients.attr1 = XTXinverted.attr1 and XTXinverted.attr1 = XTXinverted.attr2));

var 'tableResultCoefficients' from select * from (totabulardataresourceformat title:COEFFICIENTS types:text,real,real,real,real
                            select predictor, estimate, stderror, tvalue, prvalue from LRtotalresulttbl);

drop table if exists LRtotalresulttbl2;
create temp table LRtotalresulttbl2 (name text, value real);

insert into LRtotalresulttbl2 select "residual_min", %{mine};
insert into LRtotalresulttbl2 select "residual_max", %{maxe};
insert into LRtotalresulttbl2 select "residual_standard_error", case when  %{rows} = %{cols} then 0 else sqroot(%{sse}/(%{rows} -%{cols})) end;
insert into LRtotalresulttbl2 select "degrees_of_freedom",  %{rows}-%{cols};
var 'rsquared' from select 1- %{sse}/%{sst};
insert into LRtotalresulttbl2 select "R-squared", %{rsquared};
insert into LRtotalresulttbl2 select "adjusted-R", 1 - %{sse}*(%{rows}-1) / (%{sst}*(%{rows}-%{cols})) ;
insert into LRtotalresulttbl2 select "f-statistic",  %{rsquared} * (%{rows}-%{cols}) / ((1-%{rsquared})*(%{cols}-1)) ;
insert into LRtotalresulttbl2 select "variables_number", %{cols}-1  ;

var 'tableResultStats' from select * from (totabulardataresourceformat title:STATISTICS types:text,real
                                            select name,value from LRtotalresulttbl2);

var 'resultjson' from select '{ "type": "application/json", "data": ' || val ||'}' from (
select jdict("coefficients", val1,"statistics",val2)  as val from
(select tabletojson( predictor, estimate, stderror, tvalue, prvalue, "predictor,estimate,stderror,tvalue,prvalue",0) as val1 from LRtotalresulttbl),
(select tabletojson( name, value, "name,value",0) as val2 from LRtotalresulttbl2));

select '{"result": [' || '%{resultjson}' || ',' || '%{tableResultCoefficients}' || ',' || '%{tableResultStats}' || ']}';


--------------------------------------------------------------------------------------------------------
