
------------------Input for testing
------------------------------------------------------------------------------
--Test 1
-- drop table if exists inputdata;
create table inputdata as
   select *
   from (file header:t '/home/eleni/Desktop/HBP/exareme/Exareme-Docker/src/mip-algorithms/unit_tests/datasets/CSVs/desd-synthdata.csv');
--
-- var 'x' 'alzheimerbroadcategory' ;
-- var 'y' 'lefthippocampus';
-- var 'defaultDB' 'defaultDB2';
-- var 'input_local_DB' 'datasets.db';
-- var 'referencevalues' '[]';
-- var 'encodingparameter' 'simplecoding';
-- attach 'datasets.db' as 'db';

requirevars 'defaultDB' 'input_local_DB' 'db_query' 'x' 'y' 'referencevalues' 'encodingparameter';
attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

var 'xnames' from
select group_concat(xname) as  xname from
(select distinct xname from (select strsplitv(regexpr("\+|\:|\*|\-",'%{x}',"+") ,'delimiter:+') as xname) where xname!=0);

--Read dataset
drop table if exists inputdata;
create table inputdata as select * from (%{db_query});

-- Delete patients with null values (val is null or val = '' or val = 'NA'). Cast values of columns using cast function.
var 'nullCondition' from select create_complex_query(""," ? is not null and ? <>'NA' and ? <>'' ", "and" , "" , '%{xnames},%{y}');
var 'cast_xnames' from select create_complex_query("","tonumber(?) as ?", "," , "" , '%{xnames}');--TODO!!!!
drop table if exists defaultDB.localinputtblflat;
create table defaultDB.localinputtblflat as
select %{cast_xnames}, tonumber(%{y}) as '%{y}', cast(1.0 as real) as intercept --TODO!!!!
from inputdata where %{nullCondition};

var 'privacy' from
select privacychecking(no) from (select count(*) as no from defaultDB.localinputtblflat);




drop table if exists defaultDB.partialmetadatatbl;
create table defaultDB.partialmetadatatbl (code text,categorical int, enumerations text);
var 'metadata' from select create_complex_query("","  insert into  defaultDB.partialmetadatatbl
                                                      select code,categorical,enumerations from
                                                      (select '?' as code, group_concat(vals) as enumerations
                                                      from (select distinct ? as vals from defaultDB.localinputtblflat where '?' in
                                                          (select code from metadata where code='?' and isCategorical=1))),
                                                      (select code as code1,isCategorical as categorical from metadata) where code=code1
                                                       ;", "" , "" , '%{xnames},%{y}');
%{metadata};


select * from defaultDB.partialmetadatatbl;
