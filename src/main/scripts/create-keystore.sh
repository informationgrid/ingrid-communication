#!/bin/bash

OUT=$1

mkdir -p $OUT

KEY_ALG=DSA
SIG_ALG=SHA1withDSA
GROUP=/torwald-group
KEY_PASS=password
STORE_PASS=password
DNAME='CN=commonName, OU=organizationUnit, O=organization, L=location, S=state, C=country'

servers=( ibus ibus2 )
clients=( mb ms ak )

THIS_DIR=`dirname "$THIS"`


	
	for client in ${clients[@]}; do

		  echo process $client
		  CLIENT_KEYSTORE=$THIS_DIR/$OUT/$client.keystore
		    
		  keytool -genkey  \
				-keystore $CLIENT_KEYSTORE \
				-alias $GROUP:$client \
				-keyalg $KEY_ALG \
				-sigalg $SIG_ALG \
				-keypass $KEY_PASS \
				-storepass $STORE_PASS \
				-dname "$DNAME" \
		  
		  keytool -export \
				-keystore $CLIENT_KEYSTORE \
				-storepass $STORE_PASS \
				-alias $GROUP:$client \
				-file $THIS_DIR/$OUT/$client.cer \

	
			for server in ${servers[@]}; do
			  echo process $server
			  SERVER_KEYSTORE=$THIS_DIR/$OUT/$server.keystore
			  keytool -import \
					-keystore $SERVER_KEYSTORE \
					-storepass $STORE_PASS \
					-alias $GROUP:$client \
					-noprompt \
					-file $THIS_DIR/$OUT/$client.cer \

			done

		  rm  $THIS_DIR/$OUT/$client.cer

		
	done


		
