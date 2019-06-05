------------------Input for testing
------------------------------------------------------------------------------
-- hidden var 'defaultDB' defaultDB_TTEST;
-- hidden var 'x' 'subjectage,righthippocampus,lefthippocampus';
-- hidden var 'y' 'gender';
-- var 'ylevels' 'M,F';
-- hidden var 'outputformat' 'pfa';
-- hidden var 'effectsize' 1;
-- hidden var 'ci'  0;
-- hidden var 'meandiff'  0;
-- hidden var 'hypothesis'  0;
-- var 'input_local_DB' 'datasets.db';
--
-- drop table if exists inputdata;
-- create table inputdata as
-- select %{x},%{y}
-- from (file header:t '/home/eleni/Desktop/HBP/exareme/Exareme-Docker/src/mip-algorithms/unit_tests/datasets/CSVs/desd-synthdata.csv');
--
-- --http://www.sthda.com/english/wiki/t-test-formula


------------------ End input for testing
------------------------------------------------------------------------------

requirevars 'defaultDB' 'input_local_DB' 'db_query' 'x' 'y';
--x: a vector of strings naming the variables of interest in data
--testValue: a number specifying the value of the null hypothesis

attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

--Read dataset
drop table if exists inputdata;
create table inputdata as select * from (%{db_query});

-- Cast values of columns using cast function.
var 'cast_x' from select create_complex_query("","tonumber(?) as ?", "," , "" , '%{x}');
drop table if exists defaultDB.localinputtblflat;
create table defaultDB.localinputtblflat as
select %{cast_x}, cast(%{y} as text) as '%{y}' from inputdata
where %{y} is not null and %{y}  <>'NA' and %{y}  <>'';

--Independent or Unpaired T-test
var 'localstats' from select create_complex_query("","insert into  defaultDB.localstatistics
select '?' as colname, %{y} as groupval, sum(?) as S1, sum(?*?) as S2, count(?) as N from localinputtblflat
where ? is not null and ? <>'NA' and ? <>'' group by %{y};" , "" , "" , '%{x}');
drop table if exists defaultDB.localstatistics;
create table defaultDB.localstatistics (colname text, groupval text, S1 real, S2 real, N int);
%{localstats};

drop table if exists defaultDB.privacychecking; -- For error handling
create table defaultDB.privacychecking as
select privacychecking(N) from defaultDB.localstatistics;

select * from defaultDB.localstatistics;

-- --Independent T-tests
-- var 'localstats' from select create_complex_query("","insert into  defaultDB.localstatistics
-- select '?' as colname, %{group_var} as group, sum(?) as S1, sum(?*?) as S2, count(?) as N from localinputtblflat
-- where ? is not null and ? <>'NA' and ? <>'' group by %{group_var};" , "" , "" , '%{localstats}');

--Paired T-test
-- var 'localstats' from select create_complex_query("","insert into  defaultDB.localstatistics
-- select strsplit('?','delimiter:-'), sum(?) as S1, sum(?*?) as S2, count(?) as N from localinputtblflat
-- where ? is not null and ? <>'NA' and ? <>'';" , "" , "" , '%{pairs}');
-- select C1 as colname1, C2 as colname2, sum(C1-C2) as S1, sum((C1-C2)*(C1-C2)) as S2, count(C1) from localinputtblflat, select strsplit('?','delimiter:-')
