requirevars 'defaultDB' 'prv_output_global_tbl' ;
attach database '%{defaultDB}' as defaultDB;

--3. Local Query: Compute local heatmaps
--drop table if exists heatmappartial;
--create table heatmappartial as

-- heatmapfinal some val1 are '' -> throws list index out of range exception!!
select heatmapfinal(colname1,val1, minvalue1,step1)
from ( select colname1, val1, minvalue1, step1
       from (select rid as rid1 ,colname as colname1, val as val1 from defaultDB.input_local_tbl_LR_Final ),
            (select colname as colnm1, minvalue as minvalue1,step as step1 from %{prv_output_global_tbl} )
       where colname1=colnm1)
group by colname1;


