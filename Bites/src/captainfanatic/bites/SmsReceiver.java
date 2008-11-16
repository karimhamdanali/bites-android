package captainfanatic.bites;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;

/** Listen for received sms and interpret raw pdus into messages to check for recipes
 * 
 * @author ben
 *
 */
public class SmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String message;
		
		if (!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			return;
		}
		
		Bundle bdl = intent.getExtras();
		
		Object pdus[] = (Object[])bdl.get("pdus");
		for (int n=0; n<pdus.length; n++) {
			byte byteData[] = (byte[])pdus[n];
			message = SmsMessage.createFromPdu(byteData).getDisplayMessageBody();
		}
		
		//TODO: parse the message and create a recipe object if relevant
	
	}
}
