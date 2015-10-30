requirevars 'input_local_tbl' 'input_global_tbl' 'rid' 'colname' 'val'  'k' 'column1' 'column2';

select  clid,
       	colname as clcolname,
       	sum(val) as clS,
        count(val) as clN

from ( select *
       from %{input_local_tbl}
       where %{colname} in ('%{column1}', '%{column2}') ) as h,

     ( select %{rid}, clid, min(dist) as mindist
       from (  select %{rid}, clid, sum(valD * valD) as dist
               from (   select %{rid}, %{colname}, clid, %{val}-clval as valD
                        from %{input_local_tbl}
                        join clustercenters
                        where %{colname} = clcolname )
               group by %{rid}, clid  )
        group by %{rid} ) as partial_assignnearestcluster

where    h.rid =partial_assignnearestcluster.rid
group by clid, %{colname};


