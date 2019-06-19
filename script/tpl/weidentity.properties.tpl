#orgid, used for amop communication
blockchain.orgid=${ORG_ID}

#presistence configuration
#mysql connection config
jdbc.url=${JDBC_URL}?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&useSSL=false
jdbc.username=${JDBC_USERNAME}
jdbc.password=${JDBC_PASSWORD}
jdbc.maxActive=50
jdbc.minIdle=5
jdbc.maxIdle=5
jdbc.maxWait=10000
jdbc.timeBetweenEvictionRunsMillis=600000
jdbc.numTestsPerEvictionRun=5
jdbc.minEvictableIdleTimeMillis=1800000

salt.length=5

#amop config
#timeout for amop request, default:5000ms
amop.request.timeout=5000

# blockchain node info
nodes=${BLOCKCHIAN_NODE_INFO}