requirevars 'defaultDB' 'input_global_tbl' ;
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' 'defaultDB.localLRresults';

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
drop table if exists defaultDB.LRtotalresulttbl;
create table defaultDB.LRtotalresulttbl (predictor text, estimate real, stderror real, tvalue real, prvalue real);

insert into defaultDB.LRtotalresulttbl
select attr as predictor, estimate, stderror, tvalue, 2*t_distribution_cdf(-abs(tvalue), %{rows} -%{cols}) as prvalue
from ( select attr, estimate, stderror, estimate/stderror as tvalue
	     from (	select coefficients.attr1 as attr,
                     estimate,
                     sqroot(var('dSigmaSq')*val)  as stderror,
                    estimate/sqroot(var('dSigmaSq')*val) as tvalue
		          from defaultDB.coefficients, defaultDB.XTXinverted
		          where coefficients.attr1 = XTXinverted.attr1 and XTXinverted.attr1 = XTXinverted.attr2));

var 'tableResultCoefficients' from select * from (totabulardataresourceformat title:Coefficients types:text,real,real,real,real
                            select predictor, estimate, stderror, tvalue, prvalue from defaultDB.LRtotalresulttbl);

drop table if exists defaultDB.LRtotalresulttbl2;
create table defaultDB.LRtotalresulttbl2 (name text, value real);

insert into LRtotalresulttbl2 select `residual min`, %{mine};
insert into LRtotalresulttbl2 select `residual max`, %{maxe};
insert into LRtotalresulttbl2 select `residual standard error`, case when  %{rows} = %{cols} then 0 else sqroot(%{sse}/(%{rows} -%{cols})) end;
insert into LRtotalresulttbl2 select `degrees of freedom`,  %{rows}-%{cols};
var 'rsquared' from select 1- %{sse}/%{sst};
insert into LRtotalresulttbl2 select `R squared`, %{rsquared};
insert into LRtotalresulttbl2 select `adjusted R`, 1 - %{sse}*(%{rows}-1) / (%{sst}*(%{rows}-%{cols})) ;
insert into LRtotalresulttbl2 select `f-statistic`,  %{rsquared} * (%{rows}-%{cols}) / ((1-%{rsquared})*(%{cols}-1)) ;
insert into LRtotalresulttbl2 select `no of variables`, %{cols}-1  ;

var 'tableResultStats' from select * from (totabulardataresourceformat title:Coefficients types:text,real
                              select name,value from defaultDB.LRtotalresulttbl2);

select "{\"result\": ["||'%{tableCoefficients}'||","||'%{tableResultStats}'||"]}";


--------------------------------------------------------------------------------------------------------
