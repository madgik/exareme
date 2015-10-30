requirevars 'input_local_tbl';

select __colname as colname,
       FSUM(__val) as S1,
       count(__val) as N
from ( select *
       from %{input_local_tbl}
       where __val<>'NA' and __val is not null
     )
group by __colname;