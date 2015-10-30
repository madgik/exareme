requirevars 'input_local_tbl' 'column1' 'column2' ;

drop table if exists inputlocaltbl;
create table inputlocaltbl as
select * from %{input_local_tbl}
where __val<>'NA' and __val is not null;


select FSUM(x.__val) as x1,
       FSUM(FARITH('*', x.__val, x.__val)) as x2, --FSUM(x.val*x.val) as x2,
       FSUM(y.__val) as y1,
       FSUM(FARITH('*', x.__val, y.__val)) as xy,--FSUM(x.val*y.val) as xy,
       count(*) as n
from ( (select * from inputlocaltbl where __colname in ('%{column1}')) as x,
       (select * from inputlocaltbl where __colname in ('%{column2}')) as y
       )
where x.__rid =y.__rid;

