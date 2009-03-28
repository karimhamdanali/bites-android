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
	
	public static final String KEY_MSG_TEXT = "messageText";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String message = "asdf";
		
		if (!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			return;
		}
		
		Bundle bdl = intent.getExtras();
		
		Object pdus[] = (Object[])bdl.get("pdus");
		for (int n=0; n<pdus.length; n++) {
			byte byteData[] = (byte[])pdus[n];
			message = SmsMessage.createFromPdu(byteData).getDisplayMessageBody();
		}
		
		if (message.contains("Recipe")) {
			Intent broadcast = new Intent("com.captainfanatic.bites.RECEIVED_RECIPE");
			broadcast.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			broadcast.putExtra(KEY_MSG_TEXT, message);
			try {
				context.startActivity(broadcast);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}				
	}
}
