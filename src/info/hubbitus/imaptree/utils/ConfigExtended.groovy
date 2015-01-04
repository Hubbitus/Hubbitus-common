package info.hubbitus.imaptree.utils

import groovy.transform.CompileStatic

/**
 * Main goal to add functionality to ConfigObject GDK class
 *
 * @author Pavel Alexeev - <Pahan@Hubbitus.info> (pasha)
 * @created 2015-01-03 22:10
 **/
@CompileStatic
class ConfigExtended extends ConfigObject{
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
	 * @param conf
	 * @param propertyLikeKey
	 * @param value
	 */
	public void setFromPropertyPathLikeKey(String propertyLikeKey, value){
		merge((ConfigObject)new ConfigSlurper().parse( "config{ $propertyLikeKey = $value }" ).config);
	}
}
