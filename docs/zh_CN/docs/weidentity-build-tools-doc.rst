.. role:: raw-html-m2r(raw)
   :format: html

.. _weidentity-build-tools-doc:

WeIdentity JAVA SDK安装部署文档（weidentity-build-tools方式）
============================================================

整体介绍
--------

  通过安装部署工具，您可以快速的在您的应用项目中集成weidentity-java-sdk。

部署步骤
--------

1. 通过 maven 引入 weidentity-java-sdk 依赖
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

在 ``build.gradle`` 文件中中添加相关的包依赖：

::

    dependencies {
        compile 'com.webank:weidentity-java-sdk:1.2.0.rc-3'
    }

####

.. raw:: html

   <div id="section-2">

2. 部署 WeIdentity 智能合约
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^


.. raw:: html

   </div>

2.1 下载安装部署工具
''''''''''''''''''''''''''''''
::

    git clone https://github.com/WeBankFinTech/weidentity-build-tools.git 
 

该工具默认会使用最新版本的
`WeIdentity智能合约 <https://github.com/WeBankFinTech/weidentity-contract>`__\ 。该工具可以帮您编译智能合约、打包智能合约、发布智能合约和自动配置。

2.2 配置区块链节点 IP 和 channelport
''''''''''''''''''''''''''''''''''''

::

    cd weidentity-build-tools/conf    
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

2.3 配置节点证书和秘钥文件
''''''''''''''''''''''''''

请参考\ `物料包web3sdk配置 <https://fisco-bcos-documentation.readthedocs.io/zh_CN/release-1.3/docs/tools/web3sdk.html>`__
将证书文件``ca.crt`` 和 ``client.keystore`` 复制出来，拷贝至 weidentity-build-tools 下面的 ``resources``
目录：\ ``weidentity-build-tools/resources/``\ 。

2.4 部署智能合约并自动生成配置文件
''''''''''''''''''''''''''''''

如果您是第一次使用本工具，您需要先进行编译：

::

    cd ..
    chmod +x compile.sh   
    ./compile.sh

如果执行过程没报错，大约1分钟左右可以编译完成。

如果您不是发布智能合约的机构，您可以直接跳过后续步骤，直接进入章节3。

编译完成后，您可以执行脚本deploy.sh进行Weidentity智能合约的发布与自动配置。

::

    chmod +x deploy.sh   
    ./deploy.sh

运行成功后，会自动在 ``resources`` 目录下生成
``applicationContext.xml``\ 。并且自动将 weidentity-contract
部署到区块链节点上，并将相应的智能合约地址也填入到
``applicationContext.xml``\ 。
同时，我们还会在weidentity-build-tools/output/keyPair目录下动态生成公私钥对。

::

    cd output/keyPair
    ls

您将看到私钥文件ecdsa_key，以及对应的公钥文件ecdsa_key.pub，并会自动将该私钥对应的地址注册为commit member，此私钥后续用于注册authority issuer。


.. raw:: html

   <div id="section-3">

3 快速使用
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^


.. raw:: html

   </div>


在进行这个章节的操作之前，要确保weidentity的智能合约已经发布完成。

如果您是weidentity智能合约的发布者，您需要保证\ `章节2 <#section-2>`__\ 的所有步骤已经正确完成。

如果您不是weidentity的智能合约发布者，您需要确保已经获取到weidentity的智能合约地址，并正确的配置在weidentity-build-tools的\ ``resources`` 目录下的\ ``applicationContext.xml``里。
配置方法请参考\ `附录2 <#reference-2>`__\。

此步骤提供快速创建Weidentity DID、注册Authority issuer、发布CPT、拉取CPT并编译成weidentity-cpt.jar的能力，其中创建Weidentity DID、注册Authority issuer、发布CPT
等动作也可以通过直接在应用里通过weidentity-java-sdk完成，您可以结合您的需要进行选择。

3.1 创建您的Weidentiy DID
''''''''''''''''''''''''''''''

这个步骤会帮您快速创建一个weidentity DID。

::

    cd weidentity-build-tools/tools
    chmod +x *.sh
    ./create_weId.sh

执行命令大约需要5秒钟，如果执行完没有报错，会提示“new weidentity did has been created”，并会打印出刚刚生成的weidentity did，同时在output目录下生成对应的weidentity DID
以及公钥和私钥。

::


    cd ../output/create_weId/
    ls

此时，我们可以在目录下看到一些以0x开头的目录，找到跟刚刚生成的weidentity DID匹配的目录，里面包含了weidentity DID文件weId，公钥ecdsa_key.pub和私钥ecdsa_key。

3.2 注册权威机构（authority issuer）
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

该步骤需要发布智能合约的机构来执行，需要使用\ `第2.4节 <#section-2>`__\ 中生成的私钥来注册权威机构。
这个步骤会帮您将一个指定的weidentity DID注册为权威机构。
如果您不是智能合约的发布者，您可以将您的weidentity DID和机构名称发送给智能合约的发布者，以完成权威机构的注册。

执行命令之前，您需要将要注册为权威机构的weidentity DID的信息配置在配置文件里：

::

    cd ../../conf/regist_authority_issuer_config
    vim parameter.conf

填入权威机构的weidentity DID和机构名字。
配置完成之后，您可以执行注册权威机构的命令进行注册。

::

    cd ../../tools/
    ./regist_authority_issuer.sh

执行命令大约需要5秒钟，如果执行没有报错，会提示“authority issuer has been successfully registed on blockchain”。注册成功。

3.3 机构发布CPT
''''''''''''''''''''''''''''''

此步骤会帮助机构发布指定的CPT到区块链上。

执行命令之前，您需要将您的weidentity DID和您想发布的CPT配置到对应的目录和文件中。

::

    cd ../conf/regist_cpt_conf/
    vim parameter.conf

填入您的weidentity DID。
配置完之后，您需要将您的CPT文件（需要以.json后缀命名）上传至当前目录。

做完上述配置之后，您可以执行命令来发布CPT。

执行注册CPT的命令需要您指定您的私钥的文件路径，如果您是执行\ `3.1节 <#section-3>`__\生成的weidentity DID，您可以在output目录下找到您的私钥。

::

    cd ../../output/create_weId/

找到和您的weidentity DID匹配的目录，比如是0x5efd256d02c1a27675de085b86989fa2ac1baddb

::

    cd 0x5efd256d02c1a27675de085b86989fa2ac1baddb/
    ls

找到私钥文件ecdsa_key。

然后执行命令发布CPT，如果您的私钥路径为/home/app/weidentity-build-tools/output/create_weId/0x5efd256d02c1a27675de085b86989fa2ac1baddb/ecdsa_key,则执行以下命令

::

    cd ../../../tools/
    ./regist_cpt.sh /home/app/weidentity-build-tools/output/create_weId/0x5efd256d02c1a27675de085b86989fa2ac1baddb/ecdsa_key

执行命令大约需要10秒钟，假设我们要发布的CPT是ID card，另假设文件名是cpt_ID_card.json，且已经上传到配置目录下。如果执行没报错，会在屏幕打印命令的执行情况：

::


    [RegisterCpt] begin to register cpt file:cpt_ID_card.json
    [RegisterCpt] result:{"errorCode":0,"errorMessage":"success","result":{"cptId":1000,"cptVersion":1}}


说明CPT文件cpt_ID_card.json成功发布到区块链上，且发布的ID为1000，后续我们可以用这个ID来查询我们发布的CPT。

同时，我们也会将发布CPT的结果以文件的形式记录下来，方便后续查询，您可以在output目录下查看。

::

    cd ../output/regist_cpt/
    cat regist_cpt.out

您会看到类似于“cpt_ID_card.json=1000”的信息，表明cpt_ID_card.json的CPT发布成功，发布的CPT ID是1000。

3.4 拉取CPT并生成presentation policy模板
'''''''''''''''''''''''''''''''''''''''''''

此步骤，可以帮使用者从区块链上拉取指定的CPT，并转化成POJO，在创建credential的时候，可以直接使用POJO进行创建。同时也会根据您配置的CPT ID来生成一个presentation policy模板。

在执行命令之前，您需要将您要拉取的CPT配置到文件中。

::

    cd ../conf/cpt_to_pojo_config/
    vim parameter.conf

将您想拉取的CPT配置在里面，支持配置多个CPT，用逗号做分隔。比如您想拉取CPT ID为100和101的CPT，您可以配置为：

::

    cpt.list=100,101

注：此处的CPT ID是权威机构已经发布到区块链上的，否则是拉取不成功的。

配置完成后，您只需要执行对应的命令即可进行CPT的拉取和POJO的转化。


::

    cd ../../tools/
    ./cpt_to_pojo.sh

执行命令大约需要20秒，如果执行没有报错，会在屏幕打印类似于“List:[[100, 101]] are successfully transformed to pojo. List:[[]] are failed.”的信息，这条信息表明CPT ID为100和101的已经发布成功。

CPT转成POJO并生成的weidentity-cpt.jar可以到dist目录下获取。

::

    cd ../dist/app/
    ls

直接将weidentity-cpt.jar拷贝至您的应用的classpath下即可使用。

此步骤同时也会帮您生成一个默认的presentation policy的配置模板，您可以按您的需求来修改。

::

    cd ../../output/presentation_policy
    ls

4 完成 weidentity-java-sdk 的集成
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
::

    cd weidentity-build-tools/resources
    ls

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
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

::

    cd weidentity-build-tools/contracts

上传WeIdentity的智能合约的合约文件（.sol结尾的文件），要注意的是，由于部分合约之间有依赖，所以为了保证编译顺利完成，您需要将WeIdentity所有的智能合约都上传至该目录，包括您没修改过的智能合约。

部署合约并自动生成配置文件
''''''''''''''''''''''''''''''

配置好之后，执行\ ``deploy.sh``\ ：

::

    cd ..   
    ./deploy.sh  

如果执行过程没有报错，该工具会帮您部署您的新合约，并为您打包好新的智能合约的jar包
``weidentity-contract-java-*.jar``\ (具体的版本号依赖智能合约的版本号)，放在dist/app目录下，您可以使用这个jar包，替换之前的WeIdentity智能合约jar包。

您需要重新将resources目录下的\ ``ca.crt``\ ，\ ``client.keystore`` 以及
``applicationContext.xml`` 拷贝至您的应用的\ ``resources`` 目录下。

--------------

.. raw:: html

   <div id="reference-2">


附录2 手工配置ApplicationContext.xml
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. raw:: html

   </div>

前提是您已经完成\ `章节2 <#section-2>`__\的步骤。

编辑applicationContext.xml：

::

    cd weidentity-build-tools/resources/
    vim applicationContext.xml

您可以看到配置内容，我们需要将weidentity的智能合约地址写入到指定配置项，找到以下配置项：

::

    <bean class="org.springframework.beans.factory.config.PreferencesPlaceholderConfigurer"
    id="appConfig">
    <property name="properties">
      <props>
        <prop key="weId.contractaddress">0x0</prop>
        <prop key="cpt.contractaddress">0x0</prop>
        <prop key="issuer.contractaddress">0x0</prop>
        <prop key="evidence.contractaddress">0x0</prop>
        <prop key="specificissuer.contractaddress">0x0</prop>
      </props>
    </property>
    </bean>

您需要将每个配置项替换成对应的智能合约地址，比如，如果weid Contract的发布地址是0xabbc75543648af0861b14daa4f8582f28cd95f5e，
您需要将“weId.contractaddress”对应的0x0替换成0xabbc75543648af0861b14daa4f8582f28cd95f5e，变成以下内容：

::

    <bean class="org.springframework.beans.factory.config.PreferencesPlaceholderConfigurer"
    id="appConfig">
    <property name="properties">
      <props>
        <prop key="weId.contractaddress">0xabbc75543648af0861b14daa4f8582f28cd95f5e</prop>
        <prop key="cpt.contractaddress">0x0</prop>
        <prop key="issuer.contractaddress">0x0</prop>
        <prop key="evidence.contractaddress">0x0</prop>
        <prop key="specificissuer.contractaddress">0x0</prop>
      </props>
    </property>
    </bean>

其他的智能合约地址的配置依次类推，直到所有的配置项都配置完成。

附录3 升级 weidentity-java-sdk
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

如果在后续weidentity java
sdk出了新的版本，您希望将您现有的版本升级为新版本，或者回退到以前的版本，您可以手工将您的build.gradle里配置的版本改为您想要的版本，然后重新执行以上的步骤即可。默认的，您依赖的特定版本的weidentity-java-sdk会依赖对应的版本的weidentity智能合约，如果您要定制您的智能合约版本，您可以手工替换智能合约的jar包。

