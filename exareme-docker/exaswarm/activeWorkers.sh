curl -s 83.212.100.72:8500/v1/kv/active_workers?keys | jq -r '.[]' | sed 's/active_workers\///g'
