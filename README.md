# ARAM Applet

[![Build Status](https://travis-ci.org/bertrandmartel/aram-applet.svg?branch=master)](https://travis-ci.org/bertrandmartel/aram-applet)
[![Coverage Status](https://coveralls.io/repos/github/bertrandmartel/aram-applet/badge.svg?branch=master)](https://coveralls.io/github/bertrandmartel/aram-applet?branch=master)

JavaCard implementation of Global Platform Access Rule Application Master (ARA-M) applet from [Secure Element Access Control v1.0 specification](https://globalplatform.org/specificationscard.asp).

## What is this ?

ARA-M is an application (typically present on a SIM card) which manage access rules that are enforced by an Access Control Enforcer (typically present on [Android device](https://github.com/seek-for-android/pool/wiki)).
The enforcer makes sure the rules from the ARAM are enforced. An access rule is composed of :
* an AID
* a certificate hash (sha1 of client application cert)
* a set of rules

The Access Control enforcer will allow/deny a client application (for example an Android app) to send APDU to a SE applet based on these rules

More information : [seek-for-android Access Control wiki](https://github.com/seek-for-android/pool/wiki/AccessControlIntroduction)

## Features

### Get Data

- [x] get all
- [x] get specific REF-DO
- [x] get refresh tag
- [x] get next

### Store Data

- [x] store REF-AR-DO
- [x] delete AID-REF-DO
- [x] delete REF-DO
- [ ] delete REF-AR-DO
- [x] update refresh tag

### Note

* store data can be accessed via install for personalization or via raw apdu STORE DATA
* get data length is coded on **2 bytes** max
* get specific is **not** compatible with get next
* rules are not stored as data object but as plain apdu AR-DO
* format of APDU-AR-DO, NFC-AR-DO is not checked
* deleting specific rules is not implemented (only aid/hash)

## Setup

```bash
git clone git@github.com:bertrandmartel/aram-applet.git
cd aram-applet
git submodule update --init
```

* build

```bash
./gradlew build
```

* build & install (will **delete** existing applet before install)

```bash
./gradlew installJavaCard
```

## Tests

* run simulation tests

```bash
./gradlew test
```

* run tests on smartcard

```bash
./gradlew test -DtestMode=smartcard
```

## Scripts

### Install for personalization

* list rules

```bash
gp -acr-list
```

Use [GlobalPlatformPro](https://github.com/martinpaljak/GlobalPlatformPro) to send store data via the Security Domain with install comand + install for personalization :

* add rule

```bash
gp -acr-add -acr-rule 01 -app D2760001180002FF49502589C0019B18 -acr-hash 1FA8CC6CE448894C7011E23BCF56DB9BD9097432
```

* delete rule

```bash
gp -acr-delete -app D2760001180002FF49502589C0019B18 -acr-hash 1FA8CC6CE448894C7011E23BCF56DB9BD9097432
```

### Raw APDU

* list rules

```bash
./gradlew list
```

The following task send store data command raw apdu via [GlobalPlatformPro](https://github.com/martinpaljak/GlobalPlatformPro) (for add & delete) :

* add rule

```bash
./gradlew store
```

or

```bash
./gradlew test --tests fr.bmartel.aram.AramTest.storeDataValid
```

* delete rule

```bash
./gradlew delete
```
or
```bash
./gradlew test --tests fr.bmartel.aram.AramTest.deleteByAid
```

## License

The MIT License (MIT) Copyright (c) 2017 Bertrand Martel