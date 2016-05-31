requirevars 'input_global_tbl';

var 'categorical' from select case when (select count(distinct val) from %{input_global_tbl})< 20 then "True" else "False" end;
var 'valIsText' from select case when (select typeof(val) from %{input_global_tbl} limit 1) ='text' then "True" else "False" end;

-----

drop table if exists results;
create table results as
select "SummaryStatistics" as type, colname as code, "NA" as categories, "count" as header, Ntotal as gval
from ( select colname, SUM(N) as Ntotal
       from (select * from %{input_global_tbl} where val !='NA'));

insert into results
select "SummaryStatistics" as type, colname as code, "NA" as categories, "min" as header, case when '%{valIsText}'='False' then minval else "0" end as gval
from ( select colname, min(minval) as minval
       from (select * from  %{input_global_tbl} where minval !='NA' ));

insert into results
select "SummaryStatistics" as type, colname as code, "NA" as categories, "max" as header, case when '%{valIsText}'='False' then maxval else "0" end  as gval
from ( select colname, max(maxval) as maxval
       from (select * from  %{input_global_tbl}  where maxval != 'NA'));

insert into results
select "SummaryStatistics" as type, colname as code, "NA" as categories, "average" as header, case when '%{valIsText}'='False' then FARITH('/',S1A,counts) else "0" end as gval
from ( select colname, FSUM(S2) as S2A, FSUM(S1) as S1A, SUM(N) as counts
       from (select * from %{input_global_tbl} where val !='NA'));

insert into results
select "SummaryStatistics" as type, colname as code, "NA" as categories, "std" as header,
       case when '%{valIsText}'='False' then SQROOT( FARITH('/', '-', '*', counts, S2A, '*', S1A, S1A, '*', counts, '-', counts, 1)) else "0" end as gval
from ( select colname, FSUM(S2) as S2A, FSUM(S1) as S1A, SUM(N) as counts
       from (select * from %{input_global_tbl} where val != 'NA'));

insert into results
select "DatasetStatistics1" as type, colname as code, "NA" as categories, val as header, Ntotal as gval
from ( select colname, val, SUM(N) as Ntotal
       from (select * from %{input_global_tbl} where val !='NA' and '%{categorical}'='True' )
       group by val);

insert into results
select "DatasetStatistics1" as type, colname as code, "NA" as categories, "NA" as header, Ntotal as gval
from ( select colname, val, SUM(N) as Ntotal
       from (select * from  %{input_global_tbl} where val =='NA' and '%{categorical}'='True' ) --NULL values
       group by val);

insert into results
select "DatasetStatistics2" as type, colname as code, val as categories, "0" as header, N as gval  -- TODO: anti gia 0 na balw id nosokomeiou (nomizw pws auto thelei) -->des APOE
from (select * from  %{input_global_tbl} where val !='NA' and '%{categorical}'='True' and '%{valIsText}'='False');

insert into results
select "DatasetStatistics2" as type, colname as code, "NA" as categories, "0" as header, N as gval -- TODO: anti gia 0 na balw id nosokomeiou (nomizw pws auto thelei) -->des APOE
from (select * from  %{input_global_tbl} where val =='NA' and '%{categorical}'='True' and '%{valIsText}'='False'); --NULL values

select cast(type as text) as type,
       cast(code as text) as code,
       cast(categories as text) as categories,
       cast(header as text) as header,
       cast(gval as text) as val
from results;
