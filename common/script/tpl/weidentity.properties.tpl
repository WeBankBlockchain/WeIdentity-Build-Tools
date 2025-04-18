#######################################################################################################
#                                                                                                     #
#         application config                                                                          #
#                                                                                                     #
#######################################################################################################
# Deploy WeIdentity with blockchain or only database
deploy.style=${DEPLOY_STYLE}
# Crypto type, only use for deploy with database, 0 for ECDSA, 1 for SM
crypto.type=${CRYPTO_TYPE}

# The organization ID for AMOP communication.
blockchain.orgid=${ORG_ID}

# The Blockchain Type
blockchain.type=${CHAIN_TYPE}

# The Chain Id
chain.id=${CHAIN_ID}

# AMOP Config
# Timeout for amop request, default: 5000ms
amop.request.timeout=5000

amop.id=${AMOP_ID}

# Blockchain node info.
nodes=${BLOCKCHIAN_NODE_INFO}


#######################################################################################################
#                                                                                                     #
#         persistence config                                                                          #
#                                                                                                     #
#######################################################################################################
# Persistence Layer configurations. Do NOT change this if you are not using Persistence Layer features!
#Support the persistence of mysql and redis. You can choose the type of persistence.
persistence_type=${PERSISTENCE_TYPE}

# MySQL connection config
# Support multiple data source configurations with comma-separated multiple data sources.
datasource.name=datasource1

# The configuration of each data source is prefixed by the name of the data source.
datasource1.jdbc.url=jdbc:mysql://${MYSQL_ADDRESS}/${MYSQL_DATABASE}?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&useSSL=false&rewriteBatchedStatements=true&serverTimezone=Asia/Shanghai
datasource1.jdbc.driver=com.mysql.cj.jdbc.Driver
datasource1.jdbc.username=${MYSQL_USERNAME}
datasource1.jdbc.password=${MYSQL_PASSWORD}
datasource1.jdbc.maxActive=50
datasource1.jdbc.initialSize=5
datasource1.jdbc.minIdle=5
datasource1.jdbc.maxIdle=5
datasource1.jdbc.maxWait=10000
datasource1.jdbc.timeBetweenEvictionRunsMillis=600000
datasource1.jdbc.numTestsPerEvictionRun=5
datasource1.jdbc.minEvictableIdleTimeMillis=1800000

# Redis config
# If you want to configure redis in cluster mode, enter multiple node addresses separated by commas.
redis.url=${REDIS_ADDRESS}
redis.password=${REDIS_PASSWORD}
redis_single.database=0
redis_cluster.idle_connection_timeout=10000
redis_cluster.connect_timeout=10000
redis_cluster.timeout=3000
redis_cluster.slave_connection_minimum_idle_size=10
redis_cluster.slave_connection_pool_size=64
redis_cluster.master_connection_minimum_idle_size=10
redis_cluster.master_connection_pool_size=64


#######################################################################################################
#                                                                                                     #
#         persistence domain config                                                                   #
#                                                                                                     #
#######################################################################################################
# Domain configuration, which divides colons into two segments, the first segment is the name of the data source,
# the second segment is the name of the table, and if not, the default is the first data source and the default table `sdk_all_data`,
# Multiple domains can be configured at the same time.
# example:
# domain.credential=datasource1:credential_info
# domain.credential.timeout=86400000
# domain.weIdDocument=datasource1:weid_document_info
# domain.credential.timeout=86400000

# the default domain
domain.defaultInfo=datasource1:default_info
domain.defaultInfo.timeout=31556908799941

# the domain for save encrypt Key
domain.encryptKey=datasource1:encrypt_key_info
domain.encryptKey.timeout=31556908799941

# zkp credential template secret
domain.templateSecret=***REMOVED***
domain.templateSecret.timeout=31556908799941

# zkp credential master secret
domain.masterKey=***REMOVED***

# zkp credential signature
domain.credentialSignature=datasource1:credential_signature

# weid auth info
domain.weIdAuth=datasource1:weid_auth

# the domain for save resource
domain.resourceInfo=datasource1:resource_info
domain.resourceInfo.timeout=31556908799941

# tables for running locally
local.weIdDocument=datasource1:table_weid_document
local.cpt=datasource1:table_cpt
local.policy=datasource1:table_policy
local.presentation=datasource1:table_presentation
local.role=datasource1:table_role
local.authorityIssuer=datasource1:table_authority_issuer
local.specificIssuer=datasource1:table_specific_issuer
local.evidence=datasource1:table_evidence


#######################################################################################################
#                                                                                                     #
#         credential related config                                                                   #
#                                                                                                     #
#######################################################################################################
# You can configure the maximumSize of the default cache module through caffeineCache.maximumSize.xxx.
caffeineCache.maximumSize.SYS_CPT=100

# Salt length for Proof creation.
salt.length=5

# Default length of array value in CPT when creating credential based on ZKP.
zkp.cpt.array.length=5


#######################################################################################################
#                                                                                                     #
#         endpoint Service config                                                                     #
#                                                                                                     #
#######################################################################################################
# Endpoint Service Integration-side parameters
# Listener port required to be opened for RPC Server, default: 6010
endpoint.listener.port=6010


#######################################################################################################
#                                                                                                     #
#         timestamp Service config                                                                    #
#                                                                                                     #
#######################################################################################################
# Timestamp Service Parameters
wesign.accessTokenUrl=
wesign.signTicketUrl=
wesign.timestampUrl=
wesign.appId=
wesign.secret=