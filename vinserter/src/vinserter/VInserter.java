// Copyright 2015 Ecosystem Players. All Rights Reserved

package vinserter;

import java.io.File;
import java.io.StringWriter;
import java.io.IOException;
import java.net.URISyntaxException;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class VInserter {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  

	//  Database credentials
	static final String USER = System.getenv("HBA_DB_USER");
	static final String PASS = System.getenv("HBA_DB_PASS");
	static final String DB_URL = "jdbc:" + System.getenv("HBA_DB_URL");
    static final String QUEUE_URL = System.getenv("HBA_QUEUE_URL");
	static Connection conn = null;

	//  AWS SQS credentials
   static AWSCredentials credentials = null;
    static AmazonSQS sqs = null;
    
    
	//  XSL credentials
    static Transformer transformer = null;
    

    
    VInserter() throws Exception {
    	System.out.println("VInserter initializing...");
    	

        credentials = new ProfileCredentialsProvider("default").getCredentials();
 
        sqs = new AmazonSQSClient(credentials);
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        sqs.setRegion(usEast1);       
        
		TransformerFactory factory = TransformerFactory.newInstance();
		Source xslt = new StreamSource(new File("transform2sql.xsl"));
		transformer = factory.newTransformer(xslt);

		
		System.out.println("Registering JDBC driver...");
		Class.forName(JDBC_DRIVER);

		System.out.println("Connecting to database: " + DB_URL);

		conn = DriverManager.getConnection(DB_URL,USER,PASS);

    }
    

	public static void main(String[] args) 
			throws IOException, 
			URISyntaxException, 
			TransformerException, 
			TransformerConfigurationException, 
			SQLException, 
			ClassNotFoundException, Exception {
		

		new VInserter();
		consumeSqs();
		
		
/*		if (args == null) {
			System.out.println("Usage: vinserter <video id> ...");
	        System.exit(0);
		}	
*//*		for (String videoId: args) {

			System.out.println("Command Line Processor Started");

			String sql = xmlToSql(videoId);
			System.out.println(sql);

			sqlToDb(sql);
		}*/
	
	}
	
	public static void consumeSqs () throws Exception {

        try {
            System.out.println("  QueueUrl: " + QUEUE_URL);
       
            // Receive messages
            System.out.println("Receiving messages from MyQueue.\n");
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(QUEUE_URL);
                        	
            List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
            System.out.println("Num Messages:" + messages.size());

            for (Message message : messages) {
				String[] videoIds = message.getBody().split("\\s+");
				for (int i = 0; i < videoIds.length; i++) {
					 System.out.println("  VideoId: " + videoIds[i]);
					 
							String sql = xmlToSql(videoIds[i]);
							System.out.println(sql);
							sqlToDb(sql);

				}
            }
           
            // Delete the message
            System.out.println("Deleting the message.\n");
            String messageRecieptHandle = messages.get(0).getReceiptHandle();
            sqs.deleteMessage(new DeleteMessageRequest(QUEUE_URL, messageRecieptHandle));
            
            // Close DB
          System.out.println("Closing DB connection.\n");
            conn.close();

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "a serious internal problem while trying to communicate with SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

	public static String xmlToSql(String videoid) 
			throws IllegalArgumentException, 
			TransformerException, 
			TransformerFactoryConfigurationError {

		System.out.println("Transforming videoid");

		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");

		Source text = new StreamSource("http://www.youtube.com/api/timedtext?v=" + videoid + "&lang=en");
		System.out.println("URL: " + "http://www.youtube.com/api/timedtext?v=" + videoid + "&lang=en");

		StreamResult result=new StreamResult(new StringWriter());
		transformer.transform(text, result); 

		String xmlString=result.getWriter().toString();

		return xmlString;
	}

	public static void sqlToDb(String sql) throws SQLException, ClassNotFoundException {
		Statement stmt = null;


		System.out.println("Executing statement...");
		stmt = conn.createStatement();
		int ret = stmt.executeUpdate(sql);
		System.out.println("Num rows effected: " + ret);

		stmt.close();
	}

}
