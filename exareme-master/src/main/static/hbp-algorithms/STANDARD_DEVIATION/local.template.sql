requirevars 'input_local_tbl' 'column1';
select __colname as colname,
       FSUM(__val) as S1,
       FSUM(FARITH('*', __val, __val)) as S2,
       count(__val) as N
from ( select *
       from %{input_local_tbl}
       where (__val<>'NA' and __val is not null and __colname = '%{column1}' and '%{column1}' <>'all') or ( '%{column1}' ='all' )
     )
group by __colname;
