# Various common groovy-java stuff

Project split from directory https://github.com/Hubbitus/ImapTree/tree/master/src/info/hubbitus/imaptree/utils into separate repository to do not copy/paste it many times.

The main purpose to collect there useful utility classes like `ProgressLogger`, `ConfigExtended` and share them between `Groovy`/`Java` projects..

## ProgressLogger

Helper class logger of progress operation.
It is intended for easy add possibility of logging progress of operations.
1. For example in Groovy way it so simple as:

```groovy
ProgressLogger.each([1, 2, 3, 4, 5]){
    println it // Some long run operation
}
```

It will produce (by println) output like:

    Process Integer #1 from 5 (20,00%). Spent (pack by 1) time: 0,041 (from start: 0,047)
    1
    Process Integer #2 from 5 (40,00%). Spent (pack by 1) time: 0,001 (from start: 0,278), Estimated items: 3, time: 0,417
    2
    Process Integer #3 from 5 (60,00%). Spent (pack by 1) time: 0,012 (from start: 0,330), Estimated items: 2, time: 0,220
    3
    Process Integer #4 from 5 (80,00%). Spent (pack by 1) time: 0,001 (from start: 0,340), Estimated items: 1, time: 0,085
    4
    Process Integer #5 from 5 (100,00%). Spent (pack by 1) time: 0,001 (from start: 0,344)
    5

2. Ofter useful provide out method, for example to tie into current scope logger instead of global stdout, and add
some additional transform, it also simple:

```groovy
ProgressLogger.each([1, 2, 3, 4, 5]){
	println it
}{ log.info "=$it=" }
```

3. It may be used directly in plain old Java style like:
```groovy
ProgressLogger pl = new ProgressLogger(aisList, {log.info(it)});
for (Object aisObj in aisList){
    pl.next();
    try{
        rowMapper(aisObj, di)
    }
    catch(AsaException ae){
        commonErrorAdd(di, ae);
    }
}
```
5. Or measure one run:
```groovy
ProgressLogger.measure({log.info(it)}, { /* long work  * / }, 'Doing cool work')
```
Or:
```groovy
def result = ProgressLogger.measureAndLogTime({spent-> log.debug('Operation took: ' + spent) }){
	println 'test' // Some long measured work
	return 42
}
assert 42 == result
```
6. When amount of elements or iterations is not known (f.e. stream processing or recursive calls like tree traversal)
totalAmountOfElements set to -1 and simpler statistic returned:
```groovy
def pl = new ProgressLogger()
[1, 2, 3, 4].each{
    sleep 1000;
    pl.next();
}
```

Result will be something like:

    Process item #1. Spent (pack by 1) time: 1,007 (from start: 1,007)
    Process item #2. Spent (pack by 1) time: 1,000 (from start: 2,055)
    Process item #3. Spent (pack by 1) time: 1,001 (from start: 3,058)
    Process item #4. Spent (pack by 1) time: 1,001 (from start: 4,061)
    You are able provide result messages, custom message formatting, pack size after precessing write log, auto adjusting
    such pack size to do not spam log, provide custom logging methods and so on.

## ConfigExtended

Main goal to add functionality to `ConfigObject` GDK class.

### 1. setFromPropertyPathLikeKey()

First it allow operations opposite flatten, set hierarchy from string, like:
```groovy
ConfigExtended conf = …
conf.setFromPropertyPathLikeKey('some.deep.hierarchy.of.properties', value)
```
and then access it as usual:
```groovy
conf.some.deep.hierarchy.of.properties
```
not as it is one string property.
```groovy
conf.'some.deep.hierarchy.of.properties'
```

### 2. override merge to step into objects properties, not just ConfigObject instances and maps

Additionally it override merge of `ConfigObjects` and do not replace completely replace Objects but set properties of it.
For example standard behaviour:
```groovy
// Uncomment next line if you are plan run example from GroovyConsole to handle defined there classes: http://groovy.329449.n5.nabble.com/GroovyConsole-and-context-thread-loader-td4471707.html
// Thread.currentThread().contextClassLoader = getClass().classLoader
@groovy.transform.ToString
class Test{
  String s = 's initial'
  Integer i = 77
}

ConfigObject config = new ConfigSlurper().parse('''config{
  some.property = 'value'
  test = new Test()
}''').config

ConfigObject config1 = new ConfigSlurper().parse('''config{ test.s = 's change' }''').config

config.merge(config1)
assert config.test.s == 's change'
```

But stop, why config.test replaced? Our intention was to set only their field s!
**That class do that magic**


### 3. Allow by default leftShift (<<) operation on non-existing values

    ConfigExtended configExtended = ConfigExtended.create('''config{ one = 1 }''')
    configExtended.notExistent << 'list value'
    configExtended.notExistent instanceof List

It useful to do not extra checks in cycle, f.e.:

    (1..100).each{
        if (!configExtended.isSet('listValues')) configExtended.listValues = []
        configExtended.listValues.add(it)
    }

became just:

    (1..100).each{
        configExtended.listValues.add(it)
    }

and result identical!

## GlobalConfig singleton for easy access configuration from resources

Singleton class for automatically load `Config.groovy` or `config.groovy` config from current resources and dispose them as singleton.

Idea to simplify access you configuration in simple scripts application when no IOC/DI used.
F.e. in resources you have file `Config.groovy` with content:

    config {
        some {
            property = 'qwerty'
        }
    }

Then you may just do in script *without any initialization*:

    println GlobalConfig.instance.some.property

Please note, file must include config{} closure on top level which will be stripped automatically.

# Enable by Maven and gradle

## Maven
Just add dependency like:

    <dependency>
      <groupId>info.hubbitus</groupId>
      <artifactId>Hubbitus-common</artifactId>
      <version>1.7</version>
    </dependency>

## Gradle

    compile 'info.hubbitus:Hubbitus-common:1.7'


# Changelog
## version 1.7 2018-11-21 22:56
* Improve GlobalConfig to log warning when config not found, make more robust
* Minor test fir

## version 1.6 2018-04-26 16:14
* Add method `ProgressLogger.measureAndLogTime`

## version 1.5 2018-04-19 19:35
* Add class `GlobalConfig` for easy access configuration from resources.
* Update gradlew version to 4.7.
* Adopt test to be locale independent.

## version 1.4 2017-11-29 23:30
* Add test `info.hubbitus.utils.ConfigExtendedTest`
* Add method `info.hubbitus.utils.ConfigExtended.setFromPropertyPathLikeKey(java.lang.String, java.lang.String)` to do not always quote `String` values
* And `factory method` `info.hubbitus.utils.ConfigExtended.create(java.lang.String, java.lang.String)` to do not always remember use `ConfigSlurper` and cast result.

  So now instead of:

    ```
    ConfigExtended configExtended = ((ConfigExtended)new ConfigSlurper().parse('config{ one = 1 }')).config
    ```

 you may just do:
    ```
    ConfigExtended.create('config{ one = 1 }')
    ```
* Add `info.hubbitus.utils.ConfigExtended.leftShift` operator (<< operation) for easy list creation

## version 1.3.1
* Fix `ProgressLogger.measure` with object result from closure

## version 1.3
* Fix benchmark. Add test

## version 1.2
* Make `ProgressLogger` thread safe, add parallel test with GParse
* `next(Closure toRun)` method now returns result from `toRun` closure. So you may use it directly n collection methods like `collect`:
```
List list = [1, 2, 3]
ProgressLogger pl = new ProgressLoger(list);
def res = list.collect{
    pl.next{
        it * 2
    }
}
assert res = [2, 4, 6]
```
* Step to use `Producer`/`Consumer` functional interfaces instead of just `Closure` as arguments to be closer to Java calls!
 Before that you had to create instance of `Closure`. It works, but has two drawbacks:
 * It can't be simplified by `lambda` usage
 * In logback loggers, when method accounted `.call()` main method logged
 * Java test initiated also

So, now in pure Java instead of ugly:
```
    ProgressLogger pl = new ProgressLogger(list, new Closure(null) {
        @Override
        public Object call(Object... args) {
            log.debug((String) args[0]);
            return null;
        }
    });
```
 you may simply do:
```
    ProgressLogger pl = new ProgressLogger(list, it -> log.debug(it));
```
which much more cleaner.
* Step to gradle 4.1
