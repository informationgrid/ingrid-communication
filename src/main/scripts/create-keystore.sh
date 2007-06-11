#!/bin/bash

OUT=$1

mkdir -p $OUT

KEY_ALG=DSA
SIG_ALG=SHA1withDSA
GROUP=/torwald-group
KEY_PASS=password
STORE_PASS=password
DNAME='CN=commonName, OU=organizationUnit, O=organization, L=location, S=state, C=country'

servers=( torwald-ibus )
clients=( 	torwald-iplug-management \
			torwald-iplug-g2k-uok_by \
			torwald-iplug-g2k-upb \
			torwald-iplug-g2k-uok2_by \
			torwald-iplug-g2k-ulidat \
			torwald-iplug-g2k-ufordat \
			torwald-iplug-fpn \
			iplug-torwald-sns \
			kug-iplug-csw-portalu \
			kug-iplug-csw-sdi \
			kug-iplug-csw-wsv \
			kug-iplug-csw-wemove \
			iplug-torwald-ecs-portalu \
			iplug-torwald-ecs-ni \
			torwald-iplug-udk-db_bw \
			torwald-iplug-udk-db_hh \
			torwald-iplug-udk-db_ni \
			torwald-iplug-udk-db_rp \
			torwald-iplug-udk-db_sl \
			torwald-iplug-udk-db_uba \
			torwald-iplug-udk-db_test \
			torwald-iplug-udk-db_bw_addr \
			torwald-iplug-udk-db_hh_addr \
			torwald-iplug-udk-db_ni_addr \
			torwald-iplug-udk-db_rp_addr \
			torwald-iplug-udk-db_sl_addr \
			torwald-iplug-udk-db_uba_addr \
			torwald-iplug-udk-db_test_addr \
			iplug-torwald-dsc \
			iplug-se-search \
			iplug-se-index \
			opensearch \
			portal \
			torwald-iplug-jm \
			torwald-iplug-tk \
			torwald-iplug-ak \
			torwald-iplug-ms \
			torwald-iplug-mb )

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

#		  rm  $THIS_DIR/$OUT/$client.cer

		
	done


		
