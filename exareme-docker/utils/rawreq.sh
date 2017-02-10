wget --header="Authorization: Basic ZmVkZXJhdGlvbjpmZWRlcmF0aW9u" --header="Content-Type: application/json;charset=UTF-8" \
--post-data="{\"query\": \"select distinct patient_id as __rid, variable_name as __colname, value as __val from exam_value\"}" \
raw-ui0:5555/query-start
