### V3.1.1 (2023-6-15)
* 优化功能:
1. 更新complie.sh和deploy.sh，可以通过命令行执行脚本部署WeIdentity，和可视化部署效果一致
2. 增加多个CPT模板
3. 集成weid-java-sdk 3.1.1
4. 更新多个已知漏洞的组件

### V3.1.0 (2023-4-28)
* 优化功能:
1. 修复创建并启用新的应用后配置文件没有更新chain.id导致registerAuthorityIssuer失败
2. 生成的WeIdentity配置文件支持本地数据库部署
3. 集成weid-java-sdk 3.1.0和weid-kit 3.1.0
4. 更新多个已知漏洞的组件

### V3.0.0 (2022-12-15)
* 优化功能:
1. 修复registerPolicy失败和部署Evidence合约失败
2. 集成weid-java-sdk 3.0.0和weid-kit 3.0.0

### V1.3.1 (2022-10-14)
* 优化功能:
1. 前端获取weid列表的方式改变
2. 集成weid-java-sdk 1.8.6

### V1.3.0 (2022-08-16)

* 新增功能:
1. 前端页面支持选择FISCO BCOS版本（2.0或者3.0）
2. 前端页面支持选择国密或者非国密
3. 适配FISCO BCOS V3.0.0的配置生成

* 优化功能:
1. 升级log4j到2.18.0
2. 升级jackson_version到2.13.3
3. 集成weid-java-sdk 1.8.5

### V1.0.28 (2021-12-11)

* 优化功能:
1. 升级log4j到2.15.0

### V1.0.27 (2021-06-10)

* 优化功能:
1. 集成weid-java-sdk1.8.2, 此版本sdk排除了solcJ-all的依赖

### V1.0.26 (2021-04-15)

* 优化功能:
1. 白名单支持链上分页查询与删除


### V1.0.25 (2021-04-09)

* 优化功能:
1. 优化页面显示文字


### V1.0.24 (2021-03-29)

* 新增功能:
1. 前端页面重构优化
2. 集成WeBase辅助配置安装

### V1.0.23 (2021-01-26)

* 新增功能:
1. 支持国密版本安装部署
2. 新增数据概览面板
3. 支持CPT实时链上查询和分页查询
4. 支持Policy链上注册及查询

### V1.0.22 (2020-12-20)

* 新增功能:
1. 适配weid-java-sdk1.7.1

### V1.0.21 (2020-11-20)

* 新增功能:
1. 合约部署支持备注
2. 支持查询存证合约启用机构列表
3. 权威机构注册添加描述
4. 权威机构自主注册以及管理员认证和撤销
5. 安装脚本支持指定端口安装
6. 修复weid列表查询分页bug
7. 优化webase集成

### V1.0.20 (2020-11-10)

* 系统优化:
1. 升级web3sdk版本到2.4.4
2. 升级spring版本到5.2.8
3. 升级weid-java-sdk版本到1.6.7

### V1.0.19 (2020-09-19)

* 新增功能:
1. 优化cpt注册相关功能。
2. 支持weid历史查询。
3. 支持redis配置。
4. 集成webase安装，访问。
5. 其他界面节优化。

### V1.0.7 (2019-08-22)

* 新增功能:
1. 优化了脚本命令的输出日志。
2. 优化了CPT样例数据

### V1.0.0 (2019-01-31)
首次release.

* 新增功能:
1. 支持快速集成weidentity-java-sdk。支持自动发布Weidentity智能合约并自动生成配置好的weidentity-java-sdk配置文件。
3. 支持Weidentity智能合约的编译和发布。
4. 支持适配特定版本的Weidentity智能合约。
5. 支持适配特定版本的weidentity-java-sdk。
