requirevars 'defaultDB' 'prv_output_global_tbl' 'column1' 'column2' 'nobuckets';
attach database '%{defaultDB}' as defaultDB;

--var 'prv_output_global_tbl' 'defaultDB.globalResult'; --DELETE

var 'column1IsCategorical' from select distinct column1IsCategorical from %{prv_output_global_tbl};

drop table if exists defaultDB.distinctvaluesofColumn1;
create table defaultDB.distinctvaluesofColumn1 (val) ;
insert into  defaultDB.distinctvaluesofColumn1
select distinct(val) as val from defaultDB.inputlocaltbl where colname = '%{column1}' and %{column1IsCategorical}=1 order by val;

drop table if exists defaultDB.distinctvaluesofColumn2;
create table defaultDB.distinctvaluesofColumn2 (val) ;
insert into  defaultDB.distinctvaluesofColumn2
select distinct(val) as val from defaultDB.inputlocaltbl where colname = '%{column2}' and '%{column2}' <> '' order by val;


drop table if exists defaultDB.localResult; --DELETE
create table defaultDB.localResult as --DELETE
select * from (
select heatmaphistogrampoc(colname1,val1,minval1,maxval1,buckets1,colname2,val2,distinctvalues2)
from  ( select colname1, val1, %{nobuckets} as buckets1, colname2, val2
        from (select rid as rid1 ,colname as colname1, val as val1 from defaultDB.inputlocaltbl where colname = '%{column1}' and '%{column2}' <> ''),
             (select rid as rid2 ,colname as colname2, val as val2 from defaultDB.inputlocaltbl where colname = '%{column2}' and '%{column2}' <> '')
        where rid1 = rid2
		union all
		select colname1, val1, %{nobuckets} as buckets1,null as colname2, null as val2
        from (select rid as rid1 ,colname as colname1, val as val1 from defaultDB.inputlocaltbl where colname = '%{column1}'and '%{column2}' = '')
      ),
      ( select minvalue as minval1, maxvalue as maxval1 from %{prv_output_global_tbl}),
      ( select case when '%{column2}' = '' then  null else jgroup(val) end as distinctvalues2
        from ( select val from defaultDB.inputlocaltbl where colname = '%{column2}' group by val)
	  )
where %{column1IsCategorical}=0
order by id0, id1
)
where %{column1IsCategorical}=0
union all 
 -- 2. case when column1 is text  and there is no column2 
select * from (
select colname1,id0,val1, val1, null,null,null, count(rid1) 
from (
select rid1, id0 ,colname1, val1
from (select rid as rid1 ,colname as colname1, val as val1 from defaultDB.inputlocaltbl where colname = '%{column1}'),
     (select rowid as id0,val as val3 from defaultDB.distinctvaluesofColumn1)
where   val1 = val3 )
group by val1
)
where %{column1IsCategorical}=1 and '%{column2}' = '' 
union all
 -- 2. case when column1 is text  and there is column2 
select * from (
select colname1,id0,val1, val1, colname2,id1,val2, count(rid1) 
from (
select rid1, id0 ,colname1, val1, colname2,id1,val2
from (select rid as rid1 ,colname as colname1, val as val1 from defaultDB.inputlocaltbl where colname = '%{column1}' and '%{column2}' <> ''),
     (select rid as rid2 ,colname as colname2, val as val2 from defaultDB.inputlocaltbl where colname = '%{column2}' and '%{column2}' <> ''),
     (select rowid as id0,val as val3 from defaultDB.distinctvaluesofColumn1), 
     (select rowid as id1,val as val4 from defaultDB.distinctvaluesofColumn2 where '%{column2}' <> '')
where rid1 = rid2 and val1 = val3 and  val2 = val4 )
group by val1,val2
)
where %{column1IsCategorical}=1 and '%{column2}' <> '';

--Check privacy due to minimum records in each bucket
var 'minimumrecords' 10;
var 'containsmorethantheminimumrecords' from 
select * from (
select case when count(*)>0 then 0 else 1 end as containsmorethantheminimumrecords from defaultDB.localResult where num<%{minimumrecords})
where %{column1IsCategorical}=0
union all 
select * from (
select case when count(*)>0 then 0 else 1 end as containsmorethantheminimumrecords from defaultDB.localResult where num<%{minimumrecords})
where %{column1IsCategorical}=1;
 
--varminimumrec '%{containsmorethantheminimumrecords}' --prepei 1 

select * from defaultDB.localResult; 

