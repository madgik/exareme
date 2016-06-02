requirevars 'defaultDB' 'input_local_tbl' 'local_pfa';

create temporary table inputlocaltbl as
select * from %{input_local_tbl} where feature_name in ("Hippocampus_L","Hippocampus_R") order by tissue1_volume;

select * from (madtitus 'pfa:%{local_pfa}' select * from inputlocaltbl);
