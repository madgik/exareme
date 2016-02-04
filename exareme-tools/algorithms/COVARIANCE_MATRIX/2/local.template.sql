requirevars 'input_local_tbl' 'prv_output_global_tbl';

create temporary table eavtbl as
select *
from %{input_local_tbl};

select gramian(hosp.rid,
               hosp.colname,
              (hosp.val - attrib_stats.avgvalue)
              )
from (  select __rid as rid, __colname as colname, __val as val
        from eavtbl
        where __colname not in (  select distinct __colname
                                  from eavtbl
                                  where  __val = 'NA' or __val is  NULL
                                  )
        ) as hosp, %{prv_output_global_tbl} as attrib_stats
where hosp.colname = attrib_stats.colname;

