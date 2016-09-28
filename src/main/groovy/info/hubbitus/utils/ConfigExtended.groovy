package info.hubbitus.utils

import groovy.transform.CompileStatic

/**
 * Main goal to add functionality to ConfigObject GDK class
 *
 * First it allow operations opposite flatten, set hierarchy from string, like:
 * ConfigExtended conf = …
 *	conf.setFromPropertyPathLikeKey('some.deep.hierarchy.of.properties', value)
 * and then access it as usual:
 * conf.some.deep.hierarchy.of.properties
 * not as it is one string property.
 * conf.'some.deep.hierarchy.of.properties'
 *
 * Additionally it override merge of ConfigObjects and do not replace completely replace Objects but set properties of it.
 * For example:
 * Standard behaviour:
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
 * assert config.test == 's change'
 *
 * BUT stop, why config.test replaced? Our intention was to set only their field s!
 * That class do that
 *
 *
 * @author Pavel Alexeev - <Pahan@Hubbitus.info> (pasha)
 * @created 2015-01-03 22:10
 **/
class ConfigExtended extends ConfigObject{
	/**
	 * It is not work set it from doted string, but read in groovy syntax like:
	 * ConfigObject conf = new ConfigObject();
	 * conf.'some.key' = 77;
	 * assert conf.some.key == [:]
	 *
	 * It is also safe for keys without dots.
	 *
	 * WARNING! Because this method so powerful (f.e. it give ability provide closures definition from command line),
	 * there no easy possibility distinguish intentions provide atoms or string literals! So when someValue passed it
	 * is it variable name or string value? So, strings must be quoted in regular syntax, so call may look like:
	 * config.setFromPropertyPathLikeKey('some.test.s', '"s CHANGED"');
	 *
	 * Born in ais adapter.
	 *
	 * @param conf
	 * @param propertyLikeKey
	 * @param value
	 */
	public void setFromPropertyPathLikeKey(String propertyLikeKey, value){
		merge((ConfigObject)new ConfigSlurper().parse( "config{ $propertyLikeKey = $value }" ).config);
	}

	/**
	 * Override merge and doMerge to allow save Objects other than ConfigObject and just set theirs properties (fields).
	 * See more details and example in class description
	 *
	 * @param other The ConfigObject to merge with
	 * @return The result of the merge
	 */
	@Override
	public Map merge(ConfigObject other) {
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
			Map.Entry next = (Map.Entry) o;
			Object key = next.getKey();
			Object value = next.getValue();

			Object configEntry = config.get(key);

			if (configEntry == null) {
				config.put(key, value);
			} else {
				if (configEntry instanceof Map && ((Map)configEntry).size() > 0 && value instanceof Map) {
					// recur
					doMerge((Map) configEntry, (Map) value);
				} else {
					if (configEntry instanceof Map && ((Map)configEntry).size() > 0){ // As parent
						config.put(key, value);
					}
					else{ // Addition to do not replace object by instead modify its properties inplace
						value.flatten().each{prop->
							configEntry."${prop.key}" = prop.value;
						}
					}
				}
			}
		}

		return config;
	}
}
