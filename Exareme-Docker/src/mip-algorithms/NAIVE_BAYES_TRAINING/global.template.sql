requirevars 'defaultDB' 'input_global_tbl' 'alpha' 'dbIdentifier';
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' 'defaultDB.local_counts';
--var 'alpha' 0.1;


var 'classname' from select val from defaultDB.global_inputvariables where  variablename = 'classname';


----------------------------------------------------------------------------------------------------------
-- Merge local_counts
drop table if exists global_counts1;
create table global_counts1 as
select  colname, val, classval, "NA" as S1, "NA" as S2, sum(quantity) as quantity
from %{input_global_tbl}
where colname  in (select colname1 from defaultDB.global_variablesdatatype_Existing where categorical= 'Yes')
group by colname, classval,val;

insert into global_counts1
select  colname, val, classval, sum(S1) as S1, sum(S2) as S2, sum(quantity) as quantity
from %{input_global_tbl}
where colname  in (select colname1 from defaultDB.global_variablesdatatype_Existing where categorical <> 'Yes')
group by colname, classval,val;
--------------------------------------------------------------------------------------------------------
-- For each categorical column: Add 0 for the classval that does not exist. --CHECK!!!!

insert into global_counts1
select c1 as colname, c2 as val, uniqueclassval as classval, "NA" as S1, "NA"as S2, 0 as quantity
from ( select *from( setschema  'c1,c2,uniqueclassval' from select jsplit(c1), uniqueclassval
       from (select distinct c1 from (select jmerge(colname,val) as c1 from global_counts1) )
       cross join
       (select distinct(classval) as uniqueclassval from global_counts1))
	 )
where
colname in (select colname1 from defaultDB.global_variablesdatatype_Existing where categorical= 'Yes');

drop table if exists defaultDB.global_counts;
create table defaultDB.global_counts as
select colname, val, classval, S1, S2, max(quantity) as quantity
from global_counts1
group by colname,classval,val;
drop table if exists global_counts1;

-- For each categorical column: Add Laplace smoothing (https://en.wikipedia.org/wiki/Additive_smoothing)
update  defaultDB.global_counts
set quantity = quantity + %{alpha}
where colname in (select colname1 from defaultDB.global_variablesdatatype_Existing where categorical= 'Yes')
and colname <> '%{classname}'
and  0 in (select min(quantity) from defaultDB.global_counts);

-------------------------------------------------------------------------------------------------
-- For each non categorical column: compute avg and std for each classval
drop table if exists defaultDB.statistics;
create table defaultDB.statistics as
select colname, classval, cast(S1 as float)/cast(quantity as float) as average ,
       SQROOT( FARITH('/', '-', '*', quantity, S2, '*', S1, S1, '*', quantity, '-', quantity, 1))  as sigma
from defaultDB.global_counts
where colname  in (select colname1 from defaultDB.global_variablesdatatype_Existing where categorical <> 'Yes')
group by colname, classval;

-----------------------------------------------------------------------------------------------
-- Compute Naive Bayes ---
--1. Compute class prior probabilities (classname, classval, probability)
drop table if exists defaultDB.global_probabilities;
create table defaultDB.global_probabilities  as
select var('classname') as  colname, val, val as classval, "NA" as average, "NA" as sigma, cast(classquantity as float)/cast(totalquantity as float) as probability
from (select val, sum(quantity) as classquantity from defaultDB.global_counts where colname=var('classname') group by val),
     (select sum(quantity) as totalquantity from defaultDB.global_counts where colname=var('classname'));

--2. Compute likelihoods for categorical variables
insert into defaultDB.global_probabilities
select colname,val, classval, "NA" as average,"NA" as sigma, cast(quantity as float)/cast(classquantity +  %{alpha} * novals  as float) as probability
from defaultDB.global_counts,
    (select val as classval1, sum(quantity) as classquantity from defaultDB.global_counts where colname=var('classname') group by val),
    (select colname as colname1, count( distinct val) novals from defaultdb.global_counts group by colname)
where classval = classval1
and colname =colname1
and colname in (select colname1 from defaultDB.global_variablesdatatype_Existing where categorical = 'Yes')
and colname <> var('classname');

--3. Compute likelihoods for non categorical variables
insert into defaultDB.global_probabilities
select colname,"NA" as val, classval, average, sigma, "NA" as probability
from defaultDB.statistics
where colname in (select colname1 from defaultDB.global_variablesdatatype_Existing where categorical <> 'Yes')
and colname <> var('classname');

--select * from defaultDB.global_probabilities;

select jdict('results', componentresult, 'dbIdentifier', '%{dbIdentifier}') as results
from
(
select tabletojson(colname,val,classval,average,sigma,probability, "colname,val,classval,average,sigma,probability")  as componentresult
from defaultdb.global_probabilities
);
