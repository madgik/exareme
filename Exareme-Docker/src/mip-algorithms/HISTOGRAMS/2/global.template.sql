requirevars  'defaultDB' 'input_global_tbl' 'y';
attach database '%{defaultDB}' as defaultDB;

-- var 'input_global_tbl'  'defaultDB.partialhistogramresults';

drop table if exists defaultDB.histresult;
create table defaultDB.histresult as
select grouping,id,val, minval,maxval,sum(num) as totalsum
from %{input_global_tbl}
group by grouping,id;

-- Privacy Output Result
var 'minNumberOfData' 10;
drop table if exists defaultDB.privatehistresult;
create table  defaultDB.privatehistresult as
select grouping, id, val, minval, maxval, case when totalsum < %{minNumberOfData} then 0 else cast(totalsum as int) end as totalsum from defaultDB.histresult;

var 'enumerations' from select enumerations from defaultDB.metadatatbl where code =='%{y}';

var 'resulthighchart' from select * from (highchartsbasiccolumn enumerations:%{enumerations} title:Histogram ytitle:Count select * from defaultDB.privatehistresult);

-- var 'resultjson' from select tabletojson(grouping, val, minval, maxval, totalsum, "grouping,val,minval,maxval,count",1)
--                       from defaultDB.privatehistresult order by grouping,id;

var 'resultjson' from select tabletojson(y, xmin, xmax, "y,xmin,xmax",1)
                      from (select jgroup(val) as y , minval as xmin, maxval as xmax
                            from  (select jdict(grouping,totalsum) as val, minval, maxval
                                   from (select case when val is null then grouping else grouping||'_'||val end as grouping,id, minval, maxval,totalsum
                                        from defaultDB.privatehistresult)) group by  minval, maxval order by minval);

select '{"result": ['||'%{resultjson}'||','||'%{resulthighchart}'||']}';
