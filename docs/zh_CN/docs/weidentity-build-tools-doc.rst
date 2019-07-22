.. role:: raw-html-m2r(raw)
   :format: html

.. _weidentity-build-tools-doc:

WeIdentity JAVA SDK 安装部署工具
============================================================

整体介绍
--------

  通过安装部署工具，您可以快速的在您的应用项目中集成 weid-java-sdk，请参考\ `WeIdentity JAVA SDK安装部署文档 <https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/sdk/sdk.html#sdk>`__。
将证书文件 ``ca.crt``  ``node.crt`` 和 ``node.key`` 复制出来，拷贝至当前目录下。

部署步骤
--------


.. raw:: html

   <div id="section-1">

1. 部署 WeIdentity 智能合约
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^


.. raw:: html

   </div>

1.1 下载安装部署工具
''''''''''''''''''''''''''''''
::

    git clone https://github.com/WeBankFinTech/weid-build-tools.git
 

该工具默认会使用最新版本的
`WeIdentity智能合约 <https://github.com/WeBankFinTech/weidentity-contract>`__\，该工具可以帮您发布智能合约和自动配置。


1.2  配置基本信息
''''''''''''''''''''''''''''''''''''
weid-java-sdk 可以同时支持 FISCO BCOS 1.3版本和 FISCO BCOS 2.0 版本。


1.2.1  基本配置
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

::

    cd weid-build-tools   
    vim run.config   

- 修改 ``blockchain_address`` 字段，填入区块链节点 IP 和 channelport，示例如下：

.. note::
    channelport 的配置可以参考\ `FISCO BCOS 2.0 配置文件 <https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/manual/configuration.html#rpc>`__ 进行配置，FISCO BCOS 1.3可以参考 `FISCO BCOS 1.3 配置文件 <https://fisco-bcos-documentation.readthedocs.io/zh_CN/release-1.3/docs/web3sdk/config_web3sdk.html#java>`__ 进行配置。

.. code:: shell

    blockchain_address=10.10.10.10:20200

- 如果需要配置多个区块链节点，用逗号分隔，示例如下：

.. code:: shell

    blockchain_address=10.10.10.10:20200,10.10.10.11:20200


- 配置 FISCO BCOS 版本信息，如果您使用 FISCO BCOS 2.0 版本则配置为2，FISCO BCOS 1.3 则配置为 1：

.. code:: shell

    blockchain_fiscobcos_version=2


- 配置机构名称，该名称也被用作后续机构间的 \ `AMOP <https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/manual/amop_protocol.html>`__ 通信标识。

假设您的机构名为 test，您可以配置为：

.. code:: shell

    org_id=test

配置 chain id，该配置项用于路由到不同的网络。
假设您的 chain id 定义为1，则您可以配置为：

.. code:: shell

    chain_id=1

保存退出，即完成基本配置。

1.2.2 配置节点证书和秘钥文件
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

::

    cd resources/

FISCO BCOS 2.0请参考\ `2.0 web3sdk客户端配置 <https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/sdk/sdk.html#sdk>`__将证书文件 ``ca.crt`` ， ``node.crt`` 和 ``node.key`` 复制出来，拷贝至当前目录下。

FISCO BCOS 1.3请参考\ `1.3 web3sdk客户端配置 <https://fisco-bcos-documentation.readthedocs.io/zh_CN/release-1.3/docs/tools/web3sdk.html>`__将证书文件 ``ca.crt`` 和 ``client.keystore`` 复制出来，拷贝至当前目录下 。


1.3 部署智能合约并自动生成配置文件
''''''''''''''''''''''''''''''

.. raw:: html

   </div>


- 如果您是第一次使用本工具，您需要先进行编译：

::

    cd ..
    chmod +x compile.sh   
    ./compile.sh

如果执行过程没报错，大约半分钟左右可以编译完成。

- 执行脚本 deploy.sh 进行 WeIdentity 智能合约的发布。

::

    chmod +x deploy.sh   
    ./deploy.sh


运行成功后，会打印以下信息：

::

    contract is deployed with success.
    ===========================================.
    weid contract address is 0x4ba81103afbd5fc203db14322c3a48cd1abb7770
    cpt contract address is 0xb1f3f13f772f3fc04b27ad8c377def5bc0c94200
    authority issuer contract address is 0xabb97b3042d0f50b87eef3c49ffc8447560faf76
    evidence contract address is 0x8cc0de880394cbde18ca17f6ce2cf7af5c51891e
    specificIssuer contract address is 0xca5fe4a67da7e25a24d76d24efbf955c475ab9ca
    ===========================================.


.. note::
  - 发布 WeIdentity 智能合约的机构将会自动注册为委员会机构成员（ Committee Member ）。
  - 发布 WeIdentity 智能合约会同时会在 ``weid-build-tools/output/admin`` 目录下动态生成私钥文件 ``ecdsa_key`` ，以及对应的公钥文件 ``ecdsa_key.pub`` ，此私钥后续用于注册权威机构，您可以将起保存到您的其他存储库里。


至此，您已经完成 weid-java-sdk 的安装部署，您可以开始您的 Java 应用集成以及便捷工具体验。


2 weid-java-sdk 的集成
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

2.1 在自己的Java应用工程中引入weid-java-sdk
'''''''''''''''''''''''''''''''''''''''''''''

在自己的 Java 应用通过 ``build.gradle`` 引入，目前weid-java-sdk暂时只支持gradle方式引入。

::

    compile("com.webank:weid-java-sdk:1.3.1-rc.3")


2.2 配置您的应用工程
''''''''''''''''''''''''''''''''''''
将 weid-build-tools 里配置好的配置文件拷贝至您的应用工程中：
::

    cd resources/
    ls


您可以将 resources 目录下的所有文件拷贝至您的Java应用的 ``resources`` 目录下，weid-java-sdk 会自动加载相应的资源文件。

现在您可以使用 WeIdentity 开发您的区块链身份应用。weid-java-sdk
相关接口请见：\ `WeIdentity JAVA SDK文档 <https://weidentity.readthedocs.io/projects/javasdk/zh_CN/latest/docs/weidentity-java-sdk-doc.html>`__ 。

我们提供了一个基于WeID的 \ `开发样例 <https://github.com/WeBankFinTech/weid-sample/tree/develop>`__， 您可以参考。

我们也提供了一些快捷工具，可以帮您快速体验 weid-java-sdk，请参考\ `章节3 <#section-3>`__\ 。


.. raw:: html

   <div id="section-3">


3 使用 weid-build-tools 提供的便捷工具
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. raw:: html

   </div>

在进行这个章节的操作之前，要确保 WeIdentity 的智能合约已经发布完成。


此步骤提供快速创建 WeIdentity DID、注册 Authority Issuer、发布 CPT、拉取 CPT 并生成 presentation policy 的能力。

3.1 创建您的 WeIdentity DID
''''''''''''''''''''''''''''''

这个步骤会帮您快速创建一个 WeIdentity DID。

::

    cd ../tools
    chmod +x *.sh
    ./create_weid.sh

若执行成功，则会打印以下信息。

::

    new weid has been created ----> did:weid:1:0x405a7ae297fc6d6fb02fb548db64b29f08114ca1
    the related private key and public key can be found at /home/app/tonychen/test_gradle/weid-build-tools/output/create_weid/0x405a7ae297fc6d6fb02fb548db64b29f08114ca1.

表明创建的 ``WeID`` 是did:weid:1:0x405a7ae297fc6d6fb02fb548db64b29f08114ca1。

在 ``weid-build-tools/output/create_weid/`` 目录下看到一些以 0x 开头的目录，找到跟刚刚生成的 WeIdentity DID 匹配的目录，里面包含了 WeIdentity DID，公钥 ``ecdsa_key.pub`` 和私钥 ``ecdsa_key``。

3.2 注册 Authority Issuer (权威凭证发行者)
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

.. note::
    只有委员会机构成员（ Committee Member ）可以进行本节操作，若您不是委员会机构成员，您可以将您的 WeIdentity DID 和机构名称发给委员会机构成员，让其帮您注册成 Authority Issuer。

- 注册 Authority Issuer

假设您要注册的 Authority Issuer 的 WeIdentity DID 为did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb，机构名称是 test。
::

    ./register_authority_issuer.sh --org-id test --weid did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb

如果执行成功，会打印以下信息。
::

    registering authorityissuer ---> did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb, name is :test
    success.

- 移除Authority Issuer

如果您需要移除某个Authority Issuer，比如您要移除did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb：

::

    ./register_authority_issuer.sh --remove-issuer did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb

若执行成功，则会打印以下信息。
::

    removing authority issuer ---> did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb...
    success.


3.3 注册 Specific Issuer(特定类型的发行者)
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

.. note::
    只有委员会成员（ Committee Member ）可以进行本节操作，若您不是委员会成员，您可以将您的 WeIdentity DID 和机构名称发给委员会成员，让其帮您注册成 Specific Issuer。

- 注册特定类型机构

假设您要注册的机构的 WeIdentity DID 为did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb，注册类型为 college，只需执行此下命令：

::

    ./register_specific_issuer.sh --type college --weid did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb

执行成功，则会打印以下信息。
::

    [RegisterIssuer] Adding WeIdentity DID did:weid:1:0xe10e52f6b7c6751bd03afc023b8e617d7fd0429c in type: college
    specific issuers and types have been successfully registered on blockchain.

如果您需要注册多个机构，请将其 WeIdentity DID 用分号分割开，如下所示：

::

    ./register_specific_issuer.sh --type college --weid did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb;did:weid:0x6efd256d02c1a27675de085b86989fa2ac1baddb

- 移除特定类型机构

比如您要从 college类型中移除did:weid:1:0x6efd256d02c1a27675de085b86989fa2ac1baddb：

::

    ./register_specific_issuer.sh --type college --remove-issuer did:weid:1:0x6efd256d02c1a27675de085b86989fa2ac1baddb

3.4 机构发布 CPT
''''''''''''''''''''''''''''''

此步骤会帮助机构发布指定的 CPT 到区块链上。

如果您的 WeIdentity DID 是执行\ `3.1节 <#section-3>`__\生成的，您可以不用传入私钥，只用指定 CPT 的路径即可。

::

    ./register_cpt.sh --cpt-dir test_data/single/ --weid did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb

若执行成功，则会打印以下信息：
::

    [RegisterCpt] register cpt file:JsonSchema.json result ---> success. cpt id ---> 1000
    [RegisterCpt] register cpt file:JsonSchema.json with success.
    finished.

如果您是通过其他途径创建的 WeIdentity DID，您需要自己指定私钥的位置。
假如机构的 WeID 是 did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb，需要注册的 CPT 都以 .json 后缀命名上传至 ``test_data/single/`` 目录下，私钥文件路径为 ``/home/test/private_key/ecdsa_key``

::

    ./register_cpt.sh --cpt-dir test_data/single/ --weid did:weid:1:0x5efd256d02c1a27675de085b86989fa2ac1baddb --private-key /home/test/private_key/ecdsa_key

若执行成功，则会打印以下信息：
::

    [RegisterCpt] register cpt file:JsonSchema.json result ---> success. cpt id ---> 1000
    [RegisterCpt] register cpt file:JsonSchema.json with success.
    finished.


3.5 拉取CPT并生成presentation policy模板
'''''''''''''''''''''''''''''''''''''''''''
.. note::
    此步骤，可以帮使用者从区块链上拉取指定的已发布的 CPT，并转化成 POJO，同时也会根据您生成一个 presentation policy 模板。

假如您需要将 CPT id 为 1000 的 CPT 从区块链上拉取下来，并基于 CPT 1000 生成 presentation policy 的配置模板。


::

    ./cpt_to_pojo.sh --cpt-list 1000

若执行成功，则会打印以下信息。
::
 
    begin to generate pojo from cpt...
    All cpt:[1000] are successfully transformed to pojo.

    the weidentity-cpt.jar can be found in /home/app/tonychen/test_gradle/weid-build-tools/dist/app/
    begin to generate presentation policy ...
    presentation policy template is successfully generated, you can find it at /home/app/tonychen/test_gradle/weid-build-tools/output/presentation_policy.


.. raw:: html

   <div id="reference-2">


附录1 手工配置 fisco.properties
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. raw:: html

   </div>

前提是您已经完成\ `章节1 <#section-2>`__\的步骤。

编辑 fisco.properties：

::

    cd weid-build-tools/resources/
    vim fisco.properties

您可以看到配置内容，我们需要将 WeIdentity 的智能合约地址和 chain id写入到指定配置项，找到以下配置项：

您需要将每个配置项替换成对应的智能合约地址，比如，如果 WeID Contract 的发布地址是0xabbc75543648af0861b14daa4f8582f28cd95f5e，
您需要将 ``weId.contractaddress`` 对应的 0x0 替换成 0xabbc75543648af0861b14daa4f8582f28cd95f5e，变成以下内容：

::

    weid.contractaddress=0xabbc75543648af0861b14daa4f8582f28cd95f5e
    cpt.contractaddress=0x0
    issuer.contractaddress=0x0
    evidence.contractaddress=0x0
    specificissuer.contractaddress=0x0

其他的智能合约地址的配置依次类推，直到所有的配置项都配置完成。

配置完智能合约地址后，您还需要将chain id也配置到指定项：
假设您需要配置的chain id的值为1，则进行如下配置。

::

    chain.id=1


附录2 升级 weid-java-sdk
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

如果在后续 WeIdentity JAVA SDK 出了新的版本，您希望将您现有的版本升级为新版本，或者回退到以前的版本，您可以手工将您的 ``build.gradle`` 里配置的版本改为您想要的版本，然后重新执行以上的步骤即可。

