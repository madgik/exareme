requirevars  'defaultDB' 'input_global_tbl';
attach database '%{defaultDB}' as defaultDB;

create temporary table heatmapglobal as
select colname0, id0, minvalue0, maxvalue0, sum(num) as num
from %{input_global_tbl}
group by colname0, id0;


drop table if exists medianapprox;
create table medianapprox as
select approximatedmedian(colname0,id0,minvalue0,maxvalue0,num)
from (select * from heatmapglobal order by id0)
group by colname0;


drop table if exists Q1;
create table Q1 as
select approximatedmedian(colname0,id0,minvalue0,maxvalue0,num)
from (select h.colname0 as colname0,
             h.id0 as id0,
             h.minvalue0 as minvalue0,
             case when h.id0=m.bucket then m.val else h.maxvalue0 end as maxvalue0,
	         case when h.id0=m.bucket then m.numsBeforeMedian else h.num end as num
	  from heatmapglobal as h,
	       medianapprox as m
	  where h.colname0 = m.colname0 and h.id0<=m.bucket
	  order by h.colname0,h.id0
)group by colname0;


drop table if exists Q3;
create table Q3 as
select approximatedmedian(colname0,id0,minvalue0,maxvalue0,num)
from ( select h.colname0 as colname0,
              h.id0 as id0,
              case when h.id0=m.bucket then m.val else h.minvalue0 end as minvalue0,
              h.maxvalue0 as maxvalue0,
	          case when h.id0=m.bucket then m.numsAfterMedian else h.num end as num
	   from heatmapglobal as h,
	        medianapprox as m
	   where h.colname0 = m.colname0 and h.id0>=m.bucket
	   order by h.colname0,h.id0
)group by colname0;


select jdict(
      'colname', m.colname0,
       'min', s.minvalue,
       'max', s.maxvalue,
       'count', s.NA,
       'avg', s.avgvalue,
       'std', s.stdvalue,
       'median', m.val,
       'Q1', Q1.val,
       'Q2', Q3.val
       ) as result
from medianapprox as m,Q1,Q3, defaultDB.globalstats as s
where m.colname0 = Q1.colname0 and m.colname0 = Q3.colname0 and m.colname0 = s.colname;

