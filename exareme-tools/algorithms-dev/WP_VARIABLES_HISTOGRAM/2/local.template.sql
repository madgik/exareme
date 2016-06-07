requirevars 'defaultDB' 'prv_output_global_tbl' 'input_local_tbl' 'column1' 'column2' 'nobuckets';

create temporary table inputlocaltbl as
select __rid, __colname, tonumber(__val) as __val
from %{input_local_tbl}
where __val <> 'NA' and __val is not null and __val <> ""; --  and typeof(__val) != "text";



select heatmaphistogram(colname1,val1,minval1,maxval1,buckets1,colname2,val2,distinctvalues2)
from  ( select colname1, val1, %{nobuckets} as buckets1, colname2, val2
        from (select __rid as rid1 ,__colname as colname1, __val as val1 from inputlocaltbl where __colname = '%{column1}'),
             (select __rid as rid2 ,__colname as colname2, __val as val2 from inputlocaltbl where __colname = '%{column2}')
        where rid1 = rid2
      ),
      ( select minvalue as minval1, maxvalue as maxval1 from %{prv_output_global_tbl} ),
      ( select jgroup(__val) as distinctvalues2
        from (select __val from inputlocaltbl where __colname = '%{column2}' group by __val)
      );