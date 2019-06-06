-- ------------------Input for testing
-- ------------------------------------------------------------------------------
-- hidden var 'defaultDB' defaultDB_TTEST;
-- hidden var 'x' 'righthippocampus,lefthippocampus';
-- hidden var 'outputformat' 'pfa';
-- hidden var 'testvalue' 3.0;
-- hidden var 'effectsize' 1;
-- hidden var 'ci'  0;
-- hidden var 'meandiff'  0;
-- hidden var 'hypothesis'  'different';
--
--
-- drop table if exists inputdata;
-- create table inputdata as
-- select %{x}
-- from (file header:t '/home/eleni/Desktop/HBP/exareme/Exareme-Docker/src/mip-algorithms/unit_tests/datasets/CSVs/desd-synthdata.csv');

--http://www.sthda.com/english/wiki/t-test-formula


------------------ End input for testing
------------------------------------------------------------------------------

requirevars 'defaultDB' 'input_local_DB' 'db_query' 'x' ;

attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

--Read dataset
drop table if exists inputdata;
create table inputdata as select * from (%{db_query});

-- Cast values of columns using cast function.
var 'cast_x' from select create_complex_query("","tonumber(?) as ?", "," , "" , '%{x}');
drop table if exists defaultDB.localinputtblflat;
create table defaultDB.localinputtblflat as
select %{cast_x} from inputdata;

--One Sample T-test
var 'localstats' from select create_complex_query("","insert into  defaultDB.localstatistics
select '?' as colname, sum(?) as S1, sum(?*?) as S2, count(?) as N from localinputtblflat
where ? is not null and ? <>'NA' and ? <>'';" , "" , "" , '%{x}');
drop table if exists defaultDB.localstatistics;
create table defaultDB.localstatistics (colname text, S1 real, S2 real, N int);
%{localstats};

drop table if exists defaultDB.privacychecking; -- For error handling
create table defaultDB.privacychecking as
select privacychecking(N) from defaultDB.localstatistics;

select * from defaultDB.localstatistics;
