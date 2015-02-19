#Copyright 2015 Ecosystem Players

How to install
	1-FTP dist/jar file & transform.xsl
	2-cp mysql*jar (platform indep version) under 
		Java_Home\jre\lib\
		Java_Home\jre\lib\ext\
		Java_Home\lib
How to Run
	1-Set ENV variables
		export HBA_DB_PASS=
		export HBA_DB_URL=mysql://hba-mysql.cubbaktffmwm.us-east-1.rds.amazonaws.com/videots
		export HBA_DB_USER=
		export HBA_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/741793752734/videotsqs
		
		Copy AMI ext-lib folder such as: 
			sudo cp -i XXX /usr/lib/jvm/jre-1.7.0-openjdk.x86_64/lib/ext/
	
	2-Run
		Insert to SQS videoid's: eZPGsrXR4io 4vUB363cRqE
		java -jar VInserter.jar 
	