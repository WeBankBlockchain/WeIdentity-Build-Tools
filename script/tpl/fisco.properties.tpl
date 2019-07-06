# Fisco-bcos blockchain node related properties

# Common properties
bcos.version=${FISCO_BCOS_VERSION}

weId.contractaddress=${WEID_ADDRESS}
cpt.contractaddress=${CPT_ADDRESS}
issuer.contractaddress=${ISSUER_ADDRESS}
evidence.contractaddress=${EVIDENCE_ADDRESS}
specificissuer.contractaddress=${SPECIFICISSUER_ADDRESS}

chain.id=${CHAIN_ID}
group.id=1

# Blockchain connection params. Do NOT change these unless you are troubleshooting!
web3sdk.timeout=10000
web3sdk.core-pool-size=100
web3sdk.max-pool-size=200
web3sdk.queue-capacity=1000
web3sdk.keep-alive-seconds=60

# Config files locations and params. These should be originated from blockchain nodes.
v1.ca-crt-path=ca.crt
v1.client-crt-password=123456
v1.client-key-store-path=client.keystore
v1.key-store-password=123456
v2.ca-crt-path=./v2/ca.crt
v2.node-crt-path=./v2/node.crt
v2.node-key-path=./v2/node.key
