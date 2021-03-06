/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.rack.deployers;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.rack.core.RackApplicationPoolImpl;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rack.spi.RackApplicationFactory;
import org.torquebox.rack.spi.RackApplicationPool;

/**
 * <pre>
 * Stage: PRE_DESCRIBE
 *    In: RackApplicationMetaData
 *   Out: RackApplicationMetaData, RackApplicationPool, PoolMetaData
 * </pre>
 * 
 */
public class RackApplicationPoolDeployer extends AbstractSimpleVFSRealDeployer<RackApplicationMetaData> {

    static final String POOL_NAME = "web";

    public RackApplicationPoolDeployer() {
        super( RackApplicationMetaData.class );
        addOutput( RackApplicationMetaData.class );
        addOutput( BeanMetaData.class );
        addOutput( PoolMetaData.class );
        setStage( DeploymentStages.PRE_DESCRIBE );
        setRelativeOrder( 500 );
    }

    @Override
    public void deploy(VFSDeploymentUnit unit, RackApplicationMetaData metaData) throws DeploymentException {
        String beanName = AttachmentUtils.beanName( unit, RackApplicationPool.class );
        BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder( beanName, RackApplicationPoolImpl.class.getName() );

        metaData.setRackApplicationPoolName( beanName );

        String appfactoryBeanName = metaData.getRackApplicationFactoryName();
        if (appfactoryBeanName == null) {
            appfactoryBeanName = AttachmentUtils.beanName( unit, RackApplicationFactory.class );
        }

        String runtimePoolBeanName = metaData.getRubyRuntimePoolName();
        if (runtimePoolBeanName == null) {
            runtimePoolBeanName = AttachmentUtils.beanName( unit, RubyRuntimePool.class, POOL_NAME );
            metaData.setRubyRuntimePoolName( runtimePoolBeanName );
        }

        ValueMetaData runtimePoolInjection = builder.createInject( runtimePoolBeanName );
        ValueMetaData appFactoryInjection = builder.createInject( appfactoryBeanName );

        builder.addConstructorParameter( RubyRuntimePool.class.getName(), runtimePoolInjection );
        builder.addConstructorParameter( RackApplicationFactory.class.getName(), appFactoryInjection );

        AttachmentUtils.attach( unit, builder.getBeanMetaData() );
        if (null == AttachmentUtils.getAttachment( unit, POOL_NAME, PoolMetaData.class )) {
            AttachmentUtils.multipleAttach( unit, new PoolMetaData( POOL_NAME ), POOL_NAME );
        }
    }

}
