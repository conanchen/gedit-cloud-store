package com.github.conanchen.gedit.store.utils.database;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.registry.classloading.spi.ClassLoadingException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.UUIDGenerationStrategy;
import org.hibernate.id.UUIDGenerator;
import org.hibernate.id.uuid.StandardRandomStrategy;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor;

import java.io.Serializable;
import java.util.Properties;
import java.util.UUID;

/**
 * @author hai
 * @description custom uuid generator
 * @email hilin2333@gmail.com
 * @date 23/01/2018 5:52 PM
 */
public class CustomUUIDGenerator implements IdentifierGenerator, Configurable {
    public static final String UUID_GEN_STRATEGY = "uuid_gen_strategy";
    public static final String UUID_GEN_STRATEGY_CLASS = "uuid_gen_strategy_class";

    private static final CoreMessageLogger LOG = CoreLogging.messageLogger( UUIDGenerator.class );

    private UUIDGenerationStrategy strategy;
    private UUIDTypeDescriptor.ValueTransformer valueTransformer;

    public static CustomUUIDGenerator buildSessionFactoryUniqueIdentifierGenerator() {
        final CustomUUIDGenerator generator = new CustomUUIDGenerator();
        generator.strategy = new org.hibernate.id.uuid.CustomVersionOneStrategy();
        generator.valueTransformer = ToStringTransformer.INSTANCE;
        return generator;
    }

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        // check first for the strategy instance
        strategy = (UUIDGenerationStrategy) params.get( UUID_GEN_STRATEGY );
        if ( strategy == null ) {
            // next check for the strategy class
            final String strategyClassName = params.getProperty( UUID_GEN_STRATEGY_CLASS );
            if ( strategyClassName != null ) {
                try {
                    final ClassLoaderService cls = serviceRegistry.getService( ClassLoaderService.class );
                    final Class strategyClass = cls.classForName( strategyClassName );
                    try {
                        strategy = (UUIDGenerationStrategy) strategyClass.newInstance();
                    }
                    catch ( Exception ignore ) {
                        LOG.unableToInstantiateUuidGenerationStrategy(ignore);
                    }
                }
                catch ( ClassLoadingException ignore ) {
                    LOG.unableToLocateUuidGenerationStrategy( strategyClassName );
                }
            }
        }
        if ( strategy == null ) {
            // lastly use the standard random generator
            strategy = StandardRandomStrategy.INSTANCE;
        }

        if ( UUID.class.isAssignableFrom( type.getReturnedClass() ) ) {
            valueTransformer = UUIDTypeDescriptor.PassThroughTransformer.INSTANCE;
        }
        else if ( String.class.isAssignableFrom( type.getReturnedClass() ) ) {
            valueTransformer = ToStringTransformer.INSTANCE;
        }
        else if ( byte[].class.isAssignableFrom( type.getReturnedClass() ) ) {
            valueTransformer = UUIDTypeDescriptor.ToBytesTransformer.INSTANCE;
        }
        else {
            throw new HibernateException( "Unanticipated return type [" + type.getReturnedClass().getName() + "] for UUID conversion" );
        }
    }

    public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
        return valueTransformer.transform( strategy.generateUUID( session ) );
    }
}
