requirevars 'iterationsDB';
attach database '%{iterationsDB}' as iterationsDB;

update iterationsDB.iterations_counter_tbl set iterations_counter = 1 + iterations_counter;

select "ok";
