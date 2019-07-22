WeIdentity JAVA SDK安装部署文档（weid-build-tools方式）
=============================================================

整体介绍
--------

> 通过安装部署工具，您可以快速的在您的应用项目中集成weid-java-sdk。

部署步骤
--------


<div id="section-1">


### 1. 部署 WeIdentity 智能合约
</div>

#### 1.1 下载安装部署工具

``` 
    git clone https://github.com/WeBankFinTech/weid-build-tools.git 
``` 

该工具默认会使用最新版本的
[WeIdentity智能合约](https://github.com/WeBankFinTech/weid-contract)， 该工具可以帮您发布智能合约和自动配置。

#### 1.2 配置区块链节点和机构信息

weid-java-sdk可以同时支持FISCO BCOS 1.3版本和FISCO BCOS 2.0版本，请根据您的FISCO BCOS版本来选择[1.3版本配置方式](#sub-section-1) 或 [2.0版本配置方式](#sub-section-2) 。

<div id="sub-section-1">

##### 适配FISCO BCOS 1.3版本的配置方式
</div>


+ 基本配置

``` 
    cd weid-build-tools   
    vim run.config   
``` 
修改 `blockchain_address` 字段，填入区块链节点 IP 和channelport，示例如下：   
.. note::
    - channelport的配置可以参考\ `FISCO BCOS 配置项 <https://fisco-bcos-documentation.readthedocs.io/zh_CN/release-1.3/docs/web3sdk/config_web3sdk.html#java>`__ 进行配置。


``` {.sourceCode .shell}
blockchain_address=127.0.0.1:20200
```

如果需要配置多个区块链节点，用逗号分隔，示例如下：

``` {.sourceCode .shell}
blockchain_address=127.0.0.1:20200,10.10.10.11:20200
```

配置FISCO BCOS版本信息：

``` {.sourceCode .shell}
blockchain_fiscobcos_version=2
```

配置完区块链节点相关的信息后，我们还需要配置机构名称，该名称也被用作后续AMOP的通信标识。

假设您的机构名为test，您可以配置为：

``` {.sourceCode .shell}
org_id=test
```

配置chain id，该配置项用于标识您接入的区块链网络。 假设您的chain
id定义为1，则您可以配置为：

``` {.sourceCode .shell}
chain_id=1
```

+ 节点证书和秘钥文件配置

``` {.sourceCode .shell} 
cd resources
    
``` 

请参考[web3sdk客户端配置](https://fisco-bcos-documentation.readthedocs.io/zh_CN/release-1.3/docs/tools/web3sdk.html)
将证书文件 `ca.crt` 和 `client.keystore` 复制出来，拷贝至当前目录下。



<div id="sub-section-2">

##### 适配FISCO BCOS 2.0版本的配置方式
</div>

+ 基本配置

``` 
    cd weid-build-tools   
    vim run.config   
``` 
修改 `blockchain_address` 字段，填入区块链节点 IP 和channelport，示例如下：   
.. note::
    - channelport的配置可以参考\ `FISCO BCOS 配置项 <https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/manual/configuration.html#rpc>`__ 来进行配置。


``` {.sourceCode .shell}
blockchain_address=127.0.0.1:30303
```

如果需要配置多个区块链节点，用逗号分隔，示例如下：

``` {.sourceCode .shell}
blockchain_address=127.0.0.1:30303,10.10.10.11:30303
```

配置FISCO BCOS版本信息：

``` {.sourceCode .shell}
blockchain_fiscobcos_version=2
```

配置完区块链节点相关的信息后，我们还需要配置机构名称，该名称也被用作后续机构间的通信标识。

假设您的机构名为test，您可以配置为：

``` {.sourceCode .shell}
org_id=test
```

配置chain id，该配置项用于标识您接入的区块链网络。 假设您的chain
id定义为1，则您可以配置为：

``` {.sourceCode .shell}
chain_id=1
```

- 节点证书和秘钥文件配置

``` {.sourceCode .shell} 
cd resources
    
``` 

请参考[web3sdk配置](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/sdk/sdk.html)
将证书文件 `ca.crt` `node.crt` 和 `node.key` 复制出来，拷贝至当前目录下。

#### 1.3 部署智能合约并自动生成配置文件

如果您是第一次使用本工具，您需要先进行编译：

``` 
    cd ..
    chmod +x compile.sh   
    ./compile.sh
``` 

如果执行过程没报错，大约半分钟左右可以编译完成。

编译完成后，您可以执行脚本deploy.sh进行Weidentity智能合约的发布与自动配置。

``` 
    chmod +x deploy.sh   
    ./deploy.sh
``` 

运行成功后，在控制台可以看到发布好的智能合约地址。

```

contract is deployed with success.
===========================================.
weid contract address is 0x4ba81103afbd5fc203db14322c3a48cd1abb7770
cpt contract address is 0xb1f3f13f772f3fc04b27ad8c377def5bc0c94200
authority issuer contract address is 0xabb97b3042d0f50b87eef3c49ffc8447560faf76
evidence contract address is 0x8cc0de880394cbde18ca17f6ce2cf7af5c51891e
specificIssuer contract address is 0xca5fe4a67da7e25a24d76d24efbf955c475ab9ca
===========================================.
```

```eval_rst
.. important::

  - 发布weid智能合约的机构将会自动注册为委员会成员（commit member）。
  - 发布weid智能合约会同时会在weid-build-tools/output/admin目录下动态生成私钥文件ecdsa_key，以及对应的公钥文件ecdsa_key.pub
，此私钥后续用于注册权威机构，您可以将起保存到您的其他存储库里。
```


至此，您已经完成weid-java-sdk的安装部署，您可以开始您的应用集成。


### 2  weid-java-sdk 的集成

#### 2.1 JAVA应用工程中引入weid-java-sdk

在您的应用工程的gradle文件中配置weid-java-sdk依赖：
```
 
     compile 'com.webank:weid-java-sdk:1.3.1.rc-2'

```  

#### 2.2 配置您的应用工程

将build-tools里配置好的配置文件拷贝至您的应用工程中：

``` 
    cd resources/
    ls
``` 

您可以将resources目录下刚刚生成的`fisco.properties`
文件，`weidentity.properties` 文件，以及 `ca.crt`，`client.keystore`
如果是FISCO BCOS 2.0，则是 `ca.crt` `node.crt` 和 `node.key`
，拷贝至您的应用的 `resources`
目录下，weid-java-sdk会自动加载相应的资源文件。

现在您可以使用 WeIdentity 开发您的区块链身份应用。weid-java-sdk
相关接口请见：[WeIdentity JAVA
SDK文档](https://weidentity.readthedocs.io/projects/javasdk/zh_CN/latest/docs/weidentity-java-sdk-doc.html)。

[开发样例](https://github.com/WeBankFinTech/weid-sample/tree/develop)

我们也提供了一些快捷工具，可以帮您快速使用weid-java-sdk，请参考[章节3](#section-4).

<div id="section-4">

### 3 快速使用
</div>

在进行这个章节的操作之前，要确保weidentity的智能合约已经发布完成。

```eval_rst
.. note::

    - 只有weid智能合约发布机构可以注册权威机构，才能进行3.2节和3.3节的相关操作。
```

此步骤提供快速创建Weidentity DID、注册Authority
issuer、发布CPT、拉取CPT并生成presentation policy的能力。

#### 3.1 创建您的weid

这个步骤会帮您快速创建一个weid。

``` 
    cd ../tools
    chmod +x *.sh
    ./create_weid.sh
``` 

若执行成功，则会打印以下信息，表明创建的weid是did:weid:1:0x405a7ae297fc6d6fb02fb548db64b29f08114ca1。

``` 
    new weid has been created ----> did:weid:1:0x405a7ae297fc6d6fb02fb548db64b29f08114ca1
    the related private key and public key can be found at /home/app/tonychen/test_gradle/weid-build-tools/output/create_weid/0x405a7ae297fc6d6fb02fb548db64b29f08114ca1.
``` 


在weid-build-tools/output/create_weid/目录下看到一些以0x开头的目录，找到跟刚刚生成的weid匹配的目录，里面包含了weid，公钥ecdsa_key.pub和私钥ecdsa_key。

#### 3.2 注册权威机构（authority issuer）

.. note::
    - 只有委员会成员（commit member）可以进行本节操作，若您不是委员会成员，您可以将您的weid和机构id发给委员会成员，让其帮您注册成权威机构。

假设您要注册的权威机构的weid为did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb，机构名称为test。

``` 
    ./register_authority_issuer.sh --org-id test --weid did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb 

``` 
执行成功，则会打印以下信息：
``` 

    registering authorityissuer:did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb, name is :test
    success.

``` 

如果您需要移除某个权威机构，前提是您是智能合约发布者或者您有相应的权限，比如您要移除did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb

``` 

    ./register_authority_issuer.sh --remove-issuer did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb

``` 

执行成功，则会打印以下信息：
``` 

    removing authority issuer :did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb...
    success.

``` 

#### 3.3 注册特定类型机构（specific issuer）

.. note::
    - 只有委员会成员（commit member）可以进行本节操作，若您不是委员会成员，您可以将您的weid和机构id发给委员会成员，让其帮您注册成权威机构。

假设您要注册的机构的weid为did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb，注册类型为college，只需执行此下命令：

``` 
  ./register_specific_issuer.sh --type college --weid did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb
``` 
执行成功，则会打印以下信息。
```

    [RegisterIssuer] Adding WeIdentity DID did:weid:1:0xe10e52f6b7c6751bd03afc023b8e617d7fd0429c in type: college
    specific issuers and types have been successfully registered on blockchain.
```
如果您需要注册多个机构，请将其DID用分号分割开，如下所示：

``` 
   ./register_specific_issuer.sh --type college --weid did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb;did:weid:0x6efd256d02c1a27675de085b86989fa2ac1baddb
``` 

#### 3.4 机构发布CPT

此步骤会帮助机构发布指定的CPT到区块链上。

如果您的weid是执行[3.1节](#section-3)生成的，您可以不用传入私钥。

``` 
    ./register_cpt.sh --cpt-dir test_data/single/ --weid did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb
``` 

若执行成功，则会打印以下信息：
```

    [RegisterCpt] register cpt file:JsonSchema.json result ---> success. cpt id ---> 1000
    [RegisterCpt] register cpt file:JsonSchema.json with success.
    finished.
```

如果您是通过其他途径创建的weid，您需要自己指定私钥。
假如机构的weid是did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb，需要注册的cpt都以.json后缀命名假设上传至test_data/single/目录下，私钥文件路径为/home/test/private_key/ecdsa_key

``` 
    ./register_cpt.sh --cpt-dir test_data/single/ --weid did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb --private-key /home/test/private_key/ecdsa_key
``` 


#### 3.5 拉取CPT并生成presentation policy模板

.. note::
    - 此步骤，可以帮使用者从区块链上拉取指定的已发布的CPT，并转化成POJO，同时也会根据您生成一个presentation policy模板。

假如您需要将cpt id为1000的cpt从区块链上拉取下来，并基于cpt 1000生成presentation policy的配置模板。

``` 
    ./cpt_to_pojo.sh --cpt-list 1000
``` 

若执行成功，则会打印以下信息。
``` 
 
    begin to generate pojo from cpt...
    All cpt:[1000] are successfully transformed to pojo.

    the weidentity-cpt.jar can be found in /home/app/tonychen/test_gradle/weid-build-tools/dist/app/
    begin to generate presentation policy ...
    presentation policy template is successfully generated, you can find it at /home/app/tonychen/test_gradle/weid-build-tools/output/presentation_policy.
``` 



* * * * *

<div id="reference-2">

### 附录1 手工配置fisco.properties

</div>

前提是您已经完成[章节2](#section-2)的步骤。

编辑fisco.properties：

``` 
    cd weid-build-tools/resources/
    vim fisco.properties
``` 

您可以看到配置内容，我们需要将weidentity的智能合约地址和chain id写入到指定配置项，找到以下配置项：

您需要将每个配置项替换成对应的智能合约地址，比如，如果weid
Contract的发布地址是0xabbc75543648af0861b14daa4f8582f28cd95f5e，
您需要将“weId.contractaddress”对应的0x0替换成0xabbc75543648af0861b14daa4f8582f28cd95f5e，变成以下内容：

``` 

    weid.contractaddress=0xabbc75543648af0861b14daa4f8582f28cd95f5e
    cpt.contractaddress=0x0
    issuer.contractaddress=0x0
    evidence.contractaddress=0x0
    specificissuer.contractaddress=0x0
``` 

其他的智能合约地址的配置依次类推，直到所有的配置项都配置完成。

配置完智能合约地址后，您还需要将chain id也配置到指定项：
假设您需要配置的chain id的值为1，则进行如下配置。

``` 
    chain.id=1
``` 

### 附录2 升级 weid-java-sdk

如果在后续weid java
sdk出了新的版本，您希望将您现有的版本升级为新版本，或者回退到以前的版本，您可以手工将您的build.gradle里配置的版本改为您想要的版本，然后重新执行以上的步骤即可。默认的，您依赖的特定版本的weid-java-sdk会依赖对应的版本的weidentity智能合约，如果您要定制您的智能合约版本，您可以手工替换智能合约的jar包。
