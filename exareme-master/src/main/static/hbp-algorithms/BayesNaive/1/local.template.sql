requirevars 'input_local_tbl' 'classname';

create temporary table inputlocaltbl as
select __rid as rid, __colname as colname, __val as val
from %{input_local_tbl}
where __val<>'NA' and __val is not null;

select colname, val, classval, count(*) as quantity
from ( select h.rid, h.colname as colname, h.val as val , c.val as classval
       from
         inputlocaltbl  as h
       inner join
         (select rid, val from inputlocaltbl where colname = var('classname')) as c
       on h.rid = c.rid
)
group by colname, val, classval;