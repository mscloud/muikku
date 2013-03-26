package fi.muikku.plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.interceptor.Interceptors;

import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;

import fi.muikku.plugin.PluginDescriptor;
import fi.muikku.plugin.PluginLibraryDescriptor;
import fi.muikku.plugin.RESTPluginDescriptor;
import fi.muikku.plugin.manager.PluginLibraryLoadInfo;
import fi.muikku.plugin.manager.PluginManagerException;
import fi.muikku.plugin.manager.SingletonPluginManager;

public class PluginExtension implements Extension {

	private Logger logger = Logger.getLogger(PluginExtension.class.getName());
	
	void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager) {
		List<String> repositoryUrls = new ArrayList<>();
    List<PluginLibraryLoadInfo> pluginLoadInfos = new ArrayList<>();
    
    // TODO unhardcode :P
    
    repositoryUrls.add("http://repo.maven.apache.org/maven2");
    repositoryUrls.add("http://maven.otavanopisto.fi:7070/nexus/content/repositories/snapshots");
    repositoryUrls.add("http://maven.otavanopisto.fi:7070/nexus/content/repositories/releases");
    
    pluginLoadInfos.add(new PluginLibraryLoadInfo("fi.muikku", "core-plugins", "1.0.0-SNAPSHOT"));
    String eclipseWorkspace = System.getProperty("eclipse.workspace");
    
    SingletonPluginManager pluginManager;
		try {
			pluginManager = SingletonPluginManager.initialize(getClass().getClassLoader(), getPluginsFolder(), repositoryUrls, eclipseWorkspace);
		} catch (PluginManagerException e1) {
			throw new ExceptionInInitializerError(e1);
		}
		
		for (PluginLibraryLoadInfo pluginLoadInfo : pluginLoadInfos) {
      logger.info("Loading plugin library: " + pluginLoadInfo.toString());
    	try {		
	      pluginManager.loadPluginLibrary(pluginLoadInfo);
  		} catch (PluginManagerException e) {
  			logger.log(Level.SEVERE, "Failed to load plugin library: " + pluginLoadInfo.toString(), e);
  		}
    }
		
		List<PluginLibraryDescriptor> pluginLibraries = pluginManager.discoverPluginLibraries();
		for (PluginLibraryDescriptor pluginLibrary : pluginLibraries) {
			addDiscoveredBean(beanManager, beforeBeanDiscovery, pluginLibrary.getClass());
			
			for (Class<? extends PluginDescriptor> pluginDescriptorClass : pluginLibrary.getPlugins()) {
				addPluginBeans(beanManager, beforeBeanDiscovery, pluginDescriptorClass);
			}
		}
	}
	
	private void addPluginBeans(BeanManager beanManager, BeforeBeanDiscovery beforeBeanDiscovery, Class<? extends PluginDescriptor> pluginDescriptorClass) {
		String pluginName = pluginDescriptorClass.getName();
		
		try {
			PluginDescriptor tempInstance = pluginDescriptorClass.newInstance();
			pluginName = tempInstance.getName();
			
			List<Class<?>> beans = tempInstance.getBeans();
			if (beans != null) {
  			for (Class<?> bean : beans) {
  				addDiscoveredBean(beanManager, beforeBeanDiscovery, bean);
  			}
			}
			
			if (tempInstance instanceof RESTPluginDescriptor) {
				RESTPluginDescriptor restPluginDescriptor = (RESTPluginDescriptor) tempInstance;
				if (restPluginDescriptor.getRESTServices() != null) {
					for (Class<?> service : restPluginDescriptor.getRESTServices()) {
						addDiscoveredBean(beanManager, beforeBeanDiscovery, service);
					}
				}
			}

			addDiscoveredBean(beanManager, beforeBeanDiscovery, pluginDescriptorClass);
		} catch (InstantiationException e) {
			logger.log(Level.SEVERE, "Failed to initialize CDI on '" + pluginName + "' plugin.", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "Failed to initialize CDI on '" + pluginName + "' plugin.", e);
		}
	}
	
	private <T> void addDiscoveredBean(BeanManager beanManager, BeforeBeanDiscovery beforeBeanDiscovery, Class<T> beanClass) {
		logger.info("Adding discovered bean " + beanClass);
		
		// TODO: Test security annotations

		AnnotatedTypeBuilder<T> annotatedTypeBuilder = new AnnotatedTypeBuilder<T>()
      .readFromType(beanClass);
		
		for (Method method : getBeanMethods(beanClass)) {
			if (!Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers()) && !Modifier.isAbstract(method.getModifiers())) {
				Map<String, Class<?>[]> parameters = new HashMap<>();
				parameters.put("value", new Class<?>[] {
					TransactionalInterceptor.class,
					PluginContextClassLoaderInterceptor.class
				});
				
				Interceptors annotation = AnnotationInstanceProvider.of(Interceptors.class, parameters);
				annotatedTypeBuilder.addToMethod(method, annotation);
			}
		}

		AnnotatedType<T> annotatedType = annotatedTypeBuilder.create();
		
		beforeBeanDiscovery.addAnnotatedType(annotatedType);
	}
	
	private List<Method> getBeanMethods(Class<?> beanClass) {
		List<Method> result = new ArrayList<Method>();
		if (beanClass.getSuperclass() != null && !Object.class.equals(beanClass.getSuperclass())) {
			result.addAll(getBeanMethods(beanClass.getSuperclass()));
		}
		
		result.addAll(Arrays.asList(beanClass.getDeclaredMethods()));
		
		return result;
	}
	
	private static String getPluginsFolder() {
		String pluginsFolder = System.getProperty("muikku.plugin.dir");
		
		if (StringUtils.isBlank(pluginsFolder)) {
			String defaultDataDir = System.getProperty("jboss.server.data.dir");
			if (StringUtils.isBlank(defaultDataDir)) {
				defaultDataDir = new File(".").getAbsolutePath();
			}

			return defaultDataDir+ "/muikku-plugins";
		}
		
    return pluginsFolder;
  }
  
}