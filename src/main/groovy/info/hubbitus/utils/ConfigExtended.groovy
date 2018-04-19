package info.hubbitus.utils

import groovy.transform.CompileStatic

/**
 * Main goal to add functionality to ConfigObject GDK class
 *
 * <h1>1. setFromPropertyPathLikeKey</h1>
 * First it allow operations opposite flatten, set hierarchy from string, like:
 * <code><pre>
 * ConfigExtended conf = …
 *	conf.setFromPropertyPathLikeKey('some.deep.hierarchy.of.properties', value)
 * </pre></code>
 * and then access it as usual:
 * conf.some.deep.hierarchy.of.properties
 * not as it is one string property.
 * conf.'some.deep.hierarchy.of.properties'
 *
 * <h1>2. override merge to step into objects properties, not just ConfigObject instances and maps</h1>
 * Additionally it override merge of ConfigObjects and do not replace completely replace Objects but set properties of it.
 * For example:
 * Standard behaviour:
 * <pre>
 * // Uncomment next line if you are plan run example from GroovyConsole to handle defined there classes: http://groovy.329449.n5.nabble.com/GroovyConsole-and-context-thread-loader-td4471707.html
 * // Thread.currentThread().contextClassLoader = getClass().classLoader
 * @groovy.transform.ToString
 * class Test{
 * 	String s = 's initial'
 * 	Integer i = 77
 * }
 *
 * ConfigObject config = new ConfigSlurper().parse('''config{
 * 	some.property = 'value'
 * 	test = new Test()
 * }''').config
 *
 * ConfigObject config1 = new ConfigSlurper().parse('''config{ test.s = 's change' }''').config
 *
 * config.merge(config1)
 * assert config.test.s == 's change'
 * </pre>
 *
 * BUT stop, why config.test replaced? Our intention was to set only their field s!
 * That class do that
 *
 * <h1>3. Allow by default leftShift (<<) operation on non-existing values</h1>
 * @link #leftShift
 *
 * @author Pavel Alexeev - <Pahan@Hubbitus.info> (pasha)
 * @created 2015-01-03 22:10
 **/
class ConfigExtended extends ConfigObject{
	ConfigObject parent
	String name

	/**
	 * Factory to do not always remember use {@see ConfigSlurper} and cast result
	 *
	 * Instead of:
	 * <code>
	 *     ConfigExtended configExtended = ((ConfigExtended)new ConfigSlurper().parse('config{ one = 1 }')).config
	 * </code>
	 * you may just do:
	 * <code>
	 *     ConfigExtended.create('config{ one = 1 }')
	 * </code>
	 *
	 * @param script
	 * @param firstLevelToStrip
	 * @return
	 */
	static ConfigExtended create(String script, String firstLevelToStrip = 'config'){
		((ConfigExtended)new ConfigSlurper().parse(script)."$firstLevelToStrip")
	}

	ConfigExtended() {
	}

	ConfigExtended(URL file, ConfigObject parent, String name) {
		super(file)
		this.name = name
		this.parent = parent
	}

	/**
	 * It is not work set it from doted string, but read in groovy syntax like:
	 * ConfigObject conf = new ConfigObject();
	 * conf.'some.key' = 77;
	 * assert conf.some.key == [:]
	 *
	 * It is also safe for keys without dots.
	 *
	 * Born in ais adapter.
	 *
	 * @see #setFromPropertyPathLikeKey(String, String)
	 *
	 * @param conf
	 * @param propertyLikeKey
	 * @param value
	 */
	void setFromPropertyPathLikeKey(String propertyLikeKey, value){
		merge((ConfigObject)new ConfigSlurper().parse( "config{ $propertyLikeKey = $value }" ).config);
	}

	/**
	 *
	 * @see #setFromPropertyPathLikeKey(String, Object)
	 * @param propertyLikeKey
	 * @param value
	 */
	void setFromPropertyPathLikeKey(String propertyLikeKey, String value){
		merge((ConfigObject)new ConfigSlurper().parse( "config{ $propertyLikeKey = '$value' }" ).config);
	}

	/**
	 * Override merge and doMerge to allow save Objects other than ConfigObject and just set theirs properties (fields).
	 * See more details and example in class description
	 *
	 * @param other The ConfigObject to merge with
	 * @return The result of the merge
	 */
	@Override
	Map merge(ConfigObject other) {
		return doMerge(this, other);
	}

	/**
	 * Override merge and doMerge to allow save Objects other than ConfigObject and just set theirs properties (fields).
	 * See more details and example in class description
	 *
	 * @param config
	 * @param other
	 * @return
	 */
	private Map doMerge(Map config, Map other) {
		for (Object o : other.entrySet()) {
			Object key = o.key
			Object value = o.value

			Object configEntry = config.get(key)

			if (configEntry == null) {
				config.put(key, value)
			} else {
				if (configEntry instanceof Map && ((Map)configEntry).size() > 0 && value instanceof Map) {
					// recur
					doMerge((Map) configEntry, (Map) value)
				} else {
					if (configEntry instanceof Map && ((Map)configEntry).size() > 0){ // As parent
						config.put(key, value)
					}
					else{ // Addition to do not replace object by instead modify its properties inplace
						value.flatten().each{prop->
							configEntry."${prop.key}" = prop.value
						}
					}
				}
			}
		}

		return config;
	}

	/**
	 * Overrides the default getProperty implementation to create {@see ConfigObjectParentAware} instances
	 */
	@Override
	Object getProperty(String name) {
		if ("configFile".equals(name))
			return this.configFile;

		if (!containsKey(name)) {
			ConfigExtended prop = new ConfigExtended(this.getConfigFile(), this, name);
			put(name, prop);

			return prop;
		}

		return get(name);
	}

	/**
	 * Auto-replace value by list on leftShift (<<) operation
	 *
	 * <code>
	 * ConfigExtended configExtended = ConfigExtended.create('''config{ one = 1 }''')
	 * configExtended.notExistent << 'list value'
	 * configExtended.notExistent instanceof List
	 * </code>
	 *
	 * It useful to do not extra checks in cycle, f.e.:
	 *
	 * (1..100).each{
	 * if (!configExtended.isSet('listValues')) configExtended.listValues = []
	 *   configExtended.listValues.add(it)
	 * }
	 *
	 * You may just drop "if (!configExtended.isSet('listValues')) configExtended.listValues = []"!
	 *
	 * @param value
	 * @return
	 */
	ConfigExtended leftShift(value){
		// Replace self in parent! And there must be explicit getters to do not create ConfigObjects
		this.getParent()[this.getName()] = [value]
		this
	}
}
