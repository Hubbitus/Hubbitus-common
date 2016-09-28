# Various common groovy-java stuff

Project split from directory https://github.com/Hubbitus/ImapTree/tree/master/src/info/hubbitus/imaptree/utils into separate repository to do not copy/paste it many times.

The main purpose to collect there useful utility classes like `ProgressLogger`, `ConfigExtended` and share them between `Groovy`/`Java` projects..

## ProgressLogger 

Helper class logger of progress operation.
It is intended for easy add possibility of logging progress of operations.
1. For example in Groovy way it so simple as:

```
ProgressLogger.each([1, 2, 3, 4, 5]){
    println it // Some long run operation
}
```

It will produce (by println) output like:

    Process Integer #1 from 5 (20,00%). Spent (pack 1 elements) time: 0,041 (from start: 0,047)
    1
    Process Integer #2 from 5 (40,00%). Spent (pack 1 elements) time: 0,001 (from start: 0,278), Estimated items: 3, time: 0,417
    2
    Process Integer #3 from 5 (60,00%). Spent (pack 1 elements) time: 0,012 (from start: 0,330), Estimated items: 2, time: 0,220
    3
    Process Integer #4 from 5 (80,00%). Spent (pack 1 elements) time: 0,001 (from start: 0,340), Estimated items: 1, time: 0,085
    4
    Process Integer #5 from 5 (100,00%). Spent (pack 1 elements) time: 0,001 (from start: 0,344)
    5

2. Ofter useful provide out method, for example to tie into current scope logger instead of global stdout, and add
some additional transform, it also simple:

```
ProgressLogger.each([1, 2, 3, 4, 5]){
	println it
}{ log.info "=$it=" }
```

3. It may be used directly in plain old Java style like:
```
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
```
ProgressLogger.measure({log.info(it)}, { /* long work  * / }, 'Doing cool work')
```
6. When amount of elements or iterations is not known (f.e. stream processing or recursive calls like tree traversal)
totalAmountOfElements set to -1 and simpler statistic returned:
```
def pl = new ProgressLogger()
[1, 2, 3, 4].each{
    sleep 1000;
    pl.next();
}
```

Result will be something like:

    Process item #1. Spent (pack 1 elements) time: 1,007 (from start: 1,007)
    Process item #2. Spent (pack 1 elements) time: 1,000 (from start: 2,055)
    Process item #3. Spent (pack 1 elements) time: 1,001 (from start: 3,058)
    Process item #4. Spent (pack 1 elements) time: 1,001 (from start: 4,061)
    You are able provide result messages, custom message formatting, pack size after precessing write log, auto adjusting
    such pack size to do not spam log, provide custom logging methods and so on.

## ConfigExtended

Main goal to add functionality to `ConfigObject` GDK class.

First it allow operations opposite flatten, set hierarchy from string, like:
```
ConfigExtended conf = …
conf.setFromPropertyPathLikeKey('some.deep.hierarchy.of.properties', value)
```
and then access it as usual:
```
conf.some.deep.hierarchy.of.properties
```
not as it is one string property.
```
conf.'some.deep.hierarchy.of.properties'
```

Additionally it override merge of `ConfigObjects` and do not replace completely replace Objects but set properties of it.
For example standard behaviour:
```
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
assert config.test == 's change'
```

But stop, why config.test replaced? Our intention was to set only their field s!
**That class do that magic**