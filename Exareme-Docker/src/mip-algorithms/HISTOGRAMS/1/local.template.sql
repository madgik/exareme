
------------------Input for testing
------------------------------------------------------------------------------
--Test 1
-- hidden var 'defaultDB' defaultDB_Hist;
-- hidden var 'x' 'lefthippocampus';
-- hidden var 'y' 'gender';
-- var 'bins' 5;
-- var 'input_local_DB' 'datasets.db';
--
-- drop table if exists inputdata;
-- create table inputdata as
-- select %{x},%{y}
-- from (file header:t '/home/eleni/Desktop/HBP/exareme/Exareme-Docker/src/mip-algorithms/unit_tests/datasets/CSVs/desd-synthdata.csv');

-----------------------------------------------------------------------------
-- to x = real,integer,Text ,to y is text

--requirevars 'defaultDB' 'input_local_DB' 'db_query' 'x' 'y' 'dataset' 'bins';
attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

--Read dataset
drop table if exists inputdata;
create table inputdata as select * from (%{db_query});

var 'variables' from select case when '%{y}'='' then '%{x}' else '%{x},%{y}' end;

-- Delete patients with null values (val is null or val = '' or val = 'NA'). Cast values of columns using cast function.
var 'nullCondition' from select create_complex_query(""," ? is not null and ? <>'NA' and ? <>'' ", "and" , "" , '%{variables}');
var 'cast_xnames' from select create_complex_query("","tonumber(?) as ?", "," , "" , '%{variables}');--TODO!!!!
drop table if exists defaultDB.localinputtblflat;
create table defaultDB.localinputtblflat as
select  %{cast_xnames} from inputdata where %{nullCondition};

var 'privacy' from select privacychecking(no) from (select count(*) as no from defaultDB.localinputtblflat);
var 'inputerrorchecking' from select histograms_inputerrorchecking(isCategorical,'%{bins}') from (select isCategorical from metadata where code='%{x}');

drop table if exists defaultDB.partialmetadatatbl;
create table defaultDB.partialmetadatatbl (code text,categorical int, enumerations text,minval real, maxval real, N int);

var 'metadata' from select create_complex_query("","
  insert into  defaultDB.partialmetadatatbl
  select code, 1 as categorical, group_concat(vals) as enumerations , null as minval, null as maxval, null as N  from
  (select distinct code, ? as vals
  from defaultDB.localinputtblflat, (select code from metadata where code = '?' and isCategorical=1)
  where code = '?')
  union
  select code, 0 as categorical, null as enumerations , min(?) as minval, max(?) as maxval, count(?) as N  from
  defaultDB.localinputtblflat, (select code from metadata where code = '?' and isCategorical=0)
  where code = '?';
", "" , "" , '%{variables}');
%{metadata};

delete from defaultDB.partialmetadatatbl where code is null;

select * from defaultDB.partialmetadatatbl;
