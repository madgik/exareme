requirevars 'defaultDB' 'prv_output_global_tbl' 'input_local_tbl' 'column1' 'nobuckets';

create temporary table inputlocaltbl as
select __rid, __colname, tonumber(__val) as __val
from %{input_local_tbl}
where __val <> 'NA' and __val is not null and __val <> ""; -- and typeof(__val) != "text";

select heatmaphistogram(colname1,val1,minval1,maxval1,buckets1)
from  ( select colname1, val1, %{nobuckets} as buckets1
        from (select __rid as rid1 ,__colname as colname1, __val as val1 from inputlocaltbl where __colname = '%{column1}')
      ),
      ( select minvalue as minval1, maxvalue as maxval1 from %{prv_output_global_tbl} );
