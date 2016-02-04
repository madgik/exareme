-- Compute statistics only to columns containing real values (not categorical/text values)

requirevars 'input_local_tbl' 'columns';

drop table if exists columnstable;
create table columnstable as
select strsplitv('%{columns}' ,'delimiter:+') as col;

drop table if exists inputlocaltbl;
create table inputlocaltbl as
select __rid as rid, __colname as colname, __val  as val
from %{input_local_tbl}
where colname in (select col from columnstable) and
      typeof(val)='real';


select colname,
       min(val) as minval,
       max(val) as maxval,
       FSUM(val) as S1,
       FSUM(FARITH('*', val, val)) as S2,
       count(val) as N
from ( select *
       from inputlocaltbl
       where (val<>'NA' and val is not null)
     )
group by colname;

