requirevars 'input_global_tbl' ;
select  clid,
        clcolname,
        sum(clS)/sum(clN) as clval
from %{input_global_tbl}
group by clid, clcolname;