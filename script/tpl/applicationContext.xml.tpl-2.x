<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~       Copyright© (2018) WeBank Co., Ltd.
  ~
  ~       This file is part of weidentity-java-sdk.
  ~
  ~       weidentity-java-sdk is free software: you can redistribute it and/or modify
  ~       it under the terms of the GNU Lesser General Public License as published by
  ~       the Free Software Foundation, either version 3 of the License, or
  ~       (at your option) any later version.
  ~
  ~       weidentity-java-sdk is distributed in the hope that it will be useful,
  ~       but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~       GNU Lesser General Public License for more details.
  ~
  ~       You should have received a copy of the GNU Lesser General Public License
  ~       along with weidentity-java-sdk.  If not, see <https://www.gnu.org/licenses/>.
  -->

<beans xmlns="http://www.springframework.org/schema/beans"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
	   xmlns:tx="http://www.springframework.org/schema/tx" xmlns:aop="http://www.springframework.org/schema/aop"
	   xmlns:context="http://www.springframework.org/schema/context"
	   xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans-2.5.xsd  
         http://www.springframework.org/schema/tx   
    http://www.springframework.org/schema/tx/spring-tx-2.5.xsd  
         http://www.springframework.org/schema/aop   
    http://www.springframework.org/schema/aop/spring-aop-2.5.xsd
    http://www.springframework.org/schema/context
    http://www.springframework.org/schema/context/spring-context-3.0.xsd">

  <bean class="org.springframework.beans.factory.config.PreferencesPlaceholderConfigurer"
    id="appConfig">
    <property name="properties">
      <props>
        <prop key="weId.contractaddress">${WEID_ADDRESS}</prop>
        <prop key="cpt.contractaddress">${CPT_ADDRESS}</prop>
        <prop key="issuer.contractaddress">${ISSUER_ADDRESS}</prop>
        <prop key="evidence.contractaddress">${EVIDENCE_ADDRESS}</prop>
        <prop key="specificissuer.contractaddress">${EVIDENCE_ADDRESS}</prop>
      </props>
    </property>
    <!--  <property name="location" value="classpath:application.properties" />-->
  </bean>

	<context:component-scan base-package="com.webank.weid.config"/>

	<bean id="encryptType" class="org.fisco.bcos.web3j.crypto.EncryptType">
		<constructor-arg value="0"/> <!-- 0:standard 1:guomi -->
	</bean>

	<bean id="groupChannelConnectionsConfig" class="org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig">
		<property name="allChannelConnections">
			<list>
				<bean id="group1"  class="org.fisco.bcos.channel.handler.ChannelConnections">
					<property name="groupId" value="1" />
					<property name="connectionsStr">
						<list>
							${BLOCKCHIAN_NODE_INFO}
						</list>
					</property>
				</bean>
			</list>
		</property>
	</bean>

	<bean id="channelService" class="org.fisco.bcos.channel.client.Service" depends-on="groupChannelConnectionsConfig">
		<property name="groupId" value="1" />
		<property name="orgID" value="fisco" />
		<property name="allChannelConnections" ref="groupChannelConnectionsConfig"></property>
	</bean>
	
</beans>
