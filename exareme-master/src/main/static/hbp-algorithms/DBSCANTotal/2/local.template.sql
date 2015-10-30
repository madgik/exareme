requirevars 'input_local_tbl' 'prv_output_global_tbl' 'column1' 'column2';

--3. Local Query: Compute local heatmaps
--drop table if exists heatmappartial;
--create table heatmappartial as


create temporary table inputlocaltbl as
select * from %{input_local_tbl};

create temporary table prvoutputglobaltbl as
select * from %{prv_output_global_tbl};

select heatmapfinal(colname1,val1, minvalue1, step1,colname2, val2, minvalue2, step2)
from ( select  colname1, val1, minvalue1, step1, colname2, val2, minvalue2, step2
       from( select *
             from  (select __rid as rid1, __colname as colname1, __val as val1 from inputlocaltbl  where __colname = '%{column1}') ,
                   (select __rid as rid2, __colname as colname2, __val as val2 from inputlocaltbl  where __colname = '%{column2}')
             where rid1 = rid2),
           ( select *
             from ( select colname as colnm1, minvalue as minvalue1, step as step1
                    from prvoutputglobaltbl
                    where colname = '%{column1}') ,
                  ( select colname as colnm2, minvalue as minvalue2, step as step2
                    from prvoutputglobaltbl
                    where colname = '%{column2}'))
        where colname1=colnm1 and colname2=colnm2
);


