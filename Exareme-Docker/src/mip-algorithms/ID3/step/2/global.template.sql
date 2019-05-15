requirevars 'defaultDB';
attach database '%{defaultDB}' as defaultDB;

select * from defaultDB.globaltree;
