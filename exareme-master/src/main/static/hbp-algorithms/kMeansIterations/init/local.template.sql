requirevars 'input_local_tbl' 'rid' 'colname' 'val' 'k';

select  hashmodarchdep(%{rid}, %{k}) as clid,
	    %{colname} as clcolname,
        sum(%{val}) as clS,
        count(%{val}) as clN
from (  select *
        from %{input_local_tbl}
        where %{colname} in ('%{column1}', '%{column2}')
     )
group by clid, clcolname;


