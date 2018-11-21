package info.hubbitus.utils

import groovy.util.logging.Slf4j

/**
 * Singleton class to automatically load `Config.groovy` or `config.groovy` config from current resources and dispose them as singleton.
 *
 * Idea for simplify access you configuration in simple scripts application when no IOC/DI used.
 * F.e. in resources you have file:
 * Config.groovy with content:
 *
 * <code>
 * config {
 * 	some {
 *		property = 'qwerty'
 * 	}
 * }
 * </code>
 * Then you may just do in script *without any initialization*:
 * <code>
 * println GlobalConfig.instance.some.property
 * </code>
 * Please note, file must include config{} closure on top level which will be stripped automatically.
 *
 * @TODO place it into Hubbitus-common after testing
 *
 * @author Pavel Alexeev.
 * @since 2018-04-19 12:21.
 */
@Slf4j
@Singleton(lazy=true, strict=false)
class GlobalConfig {
	@Delegate
	ConfigExtended config

	GlobalConfig() {
		def config = ['/Config.groovy', '/config.groovy'].findResult{
			getClass().getResource(it)
		}
		if (!config){
			log.error("Config files '/Config.groovy' or '/config.groovy' not found in the classpath! Init with empty.")
			this?.@config = new ConfigExtended()
		}
		this?.@config = (ConfigExtended)(new ConfigSlurper().parse(config).config)
	}
}
