#!/usr/bin/env bash
#
keytool -importcert -v -trustcacerts -file 'mosquitto.org.crt' -alias certificate -keystore 'mosquitto.bks' -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath 'bcprov-jdk15on-146.jar' -storetype BKS -storepass mosquitto
#
keytool -list -keystore 'mosquitto.bks' -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath 'bcprov-jdk15on-146.jar' -storetype BKS -storepass mosquitto

