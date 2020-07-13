#config your enviroment 

#Note:Once you modify any of these configuration items, you need to re-run ./compile.sh.

#fill with your node ip and channel_listen_port, example: 0.0.0.0:20200
#if you have more than one node ,you can put them with separator","
#for example: 0.0.0.0:20200,0:0:0:1:20200 
blockchain_address=

#this build tool supports FISCO BCOS 1.3.x and FISCO BCOS 2.0
#if you want to build on FISCO BCOS 1.3.X, please fill with "1",
#if you want to build on FISCO BCOS 2.0, please fill with "2".
#and default value is "2" with FISCO BCOS 2.0.
blockchain_fiscobcos_version=2

#your organization name, used for communication on blockchain.
org_id=

#amop_id
amop_id=

#chain id
chain_id=1

#group id
group_id=1

#Configure your database information
mysql_address=
mysql_database=
mysql_username=
mysql_password=

#This variable is used to distinguish the environment. You can use "dev" to set the development environment, 
#"stg" to set the test environment, "prd" to set the production environment.
#If you do not set it, the system will use allOrg as the environment by default. 
#It is not recommended. Production use default configuration
cns_profile_active=prd
