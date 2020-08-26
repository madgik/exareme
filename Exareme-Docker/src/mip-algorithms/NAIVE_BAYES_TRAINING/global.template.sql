requirevars 'defaultDB' 'input_global_tbl' 'y' 'alpha' 'dbIdentifier';
attach database '%{defaultDB}' as defaultDB;

--------------------------------------------------------------------------------------------------------
-- Merge local_counts
drop table if exists global_counts1;
create temp table global_counts1 as
select  colname, val, classval, "NA" as S1, "NA" as S2, sum(quantity) as quantity
from %{input_global_tbl}
where colname  in (select code from defaultDB.globalmetadatatbl where categorical = 1 )
group by colname, classval,val;

insert into global_counts1
select  colname, val, classval, sum(S1) as S1, sum(S2) as S2, sum(quantity) as quantity
from %{input_global_tbl}
where colname  in (select code from defaultDB.globalmetadatatbl where categorical = 0 )
group by colname, classval,val;

--------------------------------------------------------------------------------------------------------
-- For each categorical column: Add 0 for the classval that does not exist. --CHECK!!!!

insert into global_counts1
select c1 as colname, c2 as val, uniqueclassval as classval, "NA" as S1, "NA"as S2, 0 as quantity
from ( select *from( setschema  'c1,c2,uniqueclassval' from select jsplit(c1), uniqueclassval
       from (select distinct c1 from (select jmerge(colname,val) as c1 from global_counts1) )
       cross join
       (select distinct(classval) as uniqueclassval from global_counts1)))
where colname in (select code from defaultDB.globalmetadatatbl  where categorical= 1);

drop table if exists global_counts;
create temp table global_counts as
select colname, val, classval, S1, S2, max(quantity) as quantity
from global_counts1
group by colname,classval,val;
drop table if exists global_counts1;

-- For each categorical column: Add Laplace smoothing (https://en.wikipedia.org/wiki/Additive_smoothing)
update  global_counts
set quantity = quantity + %{alpha}
where colname in (select code from defaultDB.globalmetadatatbl  where categorical= 1 and code <> '%{y}')
and  0 in (select min(quantity) from global_counts);

-------------------------------------------------------------------------------------------------
-- For each non categorical column: compute avg and std for each classval
drop table if exists statistics;
create temp table statistics as
select colname, classval, cast(S1 as float)/cast(quantity as float) as average ,
       SQROOT( FARITH('/', '-', '*', quantity, S2, '*', S1, S1, '*', quantity, '-', quantity, 1))  as sigma
from global_counts
where colname  in (select code from defaultDB.globalmetadatatbl  where categorical= 0)
group by colname, classval;

-----------------------------------------------------------------------------------------------
-- Compute Naive Bayes ---
--1. Compute class prior probabilities (classname, classval, probability)
drop table if exists global_probabilities;
create temp table global_probabilities  as
select '%{y}' as  colname, val, val as classval, "NA" as average, "NA" as sigma, cast(classquantity as float)/cast(totalquantity as float) as probability
from (select val, sum(quantity) as classquantity from global_counts where colname='%{y}' group by val),
     (select sum(quantity) as totalquantity from global_counts where colname='%{y}');

--2. Compute likelihoods for categorical variables
insert into global_probabilities
select colname,val, classval, "NA" as average,"NA" as sigma, cast(quantity as float)/cast(classquantity +  %{alpha} * novals  as float) as probability
from (select colname, val, classval,quantity from global_counts),
     (select val as classval1, sum(quantity) as classquantity from global_counts where colname ='%{y}' group by val),
     (select colname as colname1, count( distinct val) as novals from global_counts group by colname)
where classval = classval1
and colname =colname1
and colname in (select code from defaultDB.globalmetadatatbl  where categorical= 1 and code <> '%{y}');

--3. Compute likelihoods for non categorical variables
insert into global_probabilities
select colname,"NA" as val, classval, average, sigma, "NA" as probability
from statistics
where colname in (select code from defaultDB.globalmetadatatbl  where categorical= 0)
and colname <> '%{y}';

--select * from global_probabilities;
var 'jsonResult' from select '{ "type": "application/json", "data": ' || componentresult || ', "dbIdentifier": ' || '%{dbIdentifier}'   || '}' from
( select tabletojson(colname,val,classval,average,sigma,probability, "colname,val,classval,average,sigma,probability",0)  as componentresult
from global_probabilities);

select '{"result": [' || '%{jsonResult}' || ']}';
