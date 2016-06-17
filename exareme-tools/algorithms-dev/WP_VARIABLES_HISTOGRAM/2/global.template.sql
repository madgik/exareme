requirevars  'defaultDB' 'input_global_tbl';




create table histresult as
select colname0, id0, minvalue0, maxvalue0, colname1, val, sum(num) as total
from  (select colname0,id0,minvalue0,maxvalue0,colname1,val,num from %{input_global_tbl})
group by colname0, id0, minvalue0, maxvalue0, colname1, val
order by val,id0;


select jdict('code','%{column1}', 'dataType', "DatasetStatistic", 'dataset', mydataset,'label', "Histogram - %{column2}" )
from (
select  jdict('data', mydata,"name", "Count %{column1} values" ) as mydataset
from (  select jdict('categories',mycategories,'header',myheaders,'shape',"vector",'value',myvalues) as mydata
        from ( select jgroup(val) as mycategories,jgroup((maxvalue0+minvalue0)/2.0) as myheaders,jgroup(total) as myvalues from histresult )
     ));




--
--select jgroup(attr,estimate,stderror,tvalue,prvalue) as t1 from coefficients
--
--drop table if exists TotalResults;
--create table TotalResults as
--select jdict('coefficients',t1,'residualsStatistics',t2,'Rsquared_Table',t3,'F_Table',t4)
--from (select jgroup(attr,estimate,stderror,tvalue,prvalue) as t1 from coefficients),

--     (select jgroup(e_min,Q1,e_median,Q3,e_max,residualstandarderror, degreesoffreedom) as t2 from residualsStatistics),
--     (select jgroup(rsquared,adjustedR) as t3 from Rsquared_Table),
--     (select jgroup(fstatistic,degreesoffreedom,noofvariables) as t4 from F_Table);
--
--
--
