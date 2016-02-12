requirevars 'input_global_tbl';

var 'categorical' from
select case when (select count(distinct val) from %{input_global_tbl}) > 2 then "True" else "False" end;

drop table if exists results;
create table results as
select "SummaryStatistics" as type, colname as code, "NA" as categories, "count" as header, Ntotal as gval
from ( select colname, SUM(N) as Ntotal
       from (select * from %{input_global_tbl} where val !='NA')
       );

insert into results
select "SummaryStatistics" as type, colname as code, "NA" as categories, "min" as header, minval as gval
from ( select colname, min(val) as minval
       from (select * from %{input_global_tbl} where val !='NA')
       );

insert into results
select "SummaryStatistics" as type, colname as code, "NA" as categories, "max" as header, maxval as gval
from ( select colname, max(val) as maxval
       from (select * from %{input_global_tbl} where val != 'NA')
       );

insert into results
select "SummaryStatistics" as type, colname as code, "NA" as categories, "average" as header, FARITH('/',S1A,counts) as gval
from ( select colname, FSUM(S2) as S2A, FSUM(S1) as S1A, SUM(N) as counts
       from (select * from %{input_global_tbl} where val !='NA')
       );

insert into results
select "SummaryStatistics" as type, colname as code, "NA" as categories, "std" as header,
       SQROOT( FARITH('/', '-', '*', counts, S2A, '*', S1A, S1A, '*', counts, '-', counts, 1)) as gval
from ( select colname, FSUM(S2) as S2A, FSUM(S1) as S1A, SUM(N) as counts
       from (select * from %{input_global_tbl} where val != 'NA')
       );

insert into results
select "DatasetStatistics2" as type, colname as code, val as categories, "0" as header, N as gval
from (select * from %{input_global_tbl} where val !='NA' and '%{categorical}'='True');

insert into results
select "DatasetStatistics2" as type, colname as code, "NA" as categories, "0" as header, N as gval
from (select * from %{input_global_tbl} where val =='NA' and '%{categorical}'='True'); --NULL values

insert into results
select "DatasetStatistics1" as type, colname as code, "NA" as categories, val as header, Ntotal as gval
from ( select colname, val, SUM(N) as Ntotal
       from (select * from %{input_global_tbl} where val !='NA' and '%{categorical}'='True')
       group by val);

insert into results
select "DatasetStatistics1" as type, colname as code, "NA" as categories, "NA" as header, Ntotal as gval
from ( select colname, val, SUM(N) as Ntotal
       from (select * from %{input_global_tbl} where val =='NA' and '%{categorical}'='True') --NULL values
       group by val);


select cast(type as text) as type,
       cast(code as text) as code,
       cast(categories as text) as categories,
       cast(header as text) as header,
       cast(gval as text) as val
from results;

