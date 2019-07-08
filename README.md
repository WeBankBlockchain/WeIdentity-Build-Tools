WeIdentity JAVA SDK安装部署文档（weid-build-tools方式）
=============================================================

整体介绍
--------

> 通过安装部署工具，您可以快速的在您的应用项目中集成weid-java-sdk。

部署步骤
--------

### 1. 通过 maven 引入 weidentity-java-sdk 依赖

在 `build.gradle` 文件中中添加相关的包依赖：

    dependencies {
        compile 'com.webank:weid-java-sdk:1.3.0'
    }

* * * * *

<div id="section-2">


### 2. 部署 WeIdentity 智能合约
</div>

#### 2.1 下载安装部署工具

``` 
    git clone https://github.com/WeBankFinTech/weid-build-tools.git 
``` 

该工具默认会使用最新版本的
[WeIdentity智能合约](https://github.com/WeBankFinTech/weid-contract)。该工具可以帮您编译智能合约、打包智能合约、发布智能合约和自动配置。

#### 2.2 配置区块链节点和机构信息

    cd weid-build-tools   
    vim run.config   

修改 `blockchain.node.address` 字段，填入区块链节点 IP 和
channelport(需要参考区块链节点的`config.json` 配置文件)，示例如下：

``` {.sourceCode .shell}
blockchain.node.address=127.0.0.1:33034
```

如果需要配置多个区块链节点，用逗号分隔，示例如下：

``` {.sourceCode .shell}
blockchain.node.address=127.0.0.1:33034,10.10.10.11:33034
```

配置完区块链节点信息后，您还需要配置FISCO BCOS版本信息：

如果您使用FISCO BCOS 1.3.x的版本，您需要将配置项配置为1，代表基于FISCO
BCOS 1.x系列的版本进行配置。

如果您使用FISCO BCOS
2.0的版本，您需要将配置项配置为2，代表2.x系列的版本。

``` {.sourceCode .shell}
blockchain_fiscobcos_version=2
```

默认地，我们配置为FISCO BCOS 1.3.x的版本，您可以结合您的实际情况修改。

``` {.sourceCode .shell}
blockchain_fiscobcos_version=1
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

#### 2.3 配置节点证书和秘钥文件

如果您使用的是FISCO BCOS 1.3.x的版本，您可以 请参考[FISCO BCOS 1.3
web3sdk配置](https://fisco-bcos-documentation.readthedocs.io/zh_CN/release-1.3/docs/tools/web3sdk.html)
将证书文件 `ca.crt` 和 `client.keystore` 复制出来，拷贝至
weid-build-tools 下面的 `resources`
目录：`weid-build-tools/resources/`。

如果您使用的是FISCO BCOS 2.0的版本，您可以 请参考[FISCO BCOS 2.0
web3sdk配置](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/sdk/sdk.html)
将证书文件 `ca.crt` `node.crt` 和 `node.key` 复制出来，拷贝至
weid-build-tools 下面的 `resources`
目录：`weid-build-tools/resources/`。

#### 2.4 部署智能合约并自动生成配置文件

如果您是第一次使用本工具，您需要先进行编译：

``` 
    cd ..
    chmod +x compile.sh   
    ./compile.sh
``` 

如果执行过程没报错，大约1分钟左右可以编译完成。

如果您不是发布智能合约的机构，您可以直接跳过后续步骤，直接进入章节3。

编译完成后，您可以执行脚本deploy.sh进行Weidentity智能合约的发布与自动配置。

``` 
    chmod +x deploy.sh   
    ./deploy.sh
``` 

运行成功后，会自动在 `resources` 目录下生成 `fisco.properties`和
`weidentity.properties`。并且自动将 weid-contract
部署到区块链节点上，并将相应的智能合约地址也填入到 `fisco.properties`。
同时，我们还会在weid-build-tools/output/admin目录下动态生成公私钥对。

``` 
    cd output/admin
    ls
``` 

您将看到私钥文件ecdsa_key，以及对应的公钥文件ecdsa_key.pub，并会自动将该私钥对应的地址注册为commit
member，此私钥后续用于注册authority issuer。

<div id="section-3">

### 3 快速使用
</div>

在进行这个章节的操作之前，要确保weidentity的智能合约已经发布完成。

如果您是weidentity智能合约的发布者，您需要保证[章节2](#section-2)的所有步骤已经正确完成。

如果您不是weidentity的智能合约发布者，您需要确保已经获取到weidentity的智能合约地址和chain
id，并正确的配置在weidentity-build-tools的`resources`
目录下的`fisco.properties` 里。 配置方法请参考[附录1](#reference-2)。

此步骤提供快速创建Weidentity DID、注册Authority
issuer、发布CPT、拉取CPT并编译成weidentity-cpt.jar的能力，其中创建Weidentity
DID、注册Authority issuer、发布CPT
等动作也可以通过直接在应用里通过weid-java-sdk完成，您可以结合您的需要进行选择。

#### 3.1 创建您的Weidentiy DID

这个步骤会帮您快速创建一个weidentity DID。

``` 
    cd weid-build-tools/tools
    chmod +x *.sh
    ./create_weid.sh
``` 

执行命令大约需要5秒钟，如果执行完没有报错，会提示“new weidentity did has
been created”，并会打印出刚刚生成的weidentity
did，同时在output目录weid-build-tools/output/create_weid/下生成对应的weidentity
DID 以及公钥和私钥。

在目录下看到一些以0x开头的目录，找到跟刚刚生成的weidentity
DID匹配的目录，里面包含了weidentity
DID文件weId，公钥ecdsa_key.pub和私钥ecdsa_key。

#### 3.2 注册权威机构（authority issuer）

该步骤需要发布智能合约的机构来执行，需要使用[第2.4节](#section-2)中生成的私钥来注册权威机构。
这个步骤会帮您将一个指定的weidentity DID注册为权威机构。
如果您不是智能合约的发布者，您可以将您的weidentity
DID和机构名称发送给智能合约的发布者，以完成权威机构的注册。

假设您要注册的权威机构的weid为did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb，机构名称为test。

``` 
./regist_authority_issuer.sh --weid did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb --org-id test

``` 
执行命令大约需要5秒钟，如果执行没有报错，会提示“authority issuer has
been successfully registed on blockchain”。注册成功。

如果您需要移除某个权威机构，前提是您是智能合约发布者或者您有相应的权限，比如您要移除did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb：

``` 
 ./regist_authority_issuer.sh --remove-issuer did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb

``` 

#### 3.3 机构发布CPT

此步骤会帮助机构发布指定的CPT到区块链上。

假如机构的weid是did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb，需要注册的cpt都以.json后缀命名上传至/home/test/cpt目录下，私钥文件路径为/home/test/private_key/key

    ./regist_cpt.sh --weid did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb --cpt-dir /home/test/cpt --private-key /home/test/private_key/key

如果您的weid是执行[3.1节](#section-3)生成的，您可以不用传入私钥。

    ./regist_cpt.sh --weid did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb --cpt-dir /home/test/cpt

执行命令大约需要10秒钟，假设我们要发布的CPT是ID
card，另假设文件名是cpt_ID_card.json，且已经上传到配置目录下。如果执行没报错，会在屏幕打印命令的执行情况：

``` 
    [RegisterCpt] begin to register cpt file:cpt_ID_card.json
    [RegisterCpt] result:{"errorCode":0,"errorMessage":"success","result":{"cptId":1000,"cptVersion":1}}

``` 

说明CPT文件cpt_ID_card.json成功发布到区块链上，且发布的ID为1000，后续我们可以用这个ID来查询我们发布的CPT。

同时，我们也会将发布CPT的结果以文件的形式记录下来，方便后续查询，您可以在weidentity-build-tools/output/regist_cpt/目录下查看。

#### 3.4 拉取CPT并生成presentation policy模板

此步骤，可以帮使用者从区块链上拉取指定的CPT，并转化成POJO然后生成weidentity-cpt.jar，在创建credential的时候，可以直接使用POJO进行创建。同时也会根据您生成一个presentation
policy模板。

假如您需要将cpt id为1000和1001的cpt从区块链上拉取下来，并基于cpt
1000和cpt 1001生成presentation policy的配置模板。

``` 
    ./cpt_to_pojo.sh --cpt-list 1000,1001
``` 

注：此处的CPT ID是机构已经发布到区块链上的，否则是拉取不成功的。

执行命令大约需要20秒，如果执行没有报错，会在屏幕打印类似于“List:[[100,
101]] are successfully transformed to pojo. List:[[]] are
failed.”的信息，这条信息表明CPT ID为100和101的已经拉取成功。

CPT转成POJO并生成的weidentity-cpt.jar可以到dist目录下获取。

``` 
    cd ../dist/app/
    ls
``` 

直接将weidentity-cpt.jar拷贝至您的应用的classpath下即可使用。

此步骤同时也会帮您生成一个默认的presentation
policy的配置模板，您可以按您的需求来修改。

``` 
    cd ../../output/presentation_policy
    ls
``` 

#### 3.5 注册特定类型机构（specific issuer）

该步骤需要发布智能合约的机构来执行，需要使用[第2.4节](#section-2)中生成的私钥来注册各类特定类型的机构，如学校、医院、政府部门等。
这个步骤会帮您将一个指定的weidentity DID注册为特定类型的某种机构。
如果您不是智能合约的发布者，您可以将您的weidentity
DID和机构名称发送给智能合约的发布者，以完成权威机构的注册。

假设您要注册的机构的weid为did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb，注册类型为college，只需执行此下命令：

``` 
  ./register_specific_issuer.sh --weid did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb --type college
``` 

执行命令大约需要5秒钟，如果执行没有报错，会提示“specific issuer has been
successfully registered on
blockchain”。注册成功。如果类型不存在，此命令也会自动注册一个类型。

如果您需要注册多个机构，请将其DID用分号分割开，如下所示：

``` 
   ./register_specific_issuer.sh --weid did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb;did:weid:0x6efd256d02c1a27675de085b86989fa2ac1baddb --type college
``` 

### 4 完成 weidentity-java-sdk 的集成

``` 
    cd weid-build-tools/resources
    ls
``` 

您可以将resources目录下刚刚生成的`fisco.properties`
文件，`weidentity.properties` 文件，以及 `ca.crt`，`client.keystore`
如果是FISCO BCOS 2.0，则是 `ca.crt` `node.crt` 和 `node.key`
，拷贝至您的应用的 `resources`
目录下，weid-java-sdk会自动加载相应的资源文件。

现在您可以使用 WeIdentity 开发您的区块链身份应用。weidentity-java-sdk
相关接口请见：[WeIdentity JAVA
SDK文档](https://weidentity.readthedocs.io/projects/javasdk/zh_CN/latest/docs/weidentity-java-sdk-doc.html)

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
