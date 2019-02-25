.. role:: raw-html-m2r(raw)
   :format: html

.. _weidentity-build-tools-doc:

WeIdentity JAVA SDK安装部署文档（weidentity-build-tools方式）
============================================================

1. 通过 maven 引入 weidentity-java-sdk 依赖
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

在 ``build.gradle`` 文件中中添加相关的包依赖：

::

    dependencies {
        compile 'com.webank:weidentity-java-sdk:1.+'
    }

####

.. raw:: html

   <div id="section-2">

2. 部署 WeIdentity 智能合约
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. raw:: html

   </div>

下载 ``weidentity-build-tools`` 工具到您的 service 部署的服务器上，完成
WeIdentity 智能合约的部署以及配置。

该工具默认会使用最新版本的
`WeIdentity智能合约 <https://github.com/WeBankFinTech/weidentity-contract>`__\ 。该工具可以帮您编译合约、打包合约、发布合约和自动配置。

2.1 配置区块链节点 IP 和 channelport
''''''''''''''''''''''''''''''''''''

::

    cd weidentity-build-tools    
    vim run.config   

修改 ``blockchain.node.address`` 字段，填入区块链节点 IP 和
channelport(需要参考区块链节点的\ ``config.json`` 配置文件)，示例如下：

.. code:: shell

    blockchain.node.address=127.0.0.1:33034

如果需要配置多个区块链节点，用逗号分隔，示例如下：

.. code:: shell

    blockchain.node.address=127.0.0.1:33034,10.10.10.11:33034

注：如果您需要使用特定版本的 WeIdentity
智能合约，请参考附录章节进行相应的更新操作。

2.2 配置节点证书和秘钥文件
''''''''''''''''''''''''''

| 在区块链节点的安装目录下，将目录 ``build/web3sdk/conf/`` 里的证书文件
``ca.crt`` 和 ``client.keystore`` 复制出来。
| 拷贝至 weidentity-build-tools 下面的 ``resources``
目录：\ ``weidentity-build-tools/resources/``\ 。

2.3 部署合约并自动生成配置文件
''''''''''''''''''''''''''''''

如果您是第一次使用本工具，您需要先进行编译：

::

    chmod +x compile.sh   
    ./compile.sh

如果执行过程没报错，大约1分钟左右可以编译完成。编译完成后，您可以执行脚本run.sh进行Weidentity智能合约的发布与自动配置。

::

    chmod +x run.sh   
    ./run.sh

运行成功后，会自动在 ``resources`` 目录下生成
``applicationContext.xml``\ 。并且自动将 weidentity-contract
部署到区块链节点上，并将相应的智能合约地址也填入到
``applicationContext.xml``\ 。

3 完成 weidentity-java-sdk 的集成
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

您可以将resources目录下刚刚生成的\ ``applicationContext.xml`` 文件，以及
``ca.crt``\ ，\ ``client.keystore`` ，拷贝至您的应用的 ``resources``
目录下，weidentity-java-sdk会自动加载相应的资源文件。

现在您可以使用 WeIdentity 开发您的区块链身份应用。weidentity-java-sdk
相关接口请见：\ `WeIdentity JAVA
SDK文档 <https://weidentity.readthedocs.io/projects/javasdk/zh_CN/latest/docs/weidentity-java-sdk-doc.html>`__

--------------

附录1 使用特定版本的智能合约
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

如果您想使用特定版本的智能合约，或者您根据自己的需要修改了WeIdentity的智能合约，您仍然可以使用
``weidentity-build-tools`` 工具进行智能合约的编译、打包和发布。

如果您还没有完成上述的\ `第2.1节和第2.2节 <#section-2>`__\ 里的配置，您需要先完成配置。

将特定版本的智能合约上传至 ``contracts`` 目录：
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

::

    cd weidentity-build-tools/contracts

上传WeIdentity的智能合约的合约文件（.sol结尾的文件），要注意的是，由于部分合约之间有依赖，所以为了保证编译顺利完成，您需要将WeIdentity所有的智能合约都上传至该目录，包括您没修改过的智能合约。

部署合约并自动生成配置文件
^^^^^^^^^^^^^^^^^^^^^^^^^^

配置好之后，执行\ ``run.sh``\ ：

::

    cd ..   
    ./run.sh  

如果执行过程没有报错，该工具会帮您部署您的新合约，并为您打包好新的智能合约的jar包
``weidentity-contract-java-*.jar``\ (具体的版本号依赖智能合约的版本号)，放在dist/app目录下，您可以使用这个jar包，替换之前的WeIdentity智能合约jar包。

您需要重新将resources目录下的 ``ca.crt``\ ，\ ``client.keystore`` 以及
``applicationContext.xml`` 拷贝至您的应用的\ ``resources`` 目录下。

--------------

附录2 升级 weidentity-java-sdk
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

如果在后续weidentity java
sdk出了新的版本，您希望将您现有的版本升级为新版本，或者回退到以前的版本，您可以手工将您的build.gradle里配置的版本改为您想要的版本，然后重新执行以上的步骤即可。默认的，您依赖的特定版本的weidentity-java-sdk会依赖对应的版本的weidentity智能合约，如果您要定制您的智能合约版本，您可以手工替换智能合约的jar包。
