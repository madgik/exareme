requirevars  'defaultDB' 'input_global_tbl';
attach database '%{defaultDB}' as defaultDB;


create table histresult as
select colname0, id0, minvalue0, maxvalue0, sum(num) as total
from  %{input_global_tbl}
group by colname0, id0, minvalue0, maxvalue0;


select
  jdict(
    'code','%{column1}',
    'dataType', "DatasetStatistic",
    'dataset', mydataset,
    'label', "Histogram: %{column1}"
    )
from ( select  jdict('data', mydata,"name", "Count %{column1} values" ) as mydataset
       from (  select jdict('header',myheaders,'shape',"vector",'value',myvalues) as mydata
               from ( select jgroup((maxvalue0+minvalue0)/2.0) as myheaders,jgroup(total) as myvalues
                      from histresult
                )
       )
);
