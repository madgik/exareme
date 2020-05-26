## Tox Tests

1. Deploy Exareme with ENVIRONMENT_TYPE=TEST
2. `pip install --user tox` (if not already installed)
3. `tox` 


## Tests with privacy

1. Deploy Exareme with ENVIRONMENT_TYPE=PROD
2. cd exareme_tests
3. python3 -m pytest -n 5
4. cd ../algorithm_tests_with_privacy
5. python3 -m pytest -n 5