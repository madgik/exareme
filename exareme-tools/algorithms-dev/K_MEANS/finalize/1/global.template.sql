requirevars 'defaultDB' 'input_global_tbl' 'columns' 'k';
attach database '%{defaultDB}' as defaultDB;

select jdictgroup('kmeans_result', kmeans_result)
from
(
  select jgroup(cluster_info) as kmeans_result
  from
  (
    select jdict( 'cluster', clid,
                  'cluster_points', clpoints,
                  'cluster_data', grouped_cluster_data) as cluster_info
    from (
      select clid, clpoints, jgroup(cluster_data) as grouped_cluster_data
      from (
        select clid, clpoints, jdict('variable', clcolname, 'value', clval) as cluster_data
        from
        ( select  clusterinfotable.clid1 as clid,
                  clusterinfotable.clpoints as clpoints,
                  clustercenters.clcolname as clcolname,
                  clustercenters.clval as clval
          from
          clustercenters,
          (
            select clid1, count(*) as clpoints
            from %{input_global_tbl}
            group by clid1
          ) as clusterinfotable
        where clustercenters.clid = clusterinfotable.clid1)
      )
    group by clid)
  )
);