
------------------Input for testing
------------------------------------------------------------------------------
--Test 1
-- drop table if exists inputdata;
-- create table inputdata as
--    select ANOVA_var_I1,ANOVA_var_I2,ANOVA_var_I3,ANOVA_var_D
--    from (file header:t '/home/eleni/Desktop/HBP/exareme/Exareme-Docker/src/mip-algorithms/ANOVA/data_ANOVA_Total_with_inter_V1V2.csv'))
--    where dataset = 'ANOVA_Balanced_with_inter_V1V2';

-- hidden var 'defaultDB' defaultDB_ANOVA3;
-- hidden var 'y' 'ANOVA_var_D';
-- hidden var 'x' 'ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3';
 --var 'type' 2;
-- hidden var 'outputformat' 'pfa';

-- Test2
-- drop table if exists inputdata;
-- create table inputdata as
--    select ANOVA_alzheimerbroadcategory,ANOVA_gender,ANOVA_agegroup,ANOVA_lefthippocampus
--    from (file header:t '/home/eleni/Desktop/HBP/exareme/Exareme-Docker/src/mip-algorithms/unit_tests/datasets/CSVs/dataset_0.csv');
--
-- var 'type' 2;
-- var 'x' 'ANOVA_alzheimerbroadcategory*ANOVA_gender+ANOVA_agegroup' ;
-- var 'y' 'ANOVA_lefthippocampus';
-- var 'defaultDB' 'ANOVA_defaultDB';
-- var 'input_local_DB' 'datasets.db';

-- .s inputdata;
-- .s inputmetadata;
------------------ End input for testing
-----------------------------------------------------------------------------
-- to y = real, x = equation of + - * 1 0,type = 1 2 3

requirevars 'defaultDB' 'input_local_DB' 'db_query' 'x' 'y' 'dataset';
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
var 'cast_xnames' from select create_complex_query("","cast(? as text) as ?", "," , "" , '%{xnames}');
drop table if exists defaultDB.localinputtblflat;
create table defaultDB.localinputtblflat as
select %{cast_xnames}, cast(%{y} as real) as '%{y}', cast(1.0 as real) as intercept
from inputdata where %{nullCondition};

drop table if exists defaultDB.partialmetadatatbl;
create table defaultDB.partialmetadatatbl (code text, enumerations text);
var 'metadata' from select create_complex_query(""," insert into  defaultDB.partialmetadatatbl
                                                     select '?' as code, group_concat(vals) as enumerations
                                                     from (select distinct ? as vals from defaultDB.localinputtblflat);", "" , "" , '%{xnames}');
%{metadata};

-- drop table if exists defaultDB.partialmetadatatbl;
-- create table defaultDB.partialmetadatatbl (code text, enumerations text,enumerationsDB text);
-- var 'metadata' from select create_complex_query(""," insert into  defaultDB.partialmetadatatbl
--                                                      select '?' as code, group_concat(vals) as enumerations, enumerationsDB from
--                                                      (select distinct ? as vals from defaultDB.localinputtblflat),
--                                                      (select code as codeDB, enumerations as enumerationsDB from metadata
--                                                      where code in (select distinct xname from (select strsplitv(regexpr('\+|\:|\*|\-','%{x}','+') ,'delimiter:+') as xname) where xname!=0))
--                                                      where codeDB=code ;", "" , "" , '%{xnames}');
-- %{metadata};

select * from defaultDB.partialmetadatatbl;
