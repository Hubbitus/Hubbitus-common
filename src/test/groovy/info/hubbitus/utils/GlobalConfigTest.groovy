package info.hubbitus.utils

import spock.lang.Specification

/**
 * @author Pavel Alexeev.
 * @since 2018-04-19 19:32.
 */
class GlobalConfigTest extends Specification {
	def 'test GlobalConfig automatic usage'(){
		expect:
			GlobalConfig.instance.test.value == 'it is test'
	}
}
