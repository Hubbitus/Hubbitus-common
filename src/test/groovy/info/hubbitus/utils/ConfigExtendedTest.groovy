package info.hubbitus.utils

import spock.lang.Specification

/**
 * @author Pavel Alexeev.
 * @since 2017-11-29 13:47.
 */

class ConfigExtendedTest extends Specification {

    static final String CONFIG_SCRIPT = '''
        class TestClass{
            String s = 's initial'
            Integer i = 77
        }

        config{
            some.property = 'value'
            test = new TestClass()
        }'''

    def 'test merge objects'() {
        when:
            ConfigObject config = new ConfigSlurper().parse(CONFIG_SCRIPT).config
        then:
            config.test.getClass().simpleName == 'TestClass'

        when: 'Standard ConfigObject behaviour'
            ConfigObject config1 = new ConfigSlurper().parse('''config{ test.s = 's changed' }''').config
            config.merge(config1)
        then:
            config.test instanceof ConfigObject // <-!!! Not TestClass
            config.test.s == 's changed'
            !config.test.i

        when:
            ConfigExtended configExtended = ((ConfigExtended)new ConfigSlurper().parse(CONFIG_SCRIPT)).config
            configExtended.merge(config1)
        then:
            configExtended.test.getClass().simpleName == 'TestClass'
            configExtended.test.s == 's changed'
            configExtended.test.i == 77
    }

    def 'test setFromPropertyPathLikeKey'(){
        when:'Standard behaviour'
            ConfigObject config = new ConfigObject()
            config.'some.path' = 'qwerty'
        then:
            config.'some.path' == 'qwerty'
            !config.isSet('some')

        when:'Extended variant'
            ConfigExtended configExtended = new ConfigExtended()
            configExtended.setFromPropertyPathLikeKey('some.path', 'qwerty')
        then:
            !configExtended.isSet('some.path')
            configExtended.some.path == 'qwerty'
    }

    def 'test factory method create'(){
        when:
            ConfigExtended configExtended = ConfigExtended.create('''config{ one = 1 }''')
        then:
            configExtended instanceof ConfigExtended
            configExtended.size() == 1
            configExtended.one == 1

        when:
            configExtended = ConfigExtended.create('''superConfig{ one = 1 }''', 'superConfig')
        then:
            configExtended instanceof ConfigExtended
            configExtended.size() == 1
            configExtended.one == 1
    }

    def 'test leftShift (<<)'(){
        setup:
            ConfigExtended configExtended = ConfigExtended.create('''config{ one = 1 }''')

        when:
            configExtended.notExistent << 'list value'
        then:
            noExceptionThrown()
            configExtended.notExistent instanceof List
    }
}
