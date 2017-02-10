wget --header="Authorization: Basic ZmVkZXJhdGlvbjpmZWRlcmF0aW9u" --header="Content-Type: application/json" --post-data="{\"query\": \"select distinct patient_id as __rid, variable_name as __colname, value as __val from exam_value\"}" \
  0.0.0.0:54321/query
