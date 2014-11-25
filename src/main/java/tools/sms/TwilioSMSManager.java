package tools.sms;

//You may want to be more specific in your imports 
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;

public class TwilioSMSManager {
	
	//SIMEON'S TEST CREDENTIALS
	//TODO: Change this class to hold SpecieBox's account creds
	public static final String ACCOUNT_SID = "AC8eaf989b2be8147aeef8fee80f81ea14"; 
	public static final String AUTH_TOKEN = "a8515a9df5f4b6688297e257521e6a4a";
	private static final String FROMNUMBER = "+16136863717";
	
	public TwilioSMSManager() {
	}
	
	public void sendMessage(String toNumber, String messageText){
		TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
		 
	    // Build a filter for the MessageList
	    List<NameValuePair> params = new ArrayList<NameValuePair>();
	    params.add(new BasicNameValuePair("Body", messageText));
	    params.add(new BasicNameValuePair("To", toNumber));
	    params.add(new BasicNameValuePair("From", FROMNUMBER));
	 
	    MessageFactory messageFactory = client.getAccount().getMessageFactory();
	    Message message;
		try {
			message = messageFactory.create(params);
			System.out.println(message.getSid());
		} catch (TwilioRestException e) {
			e.printStackTrace();
		}
	}
}
