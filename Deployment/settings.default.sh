: ${SHOW_SETTINGS:=false}

#############################################################################
# Global settings
: ${MASTER_IP:=$(wget http://ipinfo.io/ip -qO -)}

: ${MIP_PRIVATE_NETWORK:="mip_local"}
: ${COMPOSE_PROJECT_NAME:="mip"}

: ${PORTAINER_IMAGE:="portainer/portainer"}
: ${PORTAINER_VERSION:=":latest"}
: ${PORTAINER_PORT:="9000"}
: ${PORTAINER_DATA:="${PWD}/portainer"}

#############################################################################
# DATABASES
# Service Parameters
: ${DB_IMAGE:="hbpmip/postgresraw"}
: ${DB_VERSION:=":v1.4"}
# If you want to expose the DB, you have to adapt the values below, as well
# as uncomment the port section in the docker-compose.yml file.
: ${DB_HOST:="db"} # Internal hostname, if NOT exposed, defined in docker-compose.yml
: ${DB_PORT:="5432"} # Internal port, if NOT exposed, default PostgreSQL port
: ${DB_DATA:="${PWD}/postgres"}
: ${DB_DATASETS:="${PWD}/datasets"}
: ${DB_USER_ADMIN:="postgres"}
: ${DB_PASSWORD_ADMIN:="test"}

# Databases Definitions:
#  1. To add a new DB, copy the last 3 lines below and increment the id
#  2. Add the new number in DB_CREATE_LIST
: ${DB_NAME1:="meta"}
: ${DB_USER1:="meta"}
: ${DB_PASSWORD1:="metapwd"}

: ${DB_NAME2:="features"}
: ${DB_USER2:="features"}
: ${DB_PASSWORD2:="featurespwd"}

: ${DB_NAME3:="woken"}
: ${DB_USER3:="woken"}
: ${DB_PASSWORD3:="wokenpwd"}

: ${DB_NAME4:="portal"}
: ${DB_USER4:="portal"}
: ${DB_PASSWORD4:="portalpwd"}

# Database setup tools
: ${DB_CREATE_IMAGE:="hbpmip/create-databases"}
: ${DB_CREATE_VERSION:=":1.0.0"}

# List of databases to create
: ${DB_CREATE_LIST:="1 2 3 4"}

#: ${METADATA_SETUP_IMAGE:="hbpmip/sample-meta-db-setup"}
#: ${METADATA_SETUP_VERSION:=":0.4.0"}
: ${METADATA_SETUP_IMAGE:="hbpmip/mip-cde-meta-db-setup"} # Stable Config
: ${METADATA_SETUP_VERSION:=":1.1.1"} # Stable Config
: ${METADATA_SETUP_DB:=${DB_NAME1}}

: ${SAMPLE_SETUP_IMAGE:="hbpmip/sample-data-db-setup"}
#: ${SAMPLE_SETUP_VERSION:=":0.5.0"}
: ${SAMPLE_SETUP_VERSION:=":0.3.2"} # Stable Config
: ${SAMPLE_SETUP_DB:=${DB_NAME2}}

: ${ADNI_MERGE_SETUP_IMAGE:="registry.gitlab.com/hbpmip_private/adni-merge-db-setup"}
: ${ADNI_MERGE_SETUP_VERSION:=":1.4.2"}
: ${ADNI_MERGE_SETUP_DB:=${DB_NAME2}}

: ${EDSD_SETUP_IMAGE:="registry.gitlab.com/hbpmip_private/edsd-data-db-setup"}
: ${EDSD_SETUP_VERSION:=":1.3.2"}
: ${EDSD_SETUP_DB:=${DB_NAME2}}

: ${PPMI_SETUP_IMAGE:="registry.gitlab.com/hbpmip_private/ppmi-data-db-setup"}
: ${PPMI_SETUP_VERSION:=":1.0.2"}
: ${PPMI_SETUP_DB:=${DB_NAME2}}

: ${WOKEN_SETUP_IMAGE:="hbpmip/woken-db-setup"}
: ${WOKEN_SETUP_VERSION:=":1.0.2"}
: ${WOKEN_SETUP_DB:=${DB_NAME3}}

# List of databases to populate
#: ${DB_SETUP_LIST:="SAMPLE_SETUP"}
: ${DB_SETUP_LIST:="ADNI_MERGE_SETUP EDSD_SETUP PPMI_SETUP"} # Stable Config

#############################################################################
# Federation Services
: ${CONSUL_IMAGE:="progrium/consul"}
: ${CONSUL_VERSION:="latest"}

: ${EXAREME_IMAGE:="hbpmip/exareme"}
: ${EXAREME_VERSION:="v20"}
: ${EXAREME_ROLE:=""} # The default value is set to the federation node role (worker or manager)
: ${EXAREME_KEYSTORE_PORT:="8500"}
: ${EXAREME_KEYSTORE:="exareme-keystore:${EXAREME_KEYSTORE_PORT}"}
: ${EXAREME_MODE:="global"}
: ${EXAREME_WORKERS_WAIT:="0"} # Wait for N workers
: ${EXAREME_LDSM_ENDPOINT:="query"}
: ${EXAREME_LDSM_RESULTS:="all"}
: ${EXAREME_LDSM_DATAKEY:="output"} # query used with output, query-start with data

# Exareme LDSM Settings
: ${LDSM_USERNAME:=${DB_USER2}}
: ${LDSM_PASSWORD:=${DB_PASSWORD2}}
: ${LDSM_HOST:=""} # The default value is set to the federation node
: ${LDSM_PORT:=${DB_PORT}}
: ${LDSM_DB:=${DB_NAME2}}

: ${FEDERATION_NODE:=""} # Invalid default value, this a required argument of start.sh

#############################################################################
# Local Services

#: ${FEATURES_LOCAL_TABLE:="cde_features_a"}
: ${FEATURES_LOCAL_TABLE:="mip_cde_features"} # Stable Config

: ${DB_UI_IMAGE:="hbpmip/postgresraw-ui"}
: ${DB_UI_VERSION:=":v1.5"}
: ${DB_UI_PORT:="31555"} # External port, if exposed
: ${DB_UI_FEDERATION_SOURCES:="harmonized_clinical_data"}
: ${DB_UI_LOCAL_SOURCES:="mip_cde_features harmonized_clinical_data"}

# Internal dataset folder
: ${DOCKER_DATASETS_FOLDER:="/root/exareme/datasets"}

# Host dataset folder
: ${LOCAL_DATASETS_FOLDER:="/path/to/dataset/folder"}
