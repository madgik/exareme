requirevars 'defaultDB' 'input_local_tbl' 'input_local_metadata' 'x' 'y' 'dataset';
-- To input_local_metadata einai ena table ths morfhs:  columnname, type, values (in case of categorical)

------------------------------------------------------------------------------
-------------------------------------------------Input for testing LR in madis
hidden var 'csvfileofinputlocaltbl' 'DATASETS FROM SGA1/uoa_flattable.csv';
hidden var 'input_local_tbl' 'table1';
hidden var 'input_local_metadata'  'metadata_tbl';
hidden var 'defaultDB' defaultDB_LR;
hidden var 'y' 'av45';
hidden var 'x' 'adnicategory*apoe4+subjectage+minimentalstate+gender';
hidden var 'dataset' 'adni';

-- Import dataset
drop table if exists table1;
create table table1 as
select * from (file header:t '%{csvfileofinputlocaltbl}');

-- Import input_local_metadata
drop table if exists metadata_tbl;
create table metadata_tbl ('columnname', 'type', 'sql_type', 'enumerations', 'minValue', 'maxValue');
insert into metadata_tbl select 'av45', 'real', null, null,null, null;
insert into metadata_tbl select 'adnicategory' ,'polynominal', null, 'AD,MCI,CN',null, null;
insert into metadata_tbl select 'apoe4', 'polynominal' , 'int','0,1,2',null, null;
insert into metadata_tbl select 'subjectage' ,'real', null, null, 0, 130;
insert into metadata_tbl select 'minimentalstate' ,'integer',null, null, 0, 30;
insert into metadata_tbl select 'gender', 'binominal', null,'M,F', null, null;
insert into metadata_tbl select 'dataset' ,'polynominal', null, 'adni,ppmi,edsd', null,null;
--TODO
-------------------------------------------------------- End input for testing
------------------------------------------------------------------------------
attach database '%{defaultDB}' as defaultDB;

-------
drop table if exists datasets;
create table datasets as
select strsplitv('%{dataset}','delimiter:,') as d;

drop table if exists xvariables;
create table xvariables as
select strsplitv(regexpr("\+|\:|\*|\-",'%{x}',"+") ,'delimiter:+') as xname;

var 'xnames' from select group_concat(xname) as  xname from (select xname from xvariables);

--1. Keep only the correct columns of the table : x,y, dataset
drop table if exists localinputtbl_1;
create table localinputtbl_1 as select %{xnames}, %{y}, dataset from %{input_local_tbl};

--2. Cast values of columns using tonumber function. Keep the rows of the correct dataset.
var 'udf_xnames' from select create_complex_query("","tonumber(?) as ?", "," , "" , '%{xnames}');
drop table if exists localinputtbl_2;
create table localinputtbl_2 as
select %{udf_xnames}, tonumber(%{y}) as %{y} from localinputtbl_1  --TODO . Do not use tonumber but do cast based on the metadata
where dataset in (select * from datasets);

--3.  Delete patients with null values (val is null or val = '' or val = 'NA')
var 'null_vals' from select create_complex_query("","? is null or ?='NA' or ?=''", "or" , "" , '%{xnames}');
delete from localinputtbl_2 where %{null_vals};



--TODO:  CHECK PRIVACY k-AGGREGATION

-----------------------------------------------------------------
-- Create input dataset for LR, that is input_local_tbl_LR_Final
-- A. Dummy code of categorical variables
var 'categoricalcolumns' from select jdictgroup(columnname,enumerations) as metadata from (select distinct columnname, enumerations
      from (select columnname, enumerations, tonumber(val) as val
            from (select columnname, enumerations, strsplitv(enumerations,'dialect:csv') as val
                  from %{input_local_metadata}
                  where (type is 'polynominal' or type is'binominal') and columnname <> 'dataset'))
            where typeof(val) ='text');

drop table if exists input_local_tbl_LR;
create table input_local_tbl_LR as
dummycoding  metadata:%{categoricalcolumns} select * from localinputtbl_2;

-- B. Model Formulae
drop table if exists defaultDB.input_local_tbl_LR_Final;
create table defaultDB.input_local_tbl_LR_Final as
modelFormulae formula:%{x} select * from input_local_tbl_LR;




var 'colnames' from select jmergeregexp(jgroup(colname)) from (select colname from localinputtbl group by colname having count(distinct val)=1); --NEW
drop table if exists defaultDB.deletedcolumns; --NEW
create table defaultDB.deletedcolumns as setschema 'colname'
select distinct colname from defaultDB.input_local_tbl_LR_Final where regexprmatches('%{colnames}' ,colname); --NEW

delete from  defaultDB.input_local_tbl_LR_Final --NEW
where colname in (select * from defaultDB.deletedcolumns); --NEW

insert into defaultDB.input_local_tbl_LR_Final
select rid,colname,val from input_local_tbl_LR where colname = '%{y}';
--
insert into defaultDB.input_local_tbl_LR_Final
select distinct rid as rid,'(Intercept)' as colname, 1.0 as val from input_local_tbl_LR;

--

select colname, FSUM(val) as S1, count(val) as N from defaultDB.input_local_tbl_LR_Final
group by colname;
